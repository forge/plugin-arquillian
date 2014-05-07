package org.jboss.forge.arquillian.command;

import java.util.Properties;

import org.apache.velocity.app.Velocity;


public class CreateTestCommand {
   static
   {
      Properties properties = new Properties();
      properties.setProperty("resource.loader", "class");
      properties.setProperty("class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

      Velocity.init(properties);
   }
/*
   
   @Command(value = "create-test", help = "Create a new test class with a default @Deployment method")
   public void createTest(
         @Option(name = "class", required = true, type = PromptType.JAVA_CLASS) JavaResource classUnderTest,
         @Option(name = "enableJPA", required = false, flagOnly = true) boolean enableJPA, final PipeOut out)
         throws FileNotFoundException
   {
      final TestFrameworkFacet testFrameworkFacet = project.getFacet(TestFrameworkFacet.class);
      final JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);
      final JavaSource<?> javaSource = classUnderTest.getJavaSource();

      final VelocityContext context = initializeVelocityContext(enableJPA, javaSource);

      final StringWriter writer = new StringWriter();
      Velocity.mergeTemplate(testFrameworkFacet.getTemplateName(), "UTF-8", context, writer);

      final JavaClass testClass = JavaParser.parse(JavaClass.class, writer.toString());
      java.saveTestJavaSource(testClass);
      pickup.fire(new PickupResource(java.getTestJavaResource(testClass)));
   }

   private VelocityContext initializeVelocityContext(boolean enableJPA, JavaSource<?> javaSource)
   {
      VelocityContext context = new VelocityContext();
      context.put("package", javaSource.getPackage());
      context.put("ClassToTest", javaSource.getName());
      context.put("classToTest", javaSource.getName().toLowerCase());
      context.put("packageImport", javaSource.getPackage());
      context.put("enableJPA", enableJPA);
      return context;
   }

*/
}
