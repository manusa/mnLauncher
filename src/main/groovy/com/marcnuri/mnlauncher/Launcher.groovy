/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 *  Created by Marc Nuri on 2016-02-21.
 */
package com.marcnuri.mnlauncher

import com.fasterxml.jackson.databind.ObjectMapper
import com.marcnuri.mnlauncher.icon.IconProvider
import com.marcnuri.mnlauncher.icon.LinuxIconProvider
import com.marcnuri.mnlauncher.icon.WindowsIconProvider
import groovy.util.logging.Log

import javax.imageio.ImageIO
import javax.swing.*
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.util.List

@Log
class Launcher {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Constants
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	final static String TITLE = "mnLauncher"
//    final static int S_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width
//    final static int S_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height
	final static int M_HEIGHT = 26, M_WIDTH = 26
	final static Color MENU_COLOR = Color.WHITE
	final static Color MENU_BACKGROUND = Color.DARK_GRAY
	final static String ICON_FILE_NAME = "favicon.png"
	final static String ICON_RESOURCE_URL = "/$ICON_FILE_NAME"
	final static int M_ICON_HEIGHT = 18, M_ICON_WIDTH = 18
	final static String MENU_URL = "menu.json"
	final static String GROOVY_EXTENSION = ".groovy"
	final static List<IconProvider> ICON_PROVIDERS = Arrays.asList(new WindowsIconProvider(), new LinuxIconProvider())

	static void main(String[] args) {
		new Launcher()
	}

	Launcher() {
//noinspection GroovyUnusedAssignment
		final JFrame frame = initFrame()
	}

	JFrame initFrame() {
		final JFrame frame = new JFrame()
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
		frame.setTitle(TITLE)
		frame.setAlwaysOnTop(true)
		frame.setUndecorated(true)
		frame.setVisible(true)
		final File faviconFile = new File(ICON_FILE_NAME)
		final BufferedImage logo = ImageIO.read(
				faviconFile.exists() ?
				faviconFile.toURI().toURL() :
				Launcher.class.getResource(ICON_RESOURCE_URL))
		//Taskbar Icon
		frame.setIconImage(logo)
		log.info("Loaded frame")
		//Background Icon
		final JLabel icon = new JLabel()
		icon.setIcon(new ImageIcon(logo
				.getScaledInstance(M_WIDTH, M_HEIGHT, Image.SCALE_SMOOTH)))
		frame.add(icon, BorderLayout.CENTER)
		frame.pack()
		//Add popup menu
		final JPopupMenu menu = initMenu()
		frame.add(menu)
		//Mouse Events
		final MouseAdapter mouseAdapter = initMouseAdapter(menu)
		frame.addMouseMotionListener(mouseAdapter)
		frame.addMouseListener(mouseAdapter)
		//Windows event
		frame.addWindowListener(new WindowAdapter() {
			@Override
			void windowLostFocus(WindowEvent e) {
				//AlwaysOnTop property can only be assigned to a window.
				//We reset this state so that if any other window gathers the property we recover it.
				e.getWindow().setAlwaysOnTop(false)
				e.getWindow().setAlwaysOnTop(true)
			}
		})
		return frame
	}

	JPopupMenu initMenu() {
		final JPopupMenu menu = new JPopupMenu()
		//Maybe change L&F Properties instead.
		menu.setBackground(MENU_BACKGROUND)
		menu.setBorderPainted(false)
		final ObjectMapper om = new ObjectMapper()
		final MenuEntry root = om.readValue(new File(MENU_URL), MenuEntry.class)
		log.info("Loaded menus")
		processMenu(root.getEntries(), menu, null)
		return menu
	}

	void processMenu(Collection<MenuEntry> c, JPopupMenu pm, JMenu menu) {
		for (MenuEntry me : c) {
			//Menu with Children
			if (me.getEntries() != null && !me.getEntries().isEmpty()) {
				final JMenu m = (pm == null ? menu.add(new JMenu()) : pm.add(new JMenu())) as JMenu
				m.setText(me.getName())
				m.setForeground(MENU_COLOR)
				m.setBackground(MENU_BACKGROUND)
				m.getPopupMenu().setBackground(MENU_BACKGROUND)
				m.getPopupMenu().setBorderPainted(false)
				m.setBorderPainted(false)
				processMenu(me.getEntries(), null, m)
			} //Standard Menu Entry
			else {
				final JMenuItem mi = pm == null ? menu.add(new JMenuItem()) : pm.add(new JMenuItem())
				mi.setText(me.getName())
				mi.setBackground(MENU_BACKGROUND)
				mi.setBorderPainted(false)
				mi.setForeground(MENU_COLOR)
				mi.addActionListener(new LauncherActionListener(me))
				// Set icon
				IconProvider iconProvider =ICON_PROVIDERS.stream()
						.filter {ip -> ip.applies(me)}
						.findFirst().orElse(null)
				if (iconProvider != null) {
					final Image icon = iconProvider.getIcon(me)
					mi.setIcon(icon != null ? new ImageIcon(
							icon.getScaledInstance(M_ICON_WIDTH, M_ICON_HEIGHT, Image.SCALE_SMOOTH)) :
							null)
					if (icon!= null) icon.flush()
				}
			}
			log.info("Loaded menu " + me.getName())
		}
	}


	MouseAdapter initMouseAdapter(final JPopupMenu menu) {
		return new MouseAdapter() {
			boolean moving = false
			int startX, startY

			@Override
			void mouseClicked(MouseEvent e) {
				super.mouseClicked(e)
				if (e.getButton() == MouseEvent.BUTTON3) {
					menu.show(e.getComponent(), e.getX(), e.getY())
				}
			}

			@Override
			void mousePressed(MouseEvent e) {
				super.mousePressed(e)
				if (e.getButton() == MouseEvent.BUTTON1) {
					startX = e.getX()
					startY = e.getY()
					moving = true
				}
			}

			@Override
			void mouseDragged(MouseEvent e) {
				super.mouseDragged(e)
				if (moving) {
					e.getComponent().setLocation(e.getXOnScreen() - startY, e.getYOnScreen() - startY)
				}
			}


			@Override
			void mouseReleased(MouseEvent e) {
				super.mouseReleased(e)
				if (moving) {
					moving = false
					e.getComponent().setLocation(e.getXOnScreen() - startY, e.getYOnScreen() - startY)
				}
			}
		}
	}

}
