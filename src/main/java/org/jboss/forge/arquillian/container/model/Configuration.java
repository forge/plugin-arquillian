/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.container.model;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class Configuration {
    private String type;
    private String description;
    private String name;
    private String defaultValue;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefault() {
        return defaultValue;
    }

    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return " " + type.replaceFirst("^java\\.lang\\.", "") + " " + name + " = " + ("java.lang.String".equals(type) ? '"' + defaultValue + '"' : defaultValue) + "; // " + description + " ";
    }

    /*
    @Override
    public String toString() {
        return "Configuration{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }*/
}
