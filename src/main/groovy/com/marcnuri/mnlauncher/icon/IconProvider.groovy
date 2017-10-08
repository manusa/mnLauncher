package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry

import javax.swing.ImageIcon

interface IconProvider {

	final static String OS_PROPERTY_NAME = "os.name"
	final static int M_ICON_HEIGHT = 18, M_ICON_WIDTH = 18

	ImageIcon getIcon(MenuEntry menuEntry)

	boolean applies(MenuEntry menuEntry)

}