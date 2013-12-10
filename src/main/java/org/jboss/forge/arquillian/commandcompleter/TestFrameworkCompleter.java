/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.forge.arquillian.commandcompleter;

import org.jboss.forge.arquillian.testframework.ProvidesFacetFor;
import org.jboss.forge.arquillian.testframework.TestFrameworkFacetInstaller;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestFrameworkCompleter extends SimpleTokenCompleter
{
   public static final String OPTION_TEST_FRAMEWORK = "testFramework";

   @Inject @Any
   private Instance<TestFrameworkFacetInstaller> testFrameworkFacetInstallers;

   @Override
   public Iterable<?> getCompletionTokens()
   {
      final List<String> testFrameworks = new ArrayList<String>();
      final Iterator<TestFrameworkFacetInstaller> testFrameworkProviderIterator = testFrameworkFacetInstallers.iterator();

      while (testFrameworkProviderIterator.hasNext())
      {
         testFrameworks.add(testFrameworkProviderIterator.next().getClass().getAnnotation(ProvidesFacetFor.class).value());
      }

      return testFrameworks;
   }
}
