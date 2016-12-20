package org.jboss.forge.arquillian.command;

import org.jboss.forge.addon.dependencies.DependencyQuery;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.input.InputComponent;
import org.jboss.forge.addon.ui.input.InputComponentFactory;
import org.jboss.forge.addon.ui.input.UISelectOne;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;
import org.jboss.forge.arquillian.api.ArquillianConfig;
import org.jboss.forge.arquillian.api.ArquillianFacet;
import org.jboss.forge.arquillian.api.ContainerInstallEvent;
import org.jboss.forge.arquillian.container.ContainerInstaller;
import org.jboss.forge.arquillian.container.model.Container;
import org.jboss.forge.arquillian.container.model.Dependency;
import org.jboss.forge.arquillian.util.DependencyUtil;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AddContainerDependencyStep extends AbstractProjectCommand implements UIWizardStep {

    @Inject
    private InputComponentFactory inputFactory;

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private ContainerInstaller containerInstaller;

    @Inject
    private DependencyResolver resolver;

    @Inject
    @Any
    private Event<ContainerInstallEvent> installEvent;

    private final Map<Dependency, InputComponent<?, String>> dependencyVersions = new HashMap<>();

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), getClass())
                .category(Categories.create("Arquillian"))
                .name("Arquillian: Add Container")
                .description("This addon will help you setup a Arquillian Container Adapter");
    }

    @Override
    public void initializeUI(final UIBuilder builder) throws Exception {

        Container selectedContainer = (Container) builder.getUIContext().getAttributeMap().get(ContainerSetupWizard.CTX_CONTAINER);
        if (selectedContainer == null || selectedContainer.getDependencies() == null) {
            return;
        }
        for (final Dependency dependency : selectedContainer.getDependencies()) {
            UISelectOne<String> dependencyVersion = inputFactory.createSelectOne(dependency.getArtifactId() + "-version", String.class);
            builder.add(dependencyVersion);
            dependencyVersions.put(dependency, dependencyVersion);

            final DependencyQuery dependencyCoordinate = DependencyQueryBuilder.create(
                    DependencyBuilder.create()
                            .setGroupId(dependency.getGroupId())
                            .setArtifactId(dependency.getArtifactId())
                            .getCoordinate());

            dependencyVersion.setEnabled(true);
            dependencyVersion.setValueChoices(() -> DependencyUtil.toVersionString(
                    resolver.resolveVersions(dependencyCoordinate)));
            dependencyVersion.setDefaultValue(() -> DependencyUtil.getLatestNonSnapshotVersionCoordinate(
                    resolver.resolveVersions(dependencyCoordinate)));

        }
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        Map<Object, Object> ctx = context.getUIContext().getAttributeMap();
        Container container = (Container) ctx.get(ContainerSetupWizard.CTX_CONTAINER);
        String version = (String) ctx.get(ContainerSetupWizard.CTX_CONTAINER_VERSION);

        containerInstaller.installContainer(
                getSelectedProject(context),
                container,
                version,
                getVersionedDependenciesMap());

        ArquillianFacet arquillian = getSelectedProject(context).getFacet(ArquillianFacet.class);
        ArquillianConfig config = arquillian.getConfig();
        config.addContainer(container.getProfileId());
        arquillian.setConfig(config);

        installEvent.fire(new ContainerInstallEvent(container));

        return Results.success("Installed " + container.getName() + " dependencies");
    }

    @Override
    protected boolean isProjectRequired() {
        return true;
    }

    @Override
    public boolean isEnabled(UIContext context) {
        Boolean parent = super.isEnabled(context);
        if (parent) {
            return getSelectedProject(context).hasFacet(ArquillianFacet.class);
        }
        return parent;
    }

    @Override
    protected ProjectFactory getProjectFactory() {
        return projectFactory;
    }

    private Map<Dependency, String> getVersionedDependenciesMap() {
        if (dependencyVersions.isEmpty()) {
            return null;
        }
        Map<Dependency, String> resolved = new HashMap<>();
        for (Map.Entry<Dependency, InputComponent<?, String>> dep : dependencyVersions.entrySet()) {
            resolved.put(dep.getKey(), (String) dep.getValue().getValue());
        }
        return resolved;
    }

    @Override
    public NavigationResult next(UINavigationContext context) throws Exception {
        return null;
    }
}
