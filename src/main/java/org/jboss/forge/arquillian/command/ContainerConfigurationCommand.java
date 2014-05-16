package org.jboss.forge.arquillian.command;

import java.util.Collections;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.maven.model.Profile;
import org.jboss.forge.addon.convert.Converter;
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
import org.jboss.forge.arquillian.api.ArquillianConfig;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.container.ProfileManager;
import org.jboss.forge.arquillian.container.model.Configuration;

public class ContainerConfigurationCommand extends AbstractProjectCommand implements UICommand {

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private ProfileManager profileManager;   
   
   @Inject
   @WithAttributes(shortName = 'c', label = "Container", type = InputType.DROPDOWN)
   private UISelectOne<Profile> container;

   @Inject
   @WithAttributes(shortName = 'o', label = "Container Configuration Option", type = InputType.DROPDOWN, required = true)
   private UISelectOne<Configuration> containerOption;

   @Inject
   @WithAttributes(shortName = 'v', label = "Container Configuration Value", type = InputType.DROPDOWN)
   private UISelectOne<String> containerValue;

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass())
            .category(Categories.create("Arquillian"))
            .name("Arquillian: Container Configuration")
            .description("This addon will help you configure the Container for Arquillian");
   }
   
   @Override
   public void initializeUI(final UIBuilder builder) throws Exception {
      builder.add(container)
             .add(containerOption)
             .add(containerValue);
      
      container.setValueChoices(new Callable<Iterable<Profile>>() {
         @Override
         public Iterable<Profile> call() throws Exception {
            return profileManager.getArquillianProfiles(getSelectedProject(builder.getUIContext()));
         }
      });
      container.setItemLabelConverter(new Converter<Profile, String>() {
         @Override
         public String convert(Profile source) {
            if(source == null) {
               return null;
            }
            return source.getId();
         }
      });
      containerOption.setEnabled(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return container.hasValue();
         }
      });
      containerOption.setItemLabelConverter(new Converter<Configuration, String>() {
         @Override
         public String convert(Configuration source) {
            if(source == null) {
               return null;
            }
            return source.getName();
         }
      });
      containerOption.setValueChoices(new Callable<Iterable<Configuration>>() {
         @Override
         public Iterable<Configuration> call() throws Exception {
            if(containerOption.isEnabled()) {
               Iterable<Configuration> config = profileManager.getContainer(container.getValue()).getConfigurations();
               if(config != null) {
                  return config;
               }
            }
            return Collections.emptyList();
         }
      });

      containerValue.setEnabled(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return containerOption.hasValue();
         }
      });
      containerValue.setDefaultValue(new Callable<String>() {
         @Override
         public String call() throws Exception {
            if(containerValue.isEnabled()) {
               return containerOption.getValue().getDefault();
            }
            return null;
         }
      });
      containerValue.setRequired(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            if(containerValue.isEnabled()) {
               return containerOption.getValue().getDefault() != null;
            }
            return true;
         }
      });
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      ArquillianFacet arquillian = getSelectedProject(context).getFacet(ArquillianFacet.class);
      ArquillianConfig config = arquillian.getConfig();
      config.addContainerProperty(
            container.getValue().getId(),
            containerOption.getValue().getName(),
            containerValue.getValue());
      arquillian.setConfig(config);
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
}
