package com.marcnuri.mnlauncher.icon

import com.marcnuri.mnlauncher.MenuEntry

import java.awt.*

interface IconProvider {

	final static String OS_PROPERTY_NAME = "os.name"

	Image getIcon(MenuEntry menuEntry)

	boolean applies(MenuEntry menuEntry)

}