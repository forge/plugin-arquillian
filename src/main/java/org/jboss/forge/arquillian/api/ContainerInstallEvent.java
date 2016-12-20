/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.api;

import org.jboss.forge.arquillian.container.model.Container;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerInstallEvent {
    private final Container container;

    public ContainerInstallEvent(Container container) {
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }
}
