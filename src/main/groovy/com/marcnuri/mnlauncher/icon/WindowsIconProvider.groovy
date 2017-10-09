package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry
import groovy.util.logging.Log
import sun.awt.shell.ShellFolder

import java.awt.*

@Log
class WindowsIconProvider implements IconProvider {

	private final boolean isWindows

	WindowsIconProvider() {
		isWindows = System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("win")
	}

	@Override
	Image getIcon(MenuEntry menuEntry) {
		Image ret = null
		log.info("Loading icon for: $menuEntry.getName()")
		final File fCommand = new File(menuEntry.getFirstCommand())
		if (fCommand.exists()) {
			ret = ShellFolder.getShellFolder(fCommand).getIcon(true)
		}
		return ret
	}

	@Override
	boolean applies(MenuEntry menuEntry) {
		return isWindows && menuEntry.getFirstCommand().toLowerCase().endsWith("exe")
	}

}
