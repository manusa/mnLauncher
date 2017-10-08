package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry
import groovy.util.logging.Log
import sun.awt.shell.ShellFolder

import javax.swing.ImageIcon
import java.awt.Image

@Log
class WindowsIconProvider implements IconProvider {


	@Override
	ImageIcon getIcon(MenuEntry menuEntry) {
		ImageIcon ret = null
		log.info("Loading icon for: $menuEntry.getName()")
		final File fCommand = new File(menuEntry.getFirstCommand())
		if (fCommand.exists()) {
			final Image icon = ShellFolder.getShellFolder(fCommand).getIcon(true)
			ret = icon != null ? new ImageIcon(icon.
					getScaledInstance(M_ICON_WIDTH, M_ICON_HEIGHT, Image.SCALE_SMOOTH))
					: null
		}
		return ret
	}

	@Override
	boolean applies(MenuEntry menuEntry) {
		return System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("win") && menuEntry.getFirstCommand().toLowerCase().endsWith("exe")
	}

}
