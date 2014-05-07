package org.jboss.forge.arquillian.command;

import java.util.Arrays;
import java.util.concurrent.Callable;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
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
import org.jboss.forge.arquillian.api.ContainerInstallEvent;
import org.jboss.forge.arquillian.container.ContainerInstaller;
import org.jboss.forge.arquillian.container.ContainerResolver;
import org.jboss.forge.arquillian.container.model.Container;
import org.jboss.forge.arquillian.container.model.ContainerType;
import org.jboss.forge.arquillian.util.DependencyUtil;

public class AddContainerCommand extends AbstractProjectCommand implements UICommand {

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private ContainerInstaller containerInstaller;

   @Inject
   private ContainerResolver containerCompleter;

   @Inject
   private DependencyResolver resolver;

   @Inject
   @Any
   private Event<ContainerInstallEvent> installEvent;

   @Inject
   @WithAttributes(shortName = 't', label = "Container Adapter", type = InputType.DROPDOWN, required = false)
   private UISelectOne<ContainerType> containerAdapterType;

   @Inject
   @WithAttributes(shortName = 'c', label = "Container Adapter", type = InputType.DROPDOWN)
   private UISelectOne<Container> containerAdapter;

   @Inject
   @WithAttributes(shortName = 'x', label = "Container Adapter Version", type = InputType.DROPDOWN)
   private UISelectOne<String> containerAdapterVersion;

   @Override
   public UICommandMetadata getMetadata(UIContext context) {
      return Metadata.from(super.getMetadata(context), getClass())
            .category(Categories.create("Arquillian"))
            .name("Arquillian: Add Container")
            .description("This addon will help you setup a Arquillian Container Adapter");
   }

   @Override
   public void initializeUI(UIBuilder builder) throws Exception {
      builder.add(containerAdapterType)
             .add(containerAdapter)
             .add(containerAdapterVersion);
   
      containerAdapterType.setValueChoices(Arrays.asList(ContainerType.values()));
      containerAdapter.setValueChoices(new Callable<Iterable<Container>>() {
         @Override
         public Iterable<Container> call() throws Exception {
            return containerCompleter.getContainers(containerAdapterType.getValue());
         }
      });
      
      containerAdapterVersion.setEnabled(new Callable<Boolean>() {
         @Override
         public Boolean call() throws Exception {
            return containerAdapter.hasValue();
         }
      });
      containerAdapterVersion.setValueChoices(new Callable<Iterable<String>>() {
         @Override
         public Iterable<String> call() throws Exception {
            return DependencyUtil.toVersionString(
                  resolver.resolveVersions(
                        DependencyQueryBuilder.create(
                              containerAdapter.getValue().asDependency().getCoordinate())));
         }
      });
      containerAdapterVersion.setDefaultValue(new Callable<String>() {
         @Override
         public String call() throws Exception {
            return DependencyUtil.getLatestNonSnapshotVersionCoordinate(
                  resolver.resolveVersions(
                        DependencyQueryBuilder.create(
                              containerAdapter.getValue().asDependency().getCoordinate())));
         }
      });
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      containerInstaller.installContainer(
            getSelectedProject(context),
            containerAdapter.getValue(),
            containerAdapterVersion.getValue());

      ArquillianFacet arquillian = getSelectedProject(context).getFacet(ArquillianFacet.class);
      ArquillianConfig config = arquillian.getConfig();
      config.addContainer(containerAdapter.getValue().getProfileId());
      arquillian.setConfig(config);

      installEvent.fire(new ContainerInstallEvent(containerAdapter.getValue()));

      return Results.success("Installed");
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

   /*


   @Command(value = "configure-container")
   public void configureContainer(@Option(name = "profile", required = true, completer = ProfileCommandCompleter.class) String profileId)
   {
      // loop, user presses ctrl-c to exit
      while (true)
      {
         Profile profile = getProfile(profileId);
         Container container;
         try
         {
            container = getContainer(profile);
         } catch (IOException e)
         {
            throw new RuntimeException(e);
         }

         // TODO: show current values in options list
         Configuration configuration = shell.promptChoiceTyped(
               "Which property do you want to set? (default values shown)\n(Press Enter to return to shell)",
               container.getConfigurations(), null);
         if (configuration == null)
         {
            break;
         }

         JavaSourceFacet resources = project.getFacet(JavaSourceFacet.class);
         FileResource<?> resource = (FileResource<?>) resources.getTestSourceDirectory().getChild("arquillian.xml");

         Node xml = null;
         if (!resource.exists())
         {
            xml = createNewArquillianConfig();
         } else
         {
            xml = XMLParser.parse(resource.getResourceInputStream());
         }

         // TODO show current value
         String value = shell.prompt("What value do you want to assign to the " + configuration.getName() + " property?");
         addPropertyToArquillianConfig(xml, container.getId(), configuration.getName(), value);

         resource.setContents(XMLParser.toXMLString(xml));
      }
   }


    */
}
