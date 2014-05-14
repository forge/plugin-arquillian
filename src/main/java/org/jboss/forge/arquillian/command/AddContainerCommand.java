package org.jboss.forge.arquillian.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.forge.addon.convert.Converter;
import org.jboss.forge.addon.dependencies.DependencyQuery;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.input.ValueChangeListener;
import org.jboss.forge.addon.ui.input.events.ValueChangeEvent;
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
import org.jboss.forge.arquillian.container.model.Dependency;
import org.jboss.forge.arquillian.util.DependencyUtil;

public class AddContainerCommand extends AbstractProjectCommand implements UICommand {

   @Inject
   private InputComponentFactory inputFactory;

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private ContainerInstaller containerInstaller;

   @Inject
   private ContainerResolver containerResolver;

   @Inject
   private DependencyResolver resolver;

   @Inject
   @Any
   private Event<ContainerInstallEvent> installEvent;

   private final Map<Dependency, InputComponent<?, String>> dependencyVersions = new HashMap<>();

   @Inject
   @WithAttributes(shortName = 't', label = "Container Adapter Type", type = InputType.DROPDOWN, required = false)
   private UISelectOne<ContainerType> containerAdapterType;

   @Inject
   @WithAttributes(shortName = 'c', label = "Container Adapter", type = InputType.DROPDOWN, required = true)
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
   public void initializeUI(final UIBuilder builder) throws Exception {
      builder.add(containerAdapterType)
             .add(containerAdapter)
             .add(containerAdapterVersion);
   
      containerAdapterType.setValueChoices(Arrays.asList(ContainerType.values()));
      containerAdapterType.setEnabled(true);
      containerAdapter.setEnabled(true);
      containerAdapter.setValueChoices(new Callable<Iterable<Container>>() {
         @Override
         public Iterable<Container> call() throws Exception {
            return containerResolver.getContainers(containerAdapterType.getValue());
         }
      });
      containerAdapter.setItemLabelConverter(new Converter<Container, String>() {
         @Override
         public String convert(Container source) {
            return source.getId();
         }
      });
      containerAdapter.addValueChangeListener(new ValueChangeListener() {

         @Override
         public void valueChanged(ValueChangeEvent event) {
            Container selectedContainer = (Container)event.getNewValue();
            if(selectedContainer == null || selectedContainer.getDependencies() == null) {
               return;
            }
            for(final Dependency dependency : selectedContainer.getDependencies()) {
               UISelectOne<String> dependencyVersion = inputFactory.createSelectOne(dependency.getArtifactId() + "-version", String.class);
               builder.add(dependencyVersion);
               dependencyVersions.put(dependency, dependencyVersion);
               
               final DependencyQuery dependencyCoordinate = DependencyQueryBuilder.create(
                     DependencyBuilder.create()
                     .setGroupId(dependency.getGroupId())
                     .setArtifactId(dependency.getArtifactId())
                     .getCoordinate());

               dependencyVersion.setEnabled(true);
               dependencyVersion.setValueChoices(new Callable<Iterable<String>>() {
                  @Override
                  public Iterable<String> call() throws Exception {
                     return DependencyUtil.toVersionString(
                           resolver.resolveVersions(dependencyCoordinate));
                  }
               });
               dependencyVersion.setDefaultValue(new Callable<String>() {
                  @Override
                  public String call() throws Exception {
                     return DependencyUtil.getLatestNonSnapshotVersionCoordinate(
                           resolver.resolveVersions(dependencyCoordinate));
                  }
               });

            }
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
            if(containerAdapterVersion.isEnabled()) {
               return DependencyUtil.toVersionString(
                     resolver.resolveVersions(
                           DependencyQueryBuilder.create(
                                 containerAdapter.getValue().asDependency().getCoordinate())));
            }
            return Collections.emptyList();
         }
      });
      containerAdapterVersion.setDefaultValue(new Callable<String>() {
         @Override
         public String call() throws Exception {
            if(containerAdapter.hasValue()) {
               return DependencyUtil.getLatestNonSnapshotVersionCoordinate(
                     resolver.resolveVersions(
                           DependencyQueryBuilder.create(
                                 containerAdapter.getValue().asDependency().getCoordinate())));
            }
            return null;
         }
      });
   }

   @Override
   public Result execute(UIExecutionContext context) throws Exception {
      containerInstaller.installContainer(
            getSelectedProject(context),
            containerAdapter.getValue(),
            containerAdapterVersion.getValue(),
            getVersionedDependenciesMap());

      ArquillianFacet arquillian = getSelectedProject(context).getFacet(ArquillianFacet.class);
      ArquillianConfig config = arquillian.getConfig();
      config.addContainer(containerAdapter.getValue().getProfileId());
      arquillian.setConfig(config);

      installEvent.fire(new ContainerInstallEvent(containerAdapter.getValue()));

      return Results.success("Installed " + containerAdapter.getValue().getName());
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

   private Map<Dependency, String> getVersionedDependenciesMap() {
      if(dependencyVersions.isEmpty()) {
         return null;
      }
      Map<Dependency, String> resolved = new HashMap<>();
      for(Map.Entry<Dependency, InputComponent<?, String>> dep : dependencyVersions.entrySet()) {
         resolved.put(dep.getKey(), (String)dep.getValue().getValue());
      }
      return resolved;
   }
}
