package org.jboss.forge.arquillian.command;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UINavigationContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.NavigationResult;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.wizard.UIWizard;
import org.jboss.forge.arquillian.api.ArquillianFacet;

import javax.inject.Inject;

public class ContainerSetupWizard extends AbstractProjectCommand implements UIWizard {

    static final String CTX_CONTAINER = "arq-container";
    static final String CTX_CONTAINER_VERSION = "arq-container-version";

    @Inject
    private ProjectFactory projectFactory;

    @Override
    public UICommandMetadata getMetadata(UIContext context) {
        return Metadata.from(super.getMetadata(context), getClass())
                .category(Categories.create("Arquillian"))
                .name("Arquillian: Container Setup")
                .description("This addon will guide you through adding a Container Adapter for Arquillian");
    }

    @Override
    public void initializeUI(final UIBuilder builder) throws Exception {
    }

    @Override
    public Result execute(UIExecutionContext context) throws Exception {
        return Results.success("Arquillian container setup complete");
    }

    @Override
    @SuppressWarnings("unchecked")
    public NavigationResult next(UINavigationContext context) throws Exception {
        return Results.navigateTo(
                AddContainerStep.class,
                AddContainerDependencyStep.class);
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

}
