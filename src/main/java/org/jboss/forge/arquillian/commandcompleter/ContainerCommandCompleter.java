package org.jboss.forge.arquillian.commandcompleter;

import org.jboss.forge.shell.completer.SimpleTokenCompleter;
import org.jboss.forge.arquillian.container.ContainerDirectoryParser;

import javax.inject.Inject;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerCommandCompleter extends SimpleTokenCompleter{
    @Inject
    ContainerDirectoryParser parser;

    @Override
    public Iterable<?> getCompletionTokens() {

        return parser.getContainers();
    }
}
