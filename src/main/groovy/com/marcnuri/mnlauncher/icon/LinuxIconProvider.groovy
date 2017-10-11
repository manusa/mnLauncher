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
	private static final def XDG_DATA_DIRS_HICOLOR_SUFFIX = "/hicolor/$HICOLOR_DEFAULT_ICONS_DIRECTORY"
	private static final def HICOLOR_DEFAULT_ICONS_DIRECTORY = "48x48/apps"
	private static final def GNOME_THEME_INDEX = "index.theme"
	private static final def GNOME_THEME_INDEX_DIRECTORIES = "Directories"
	private static final def HOME_ICONS_DIR = ".icons"
	private static final def PIXMAPS = "/usr/share/pixmaps"
	private static final def SH_SCRIPT_EXTENSION = ".sh"
	private static final def GSETTINGS_COMMAND = "/usr/bin/gsettings"
	private static final def GSETTINGS_COMMAND_ARGS = "get org.gnome.desktop.interface icon-theme"

	private final boolean isLinux
	private final Map<String, File> homeIcons
	private final Map<String, File> gnomeThemeIcons
	private final Map<String, File> hicolorIcons
	private final Map<String, File> pixmapsIcons

	private String gnomeTheme


	LinuxIconProvider() {
		isLinux = System.getProperty(OS_PROPERTY_NAME).toLowerCase().contains("linux")
		homeIcons = new HashMap<>()
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
					homeIcons, gnomeThemeIcons, hicolorIcons, pixmapsIcons)) {
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
		return isLinux
	}

	private initIndexes() {
		// Load Home Icons
		def userHome = System.getProperty("user.home")
		addIconsToMap(homeIcons, new File("$userHome/$HOME_ICONS_DIR"))
		// Detect Gnome Theme (if applies)
		// Load XDG Icons
		if (XDG_DATA_DIRS != null) {
			loadGnomeTheme()
			for(def xdgDir : XDG_DATA_DIRS.split(":")) {
				File xdgDirFile = new File(xdgDir.concat(XDG_DATA_DIRS_ICON_SUFFIX))
				if (xdgDirFile.exists() && xdgDirFile.isDirectory()) {
					// Try to add Gnome theme icons
					def themeIndexF = new File(xdgDirFile,"$gnomeTheme/$GNOME_THEME_INDEX")
					if (themeIndexF.exists()) {
						def themeIndex = new Properties()
						themeIndex.load(new FileInputStream(themeIndexF))
						String directories = themeIndex.get(GNOME_THEME_INDEX_DIRECTORIES)
						directories.split(",").toList().stream().
							filter({ dir -> dir.contains("48") && dir.toLowerCase().contains("apps") }).
							forEach({ dir ->
								addIconsToMap(gnomeThemeIcons,
										new File(xdgDirFile, "$gnomeTheme/$dir"))
							})
					}
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
				.filter({ p -> Files.isRegularFile(p) | Files.isSymbolicLink(p) })
				.filter({ p -> p.toString().endsWith("png")})
				.forEach({ p ->
					// Add file name without extension
					map.put(p.getFileName().toString().replaceFirst(
							"\\..*\$", ""), p.toFile())
			})
		}
	}

}
