/*
 * Copyright (c) 2000-2015 TeamDev Ltd. All rights reserved.
 * TeamDev PROPRIETARY and CONFIDENTIAL.
 * Use is subject to license terms.
 */

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The sample demonstrates how to create Browser instance, embed it, display and load
 * specified URL.
 */
public class HelloWorld {
    public static void main(String[] args) {
        final Browser browser = new Browser();
        BrowserView browserView = new BrowserView(browser);

        final JTextField addressBar = new JTextField("http://www.teamdev.com/jxbrowser");
        addressBar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browser.loadURL(addressBar.getText());
            }
        });

        JPanel addressPane = new JPanel(new BorderLayout());
        addressPane.add(new JLabel(" URL: "), BorderLayout.WEST);
        addressPane.add(addressBar, BorderLayout.CENTER);

        JFrame frame = new JFrame("JxBrowser - Hello World");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(addressPane, BorderLayout.NORTH);
        frame.add(browserView, BorderLayout.CENTER);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        browser.loadURL(addressBar.getText());
    }
}
