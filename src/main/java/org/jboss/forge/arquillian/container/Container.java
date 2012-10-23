package org.jboss.forge.arquillian.container;

import java.util.List;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class Container {
    private String group_id;
    private String artifact_id;
    private String name;
    private ContainerType containerType;
    private List<Dependency> dependencies;
    private Dependency download;
    private List<Configuration> configurations;

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getArtifact_id() {
        return artifact_id;
    }

    public void setArtifact_id(String artifact_id) {
        this.artifact_id = artifact_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public void setContainerType(ContainerType containerType) {
        this.containerType = containerType;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Dependency getDownload() {
        return download;
    }

    public void setDownload(Dependency download) {
        this.download = download;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    public String getId() {
        return name.replace("Arquillian Container ", "").replaceAll(" ", "_").toUpperCase();
    }

    @Override
    public String toString() {
        return getId();
    }
}
