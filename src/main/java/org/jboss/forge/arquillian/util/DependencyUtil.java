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
package org.jboss.forge.arquillian.util;

import org.jboss.forge.addon.dependencies.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * DependencyUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class DependencyUtil {
    private DependencyUtil() {
    }

    public static List<String> toVersionString(List<Coordinate> dependencies) {
        List<String> versions = new ArrayList<>();
        for (Coordinate cor : dependencies) {
            versions.add(cor.getVersion());
        }
        return versions;
    }

    public static String getLatestNonSnapshotVersionCoordinate(List<Coordinate> dependencies) {
        return getLatestNonSnapshotVersion(toVersionString(dependencies));
    }

    public static String getLatestNonSnapshotVersion(List<String> dependencies) {
        if (dependencies == null) {
            return null;
        }
        for (int i = dependencies.size() - 1; i >= 0; i--) {
            String dep = dependencies.get(i);
            if (!dep.endsWith("SNAPSHOT")) {
                return dep;
            }
        }
        return dependencies.get(dependencies.size() - 1);
    }
}
