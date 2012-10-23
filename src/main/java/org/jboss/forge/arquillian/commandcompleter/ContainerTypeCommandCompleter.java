package org.jboss.forge.arquillian.commandcompleter;

import java.util.Arrays;

import javax.inject.Singleton;

import org.jboss.forge.arquillian.container.ContainerType;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
@Singleton
public class ContainerTypeCommandCompleter extends SimpleTokenCompleter{

    @Override
    public Iterable<?> getCompletionTokens() {
        return Arrays.asList(ContainerType.values());
    }
}
