package com.marcnuri.mnlauncher

import groovy.util.logging.Log

import javax.swing.AbstractAction
import java.awt.event.ActionEvent

/**
 *
 * Created by Marc Nuri <marc@marcnuri.com> on 2017-10-07.
 */
@Log
class LauncherActionListener extends AbstractAction {
	final List<String> command

	LauncherActionListener(MenuEntry menuEntry) {
		this.command = menuEntry.getCommand()
	}

	@Override
	void actionPerformed(ActionEvent e) {
		log.info("Running " + command)
		//Command is a groovy script
		if ((++command.iterator()).toLowerCase().endsWith(Launcher.GROOVY_EXTENSION)) {
			final File gsFile = new File(++command.iterator())
			final List<String> args = command.size() > 1 ?
					command.subList(1, command.size()) : Collections.<String> emptyList()
			new GroovyShell().run(gsFile, args)
		}
		//Command is a process
		else {
			final ProcessBuilder pb = new ProcessBuilder(command)
			pb.redirectErrorStream(true)
					.redirectOutput(ProcessBuilder.Redirect.INHERIT)
					.redirectInput(ProcessBuilder.Redirect.INHERIT)
					.redirectError(ProcessBuilder.Redirect.INHERIT)
					.inheritIO()
					.start()
		}
		log.info("Started " + command)
	}
}