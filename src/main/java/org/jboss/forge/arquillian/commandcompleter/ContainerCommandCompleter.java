package org.jboss.forge.arquillian.commandcompleter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.arquillian.container.Container;
import org.jboss.forge.arquillian.container.ContainerDirectoryParser;
import org.jboss.forge.arquillian.container.ContainerType;
import org.jboss.forge.shell.completer.CommandCompleterState;
import org.jboss.forge.shell.completer.SimpleTokenCompleter;

import static org.jboss.forge.arquillian.ArquillianPlugin.OPTION_CONTAINER_TYPE;

/**
 * @Author Paul Bakker - paul.bakker.nl@gmail.com
 */
public class ContainerCommandCompleter extends SimpleTokenCompleter {

    @Inject
    private ContainerDirectoryParser parser;

    private CommandCompleterState state;

    @Override
    public void complete(CommandCompleterState state) {
        this.state = state;
        super.complete(state);
    }

    @Override
    public Iterable<?> getCompletionTokens() {
        ContainerType containerType = ContainerType.valueOf(getInformedContainerType());
        List<Container> containers = parser.getContainers();
        List<Container> filtered = new ArrayList<Container>();
        for (Container container : containers) {
            if (container.getContainerType() == containerType) {
                filtered.add(container);
            }
        }
        return filtered;
    }

    /**
     * Get the value of containerType command option
     * 
     * @return informed ContainerType as String
     */
    private String getInformedContainerType() {
        String completeCommand = state.getBuffer();
        String[] splitedCommand = completeCommand.split("[\\s]++"); // split by one or more whitespaces
        int cont = 0;
        for (String token : splitedCommand) {
            cont++;
            if (("--" + OPTION_CONTAINER_TYPE).equals(token)) {
                break;
            }
        }
        return splitedCommand[cont];
    }
}
