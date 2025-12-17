package com.github.ozanaaslan.leafshot;

import com.github.ozanaaslan.leafshot.gui.ScreenshotWindow;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeafShot implements NativeKeyListener {

    public static void main(String[] args) {
        try {
            // Disable JNativeHook logging to keep console clean
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        GlobalScreen.addNativeKeyListener(new LeafShot());
        SwingUtilities.invokeLater(LeafShot::setupSystemTray);
        System.out.println("LeafShot is running in background. Press 'Print Screen' to capture.");
    }

    private static void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        PopupMenu popup = new PopupMenu();
        MenuItem captureItem = new MenuItem("Take Screenshot");
        MenuItem exitItem = new MenuItem("Exit");

        captureItem.addActionListener(e -> triggerScreenshot());
        exitItem.addActionListener(e -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });

        popup.add(captureItem);
        popup.addSeparator();
        popup.add(exitItem);

        // Load icon from resources. Ensure you have an icon.png in src/main/resources
        URL imageURL = LeafShot.class.getResource("/LeafShotTrayMin.png");
        Image iconImage;
        if (imageURL != null) {
            iconImage = new ImageIcon(imageURL).getImage();
        } else {
            // Fallback if no icon is found: draw a simple red square
            iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics g = iconImage.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
        }

        TrayIcon trayIcon = new TrayIcon(iconImage, "LeafShot", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private static void triggerScreenshot() {
        SwingUtilities.invokeLater(() -> {
            ScreenshotWindow window = new ScreenshotWindow();
            window.setVisible(true);
            window.setAlwaysOnTop(true);
        });
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // NativeKeyEvent.VC_PRINTSCREEN is the constant for the Print Screen button
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
            SwingUtilities.invokeLater(() -> {
                ScreenshotWindow window = new ScreenshotWindow();
                window.setVisible(true);
                window.setAlwaysOnTop(true);
            });
        }
    }
}
