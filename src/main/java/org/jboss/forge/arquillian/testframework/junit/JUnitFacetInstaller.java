/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework.junit;

import org.jboss.forge.arquillian.testframework.ProvidesFacetFor;
import org.jboss.forge.arquillian.testframework.TestFrameworkFacetInstaller;
import org.jboss.forge.project.facets.events.InstallFacets;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
@ProvidesFacetFor("junit")
public class JUnitFacetInstaller implements TestFrameworkFacetInstaller
{
   @Inject
   private Event<InstallFacets> installFacetsEvent;

   @Override
   public void install()
   {
      installFacetsEvent.fire(new InstallFacets(JUnitFacet.class));
   }
}
