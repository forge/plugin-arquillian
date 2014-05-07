package org.jboss.forge.arquillian.command;

import java.util.concurrent.Callable;

import javax.inject.Inject;

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

public class AddArquillianCommand  extends AbstractProjectCommand implements UICommand {

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private FacetFactory facetFactory;

   @Inject
   private ArquillianFacet facet;

   @Inject
   @WithAttributes(shortName = 'v', label = "Arquillian Core version", type = InputType.DROPDOWN)
   private UISelectOne<String> arquillianVersion;

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass())
            .category(Categories.create("Arquillian"))
            .name("Arquillian: Add")
            .description("This addon will help you setup the base Arquillian");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(arquillianVersion);
      
      arquillianVersion.setDefaultValue(new Callable<String>() {
         @Override
         public String call() throws Exception {
            return facet.getDefaultVersion();
         }
      });
      arquillianVersion.setValueChoices(new Callable<Iterable<String>>() {
         @Override
         public Iterable<String> call() throws Exception {
            return facet.getAvailableVersions();
         }
      });
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      facet.setVersion(arquillianVersion.getValue());
      facetFactory.install(getSelectedProject(context), facet);
      return Results.success("Installed Arquillian " + arquillianVersion.getValue());
   }

   @Override
   protected boolean isProjectRequired() {
      return true;
   }
   
   @Override
   public boolean isEnabled(UIContext context) {
      Boolean parent = super.isEnabled(context);
      if(parent) {
         return !getSelectedProject(context).hasFacet(ArquillianFacet.class);
      }
      return parent;
   }

   @Override
   protected ProjectFactory getProjectFactory() {
      return projectFactory;
   }
}
