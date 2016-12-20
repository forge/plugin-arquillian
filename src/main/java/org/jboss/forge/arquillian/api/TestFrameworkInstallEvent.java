/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.api;


/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class TestFrameworkInstallEvent {
    private final TestFrameworkFacet testFramework;

    public TestFrameworkInstallEvent(TestFrameworkFacet testFramework) {
        this.testFramework = testFramework;
    }

    public TestFrameworkFacet getContainer() {
        return testFramework;
    }
}
