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
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.7.1')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.7.1')
import com.fasterxml.jackson.databind.ObjectMapper
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.7.1')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.7.1')
import com.fasterxml.jackson.databind.ObjectMapper
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.7.1')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.7.1')
import com.fasterxml.jackson.databind.ObjectMapper
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-core', version = '2.7.1')
@Grab(group = 'com.fasterxml.jackson.core', module = 'jackson-databind', version = '2.7.1')
import com.fasterxml.jackson.databind.ObjectMapper
import sun.awt.shell.ShellFolder

import javax.imageio.ImageIO
import javax.swing.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

class Constants {
    final static String TITLE = "mnLauncher";
//    final static int S_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
//    final static int S_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    final static int M_HEIGHT = 26, M_WIDTH = 26;
    final static int M_ICON_HEIGHT = 20, M_ICON_WIDTH = 20;
    final static Color MENU_COLOR = Color.WHITE;
    final static Color MENU_BACKGROUND = Color.DARK_GRAY;
    final static String ICON_URL = "favicon.png";
    final static String MENU_URL = "menu.json";

}

//noinspection GroovyUnusedAssignment
final JFrame frame = initFrame();

private JFrame initFrame() {
    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle(Constants.TITLE);
    frame.setAlwaysOnTop(true);
    frame.setUndecorated(true);
    frame.setVisible(true);
    final BufferedImage logo = ImageIO.read(new File(Constants.ICON_URL));
    //Taskbar Icon
    frame.setIconImage(logo);
    //Background Icon
    final JLabel icon = new JLabel();
    icon.setIcon(new ImageIcon(logo
            .getScaledInstance(Constants.M_WIDTH, Constants.M_HEIGHT, Image.SCALE_SMOOTH)));
    frame.add(icon, BorderLayout.CENTER);
    frame.pack();
    final JPopupMenu menu = initMenu();
    frame.add(menu);
    final MouseAdapter mouseAdapter = initMouseAdapter(menu);
    frame.addMouseMotionListener(mouseAdapter);
    frame.addMouseListener(mouseAdapter);
    return frame;
}

private JPopupMenu initMenu() {
    final JPopupMenu menu = new JPopupMenu();
    //Maybe change L&F Properties instead.
    menu.setBackground(Constants.MENU_BACKGROUND);
    menu.setBorderPainted(false);
    final ObjectMapper om = new ObjectMapper();
    final MenuEntry root = om.readValue(new File(Constants.MENU_URL), MenuEntry.class);
    processMenu(root.getEntries(), menu, null);
    return menu;
}

private void processMenu(Collection<MenuEntry> c, JPopupMenu pm, JMenu menu) {
    for (MenuEntry me : c) {
        //Menu with Children
        if (me.getEntries() != null && !me.getEntries().isEmpty()) {
            final JMenu m = (pm == null ? menu.add(new JMenu()) : pm.add(new JMenu())) as JMenu;
            m.setText(me.getName());
            m.setForeground(Constants.MENU_COLOR);
            m.setBackground(Constants.MENU_BACKGROUND);
            m.getPopupMenu().setBackground(Constants.MENU_BACKGROUND);
            m.getPopupMenu().setBorderPainted(false);
            m.setBorderPainted(false);
            processMenu(me.getEntries(), null, m);
        } //Standard Menu Entry
        else {
            final JMenuItem mi = pm == null ? menu.add(new JMenuItem()) : pm.add(new JMenuItem());
            mi.setText(me.getName());
            mi.setBackground(Constants.MENU_BACKGROUND);
            mi.setBorderPainted(false);
            mi.setForeground(Constants.MENU_COLOR);
            mi.addActionListener(prepareAction(me));
            //Get Icon
            if (me.getFirstCommand().toLowerCase().endsWith("exe")) {
                System.out.println("Loading Icon for: " + me.getFirstCommand());
                final File fCommand = new File(me.getFirstCommand());
                if (fCommand.exists()) {
                    mi.setIcon(new ImageIcon(
                            ShellFolder.getShellFolder(fCommand).getIcon(true)
                                    .getScaledInstance(Constants.M_ICON_WIDTH, Constants.M_ICON_HEIGHT, Image.SCALE_SMOOTH)));
                }
            }
        }
    }
}

@SuppressWarnings("GroovyUnusedAssignment")
private ActionListener prepareAction(MenuEntry me) {
    new AbstractAction() {
        @Override
        void actionPerformed(ActionEvent e) {
            System.out.println("Running Command " + me.getCommand());
            final ProcessBuilder pb;
            if (me.getCommand() instanceof String) {
                pb = new ProcessBuilder(me.getCommand() as String);
            } else {
                pb = new ProcessBuilder(me.getCommand() as List<String>);
            }
            pb.redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectInput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .inheritIO()
                    .start();
        }
    }
}

private MouseAdapter initMouseAdapter(final JPopupMenu menu) {
    return new MouseAdapter() {
        boolean moving = false;
        int startX, startY;

        @Override
        void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (e.getButton() == MouseEvent.BUTTON3) {
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        @Override
        void mousePressed(MouseEvent e) {
            super.mousePressed(e)
            if (e.getButton() == MouseEvent.BUTTON1) {
                startX = e.getX();
                startY = e.getY();
                moving = true;
            }
        }

        @Override
        void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (moving) {
                e.getComponent().setLocation(e.getXOnScreen() - startY, e.getYOnScreen() - startY);
            }
        }


        @Override
        void mouseReleased(MouseEvent e) {
            super.mouseReleased(e)
            if (moving) {
                moving = false;
                e.getComponent().setLocation(e.getXOnScreen() - startY, e.getYOnScreen() - startY);
            }
        }
    };
}

@SuppressWarnings("GroovyUnusedDeclaration")
class MenuEntry {
    private String name;
    private Object command;
    private List<MenuEntry> entries;

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    Object getCommand() {
        return command
    }

    void setCommand(Object command) {
        this.command = command
    }

    String getFirstCommand() {
        return command instanceof List ? ((List) command).iterator().next() : command;
    }

    List<MenuEntry> getEntries() {
        return entries
    }

    void setEntries(List<MenuEntry> entries) {
        this.entries = entries
    }
}