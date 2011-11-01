package org.jboss.forge.arquillian.container;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class Dependency {
    private String group_id;
    private String artifact_id;
    private String url;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
