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

import com.fasterxml.jackson.core.type.TypeReference
@Grab(group='com.fasterxml.jackson.core', module='jackson-core', version='2.7.1')
@Grab(group='com.fasterxml.jackson.core', module='jackson-databind', version='2.7.1')
import com.fasterxml.jackson.databind.ObjectMapper

import javax.imageio.ImageIO
import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class Constants {
    final static String TITLE = "MNLauncher";
    final static int S_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    final static int S_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    final static int M_HEIGHT = 25, M_WIDTH = 25;
    final static String ICON_URL = "favicon.png";
    final static String MENU_URL = "menu.json";

}


final JFrame frame = initFrame();

private JFrame initFrame() {
    final JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setTitle(Constants.TITLE);
    frame.setAlwaysOnTop(true);
    frame.setUndecorated(true);
    frame.setVisible(true);
    final JLabel icon = new JLabel();
    icon.setIcon(new ImageIcon(ImageIO.read(new File(Constants.ICON_URL))
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
    final ObjectMapper om = new ObjectMapper();
    final java.util.List<MenuEntry> entries = om.readValue(new File(Constants.MENU_URL), new TypeReference<java.util.List<MenuEntry>>(){});
    processMenu(entries, menu, null);
    return menu;
}

private void processMenu(Collection<MenuEntry> c, JPopupMenu pm,JMenu menu){
    for(MenuEntry me : c){
        //Menu with Children
        if(me.getEntries() != null && !me.getEntries().isEmpty()){
            final JMenu m = pm == null ? menu.add(new JMenu()) : pm.add(new JMenu());
            m.setText(me.getName());
            processMenu(me.getEntries(), null, m);
        } //Standard Menu Entry
        else {
            final JMenuItem mi = pm == null ? menu.add(new JMenuItem()) : pm.add(new JMenuItem());
            mi.setText(me.getName());
            mi.addActionListener(prepareAction(me));
        }
    }
}
private ActionListener prepareAction(MenuEntry me){
    new AbstractAction() {
        @Override
        void actionPerformed(ActionEvent e) {
            final Process p = new ProcessBuilder(me.getCommand())
                    .redirectErrorStream(true)
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

class MenuEntry {
    private String name;
    private String command;
    private java.util.List<MenuEntry> entries;

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getCommand() {
        return command
    }

    void setCommand(String command) {
        this.command = command
    }

    java.util.List<MenuEntry> getEntries() {
        return entries
    }

    void setEntries(java.util.List<MenuEntry> entries) {
        this.entries = entries
    }
}