package org.jboss.seam.forge.arquillian;

import org.jboss.seam.forge.project.Project;
import org.jboss.seam.forge.project.facets.MavenCoreFacet;
import org.jboss.seam.forge.shell.ShellColor;
import org.jboss.seam.forge.shell.plugins.Alias;
import org.jboss.seam.forge.shell.plugins.Command;
import org.jboss.seam.forge.shell.plugins.PipeOut;
import org.jboss.seam.forge.shell.plugins.Plugin;

import javax.inject.Inject;

@Alias("arquillian")
public class ArquillianPlugin implements Plugin {
    @Inject private Project project;

    @Command("setup")
    public void setup(final PipeOut out) {
       out.print(ShellColor.RED, "***HALLO!***");
    }

}
