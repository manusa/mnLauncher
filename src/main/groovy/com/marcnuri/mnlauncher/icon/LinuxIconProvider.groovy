package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry

import javax.imageio.ImageIO
import java.awt.Image
import java.nio.file.Files

class LinuxIconProvider implements IconProvider {

	private static final String XDG_DATA_DIRS = System.getenv("XDG_DATA_DIRS")
	private static final String XDG_DATA_DIRS_ICON_SUFFIX = "/icons"
	private static final String PIXMAPS = "/usr/share/pixmaps"

	private final boolean isLinux
	private final Map<String, File> pixmapsIcons

	LinuxIconProvider() {
		isLinux = System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("linux")
		pixmapsIcons = new HashMap<>()
		initIndexes()
	}

	@Override
	Image getIcon(MenuEntry menuEntry) {
		Image ret = null
		File command = new File(menuEntry.getFirstCommand())
		if (command.exists()) {
			if (pixmapsIcons.containsKey(command.getName())) {
				ret = ImageIO.read(pixmapsIcons.get(command.getName()))
			}
		}
		return ret
	}

	@Override
	boolean applies(MenuEntry menuEntry) {
		return isLinux  && menuEntry.getFirstCommand().startsWith("/usr/bin/")
	}

	private initIndexes() {
		// Load XDG Icons
		if (XDG_DATA_DIRS != null) {
			for(String xdgDir : XDG_DATA_DIRS.split(":")) {
				File xdgDirFile = new File(XDG_DATA_DIRS.concat(XDG_DATA_DIRS_ICON_SUFFIX))
				if (xdgDirFile.exists() && xdgDirFile.isDirectory()) {

				}
			}
		}
		// Load pixmaps Icons
		File pixmaps = new File(PIXMAPS)
		if (pixmaps.exists() && pixmaps.isDirectory()) {
			Files.list(pixmaps.toPath())
				.filter({p -> Files.isRegularFile(p)})
				.forEach({ p -> pixmapsIcons.put(
					p.getFileName().toString().replaceFirst("\\..*\$", ""), p.toFile())
				})
		}
	}

}
