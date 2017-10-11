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

	private static final def PROCESS_TIMEOUT = 3000L

	private static final def XDG_DATA_DIRS = System.getenv("XDG_DATA_DIRS")
	private static final def XDG_DATA_DIRS_ICON_SUFFIX = "/icons"
	private static final def DEFAULT_THEME_ICONS_DIRECTOERY = "48x48/apps"
	private static final def XDG_DATA_DIRS_HICOLOR_SUFFIX = "/hicolor/$DEFAULT_THEME_ICONS_DIRECTOERY"
	private static final def PIXMAPS = "/usr/share/pixmaps"
	private static final def SH_SCRIPT_EXTENSION = ".sh"
	private static final def GSETTINGS_COMMAND = "/usr/bin/gsettings"
	private static final def GSETTINGS_COMMAND_ARGS = "get org.gnome.desktop.interface icon-theme"

	private final boolean isLinux
	private final Map<String, File> gnomeThemeIcons
	private final Map<String, File> hicolorIcons
	private final Map<String, File> pixmapsIcons

	private String gnomeTheme


	LinuxIconProvider() {
		isLinux = System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("linux")
		gnomeThemeIcons = new HashMap<>()
		hicolorIcons = new HashMap<>()
		pixmapsIcons = new HashMap<>()
		initIndexes()
	}

	@Override
	Image getIcon(MenuEntry menuEntry) {
		Image ret = null
		File command = new File(menuEntry.getFirstCommand())
		if (command.exists() && command.isFile()) {
			for(Map<String, File> fileMap : Arrays.asList(
					// Search in order according to specification
					gnomeThemeIcons, hicolorIcons, pixmapsIcons)) {
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
		//Detect Gnome Theme (if applies)
		loadGnomeTheme()
		// Load XDG Icons
		if (XDG_DATA_DIRS != null) {
			for(def xdgDir : XDG_DATA_DIRS.split(":")) {
				File xdgDirFile = new File(xdgDir.concat(XDG_DATA_DIRS_ICON_SUFFIX))
				if (xdgDirFile.exists() && xdgDirFile.isDirectory()) {
					// Try to add Gnome theme icons
					addIconsToMap(gnomeThemeIcons,
							new File(xdgDirFile, "$gnomeTheme/$DEFAULT_THEME_ICONS_DIRECTOERY"))
					// Try to add Hicolor icons
					addIconsToMap(hicolorIcons, new File(xdgDirFile, XDG_DATA_DIRS_HICOLOR_SUFFIX))
				}
			}
		}
		// Load pixmaps Icons
		addIconsToMap(pixmapsIcons, new File(PIXMAPS))
	}

	private loadGnomeTheme() {
		if (new File(GSETTINGS_COMMAND).canExecute()) {
			def out = new StringBuilder()
			def err = new StringBuilder()
			def process = [GSETTINGS_COMMAND, *GSETTINGS_COMMAND_ARGS.split(" ")].execute()
			process.consumeProcessOutput(out, err)
			process.waitForOrKill(PROCESS_TIMEOUT)
			gnomeTheme = out.toString().
					replace('\'', '').
					replace('\n', '')
		}
	}

	private static final Image locateIcon(Map<String, File> map, String name) {
		Image ret = null
		if (map.containsKey(name)) {
			ret = ImageIO.read(map.get(name))
		}
		return ret
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
