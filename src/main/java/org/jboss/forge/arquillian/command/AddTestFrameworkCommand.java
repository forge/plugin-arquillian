package org.jboss.forge.arquillian.command;

import java.util.concurrent.Callable;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.api.TestFrameworkFacet;
import org.jboss.forge.arquillian.api.TestFrameworkInstallEvent;

public class AddTestFrameworkCommand extends AbstractProjectCommand implements UICommand {

   @Inject
   private ProjectFactory projectFactory;
   
   @Inject
   private FacetFactory facetFactory;

   @Inject
   @Any
   private Event<TestFrameworkInstallEvent> installEvent;

   @Inject
   @WithAttributes(shortName = 't', label = "Test Framework", type = InputType.DROPDOWN)
   private UISelectOne<TestFrameworkFacet> testFramework;

   @Inject
   @WithAttributes(shortName = 'f', label = "Test Framework Version", type = InputType.DROPDOWN)
   private UISelectOne<String> testFrameworkVersion;

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass())
            .category(Categories.create("Arquillian"))
            .name("Arquillian: Add TestFramework")
            .description("This addon will help you setup a testframework for Arquillian");
   }
   
   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(testFramework)
             .add(testFrameworkVersion);

      testFramework.setEnabled(true);
      testFramework.setItemLabelConverter(new Converter<TestFrameworkFacet, String>() {
         @Override
         public String convert(TestFrameworkFacet source) {
            return source.getFrameworkName().toLowerCase();
         }
      });
      testFramework.setRequired(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return true; // check if already installed
         }
      });

      testFrameworkVersion.setRequired(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return true;
         }
      });
      testFrameworkVersion.setEnabled(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return testFramework.hasValue();
         }
      });
      testFrameworkVersion.setValueChoices(new Callable<Iterable<String>>() {
         @Override
         public Iterable<String> call() throws Exception {
            return testFramework.getValue().getAvailableVersions();
         }
      });
      testFrameworkVersion.setDefaultValue(new Callable<String>() {
         @Override
         public String call() throws Exception {
            return testFrameworkVersion.isEnabled() ? testFramework.getValue().getDefaultVersion():null;
         }
      });
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      TestFrameworkFacet selectedTestFramework = testFramework.getValue();
      try {
         selectedTestFramework.setVersion(testFrameworkVersion.getValue());
         facetFactory.install(getSelectedProject(context), selectedTestFramework);
         installEvent.fire(new TestFrameworkInstallEvent(selectedTestFramework));

         return Results.success(selectedTestFramework + " installed");
      } catch(Exception e) {
         return Results.fail("Could not install Test Framework " + selectedTestFramework, e);
      }
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
}
