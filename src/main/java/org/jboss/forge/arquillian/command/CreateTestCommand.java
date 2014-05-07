package org.jboss.forge.arquillian.command;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.api.TestFrameworkFacet;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaSource;

public class CreateTestCommand extends AbstractProjectCommand implements UICommand {
   
   static
   {
      Properties properties = new Properties();
      properties.setProperty("resource.loader", "class");
      properties.setProperty("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

      Velocity.init(properties);
   }

   @Inject
   private ProjectFactory projectFactory;
   
   @Inject
   @WithAttributes(shortName = 's', label = "Target source", required = true, type = InputType.JAVA_CLASS_PICKER)
   private UIInput<JavaResource> value;

   @Inject
   @WithAttributes(shortName = 'e', label = "Enable JPA", defaultValue = "true", required = false)
   private UIInput<Boolean> enableJPA;

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass())
            .category(Categories.create("Arquillian"))
            .name("Arquillian: Create Test")
            .description("This addon will help you create a test skeleton based on a given class");
   }
   
   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(value)
             .add(enableJPA);
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      createTest(getSelectedProject(context), value.getValue(), enableJPA.getValue());
      return Results.success();
   }
   
   @Override
   protected boolean isProjectRequired() {
      return true;
   }

   @Override
   public boolean isEnabled(UIContext context) {
      Boolean parent = super.isEnabled(context);
      if(parent) {
         return getSelectedProject(context).hasFacet(ArquillianFacet.class);
      }
      return parent;
   }

   @Override
   protected ProjectFactory getProjectFactory() {
      return projectFactory;
   }

   private void createTest(Project project, JavaResource classUnderTest, boolean enableJPA)
         throws FileNotFoundException
   {
      final TestFrameworkFacet testFrameworkFacet = project.getFacet(TestFrameworkFacet.class);
      final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      
      final JavaSource<?> javaSource = Roaster.parse(JavaSource.class, classUnderTest.getContents());

      final VelocityContext context = initializeVelocityContext(enableJPA, javaSource);

      final StringWriter writer = new StringWriter();
      Velocity.mergeTemplate(testFrameworkFacet.getTemplateName(), "UTF-8", context, writer);

      final JavaSource<?> testClass = Roaster.parse(JavaSource.class, writer.toString());
      java.saveTestJavaSource(testClass);
      //pickup.fire(new PickupResource(java.getTestJavaResource(testClass)));
   }

   private VelocityContext initializeVelocityContext(boolean enableJPA, JavaSource<?> javaSource) {
      VelocityContext context = new VelocityContext();
      context.put("package", javaSource.getPackage());
      context.put("ClassToTest", javaSource.getName());
      context.put("classToTest", javaSource.getName().toLowerCase());
      context.put("packageImport", javaSource.getPackage());
      context.put("enableJPA", enableJPA);
      return context;
   }
}
