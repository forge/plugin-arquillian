/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.forge.arquillian;

import junit.framework.Assert;
import org.jboss.forge.project.dependencies.Dependency;
import org.jboss.forge.project.dependencies.DependencyBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * DependencyUtilTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class DependencyUtilTest
{

   @Test
   public void shouldGetLastNonSnapshotVersion()
   {
      List<Dependency> deps = new ArrayList<Dependency>();
      deps.add(DependencyBuilder.create().setVersion("1.0"));
      deps.add(DependencyBuilder.create().setVersion("1.0-SNAPSHOT"));

      Dependency dep = DependencyUtil.getLatestNonSnapshotVersion(deps);

      Assert.assertEquals("1.0", dep.getVersion());
   }

   @Test
   public void shouldReturnLatestIfAllSnapshots()
   {
      List<Dependency> deps = new ArrayList<Dependency>();
      deps.add(DependencyBuilder.create().setVersion("1.0-SNAPSHOT"));
      deps.add(DependencyBuilder.create().setVersion("2.0-SNAPSHOT"));

      Dependency dep = DependencyUtil.getLatestNonSnapshotVersion(deps);

      Assert.assertEquals("2.0-SNAPSHOT", dep.getVersion());
   }
}
