/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.testframework;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author <a href="mailto:bartosz.majsak@gmail.com">Bartosz Majsak</a>
 */
public class ProvidesFacetForQualifier extends AnnotationLiteral<ProvidesFacetFor> implements ProvidesFacetFor
{
   private final String value;

   public ProvidesFacetForQualifier(String value)
   {
      this.value = value.toLowerCase();
   }

   @Override
   public String value()
   {
      return value;
   }
}
