package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry

import javax.imageio.ImageIO
import java.awt.*
import java.nio.file.Files

/**
 * https://standards.freedesktop.org/icon-theme-spec/icon-theme-spec-latest.html
 *
 */
class LinuxIconProvider implements IconProvider {

	private static final String XDG_DATA_DIRS = System.getenv("XDG_DATA_DIRS")
	private static final String XDG_DATA_DIRS_ICON_SUFFIX = "/icons"
	private static final String XDG_DATA_DIRS_HICOLOR_SUFFIX = "/hicolor/48x48/apps"
	private static final String PIXMAPS = "/usr/share/pixmaps"
	private static final String SH_SCRIPT_EXTENSION = ".sh"

	private final boolean isLinux
	private final Map<String, File> hicolorIcons
	private final Map<String, File> pixmapsIcons


	LinuxIconProvider() {
		isLinux = System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("linux")
		hicolorIcons = new HashMap<>()
		pixmapsIcons = new HashMap<>()
		initIndexes()
	}

	@Override
	Image getIcon(MenuEntry menuEntry) {
		Image ret = null
		File command = new File(menuEntry.getFirstCommand())
		if (command.exists() && command.isFile()) {
			for(Map<String, File> fileMap : Arrays.asList(hicolorIcons, pixmapsIcons)) {
				final String cName = command.getName()
				ret = locateIcon(fileMap, cName)
				// Locate sh script icon by name
				if (ret == null && cName.toLowerCase().endsWith(SH_SCRIPT_EXTENSION)) {
					ret = locateIcon(fileMap,
							cName.substring(0, cName.length() - SH_SCRIPT_EXTENSION.length()))
				}
				if (ret != null) break
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
				File xdgDirFile = new File(xdgDir.concat(XDG_DATA_DIRS_ICON_SUFFIX))
				if (xdgDirFile.exists() && xdgDirFile.isDirectory()) {
					addIconsToMap(hicolorIcons, new File(xdgDirFile, XDG_DATA_DIRS_HICOLOR_SUFFIX))
				}
			}
		}
		// Load pixmaps Icons
		addIconsToMap(pixmapsIcons, new File(PIXMAPS))
	}

	private static final Image locateIcon(Map<String, File> map, String name) {
		Image ret = null;
		if (map.containsKey(name)) {
			ret = ImageIO.read(map.get(name))
		}
		return ret;
	}

	private static final addIconsToMap(Map<String, File> map, File iconDir) {
		if(iconDir.exists() && iconDir.isDirectory()) {
			Files.list(iconDir.toPath())
				.filter({ p -> Files.isRegularFile(p) })
				.forEach({ p ->
					// Add file name without extension
					map.put(p.getFileName().toString().replaceFirst(
							"\\..*\$", ""), p.toFile())
			})
		}
	}

}
