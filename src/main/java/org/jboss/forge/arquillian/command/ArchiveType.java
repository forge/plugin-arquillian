/**
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 * <p>
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.arquillian.command;

import org.jboss.forge.roaster.model.util.Types;

/**
 * The Archive type to be generated in the tests
 *
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public enum ArchiveType {
    JAR("org.jboss.shrinkwrap.api.spec.JavaArchive"),
    WAR("org.jboss.shrinkwrap.api.spec.WebArchive");

    private final String className;
    private final String simpleClassName;

    ArchiveType(String className) {
        this.className = className;
        this.simpleClassName = Types.toSimpleName(className);
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }
}
