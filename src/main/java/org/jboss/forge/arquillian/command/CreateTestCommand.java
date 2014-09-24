package org.jboss.forge.arquillian.command;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.parser.java.resources.JavaResourceVisitor;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.visit.VisitContext;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UISelection;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.api.TestFrameworkFacet;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.JavaType;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaSource;

@FacetConstraint(ArquillianFacet.class)
public class CreateTestCommand extends AbstractProjectCommand implements UICommand
{

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
   @WithAttributes(shortName = 't', label = "Targets", required = true, type = InputType.JAVA_CLASS_PICKER)
   private UISelectMany<JavaClassSource> targets;

   @Inject
   @WithAttributes(shortName = 'e', label = "Enable JPA", required = false)
   private UIInput<Boolean> enableJPA;

   @Inject
   @WithAttributes(shortName = 'a', label = "Archive Type", defaultValue = "JAR")
   private UISelectOne<ArchiveType> archiveType;

   @Override
   public UICommandMetadata getMetadata(UIContext context)
   {
      return Metadata.from(super.getMetadata(context), getClass())
               .category(Categories.create("Arquillian"))
               .name("Arquillian: Create Test")
               .description("This addon will help you create a test skeleton based on a given class");
   }

   @Override
   public void initializeUI(final UIBuilder builder) throws Exception
   {
      builder.add(targets).add(enableJPA).add(archiveType);

      Project project = getSelectedProject(builder);
      final List<JavaClassSource> sources = new ArrayList<>();
      project.getFacet(JavaSourceFacet.class).visitJavaSources(new JavaResourceVisitor()
      {
         @Override
         public void visit(VisitContext context, JavaResource javaResource)
         {
            JavaType<?> javaType;
            try
            {
               javaType = javaResource.getJavaType();
               if (javaType.isClass())
               {
                  sources.add((JavaClassSource) javaType);
               }
            }
            catch (FileNotFoundException e)
            {
               // Do nothing
            }
         }
      });
      targets.setItemLabelConverter(new Converter<JavaClassSource, String>()
      {
         @Override
         public String convert(JavaClassSource source)
         {
            return source == null ? null : source.getQualifiedName();
         }
      });

      targets.setValueChoices(sources);

      UISelection<Object> initialSelection = builder.getUIContext().getInitialSelection();
      if (initialSelection.get() instanceof JavaResource)
      {
         JavaResource javaResource = (JavaResource) initialSelection.get();
         JavaType<?> javaType = javaResource.getJavaType();
         if (javaType.isClass())
         {
            targets.setDefaultValue(Arrays.asList((JavaClassSource) javaType));
         }
      }
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception
   {
      List<Result> results = new ArrayList<>();
      UIContext uiContext = context.getUIContext();
      List<JavaResource> resources = new ArrayList<>();
      for (JavaClassSource clazz : targets.getValue())
      {
         JavaResource test = createTest(getSelectedProject(context), clazz, enableJPA.getValue(),
                  archiveType.getValue());
         resources.add(test);
         results.add(Results.success("Created test class " + test.getJavaType().getQualifiedName()));
      }
      if (!resources.isEmpty())
         uiContext.setSelection(resources);
      return Results.aggregate(results);
   }

   @Override
   protected boolean isProjectRequired()
   {
      return true;
   }

   @Override
   protected ProjectFactory getProjectFactory()
   {
      return projectFactory;
   }

   private JavaResource createTest(Project project, JavaClassSource classUnderTest, boolean enableJPA, ArchiveType type)
            throws FileNotFoundException
   {
      final TestFrameworkFacet testFrameworkFacet = project.getFacet(TestFrameworkFacet.class);
      final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

      final VelocityContext context = initializeVelocityContext(enableJPA, type, classUnderTest);

      final StringWriter writer = new StringWriter();
      Velocity.mergeTemplate(testFrameworkFacet.getTemplateName(), "UTF-8", context, writer);

      final JavaSource<?> testClass = Roaster.parse(JavaSource.class, writer.toString());
      return java.saveTestJavaSource(testClass);
   }

   private VelocityContext initializeVelocityContext(boolean enableJPA, ArchiveType type, JavaSource<?> javaSource)
   {
      VelocityContext context = new VelocityContext();
      context.put("package", javaSource.getPackage());
      context.put("ClassToTest", javaSource.getName());
      context.put("classToTest", javaSource.getName().toLowerCase());
      context.put("packageImport", javaSource.getPackage());
      context.put("enableJPA", enableJPA);
      context.put("archiveType", type);
      return context;
   }
}
