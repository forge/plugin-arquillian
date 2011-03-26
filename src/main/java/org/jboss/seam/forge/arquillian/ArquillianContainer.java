package org.jboss.seam.forge.arquillian;

import org.jboss.seam.forge.arquillian.container.*;

public enum ArquillianContainer {
    JBOSS_AS_5_REMOTE(Jboss5Remote.class),
    JBOSS_AS_5__1_REMOTE(Jboss5_1Remote.class),
    JBOSS_AS_5__1_MANAGED(Jboss5_1Managed.class),
    JBOSS_AS_6_REMOTE(Jboss6Remote.class),
    GLASSFISH_3_1_EMBEDDED(GlassFish3Embedded.class),
    JBOSS_AS_6_MANAGED(Jboss6Managed.class);

    public Class<? extends Container> getContainer() {
        return container;
    }

    private Class<? extends Container> container;

    private ArquillianContainer(Class<? extends Container> container) {
        this.container = container;
    }
}
