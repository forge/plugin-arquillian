/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package test.integration.util;

import org.jboss.forge.Root;
import org.jboss.forge.arquillian.ArquillianPlugin;
import org.jboss.seam.render.RenderRoot;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.solder.SolderRoot;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class Deployments
{

   public static JavaArchive basicPluginInfrastructure()
   {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
                       .addPackages(true, Root.class.getPackage())
                       .addPackages(true, RenderRoot.class.getPackage())
                       .addPackages(true, SolderRoot.class.getPackage())
                       .addPackages(true, ArquillianPlugin.class.getPackage())
                       .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
   }
}
