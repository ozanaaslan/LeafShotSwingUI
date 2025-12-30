package com.github.ozanaaslan.leafshot.manager;

import com.github.ozanaaslan.leafshot.LeafShot;
import com.github.ozanaaslan.leafshot.util.manager.event.BeforeTrayFinalize;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class TrayManager {
    private final NativeHookManager nativeHookManager;

    public TrayManager(NativeHookManager nativeHookManager) {
        this.nativeHookManager = nativeHookManager;
    }

    public void setup() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        PopupMenu popup = new PopupMenu();
        MenuItem captureItem = new MenuItem("Take Screenshot");
        MenuItem configItem = new MenuItem("Settings");
        MenuItem exitItem = new MenuItem("Exit");

        captureItem.addActionListener(e -> nativeHookManager.triggerScreenshot());
        exitItem.addActionListener(e -> {
            nativeHookManager.unregister();
            System.exit(0);
        });
        configItem.addActionListener(e -> LeafShot.getLeafShot().getLeafShotConfig().showGui());

        popup.add(captureItem);
        popup.addSeparator();
        popup.add(configItem);
        popup.addSeparator();
        popup.add(exitItem);

        LeafShot.getLeafShot().getEventManager().dispatch(new BeforeTrayFinalize(SystemTray.getSystemTray(), popup));

        Image iconImage = loadIcon();
        TrayIcon trayIcon = new TrayIcon(iconImage, "LeafShot", popup);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private Image loadIcon() {
        URL imageURL = LeafShot.class.getResource("/LeafShotTrayMin.png");
        if (imageURL != null) {
            return new ImageIcon(imageURL).getImage();
        } else {
            // Fallback if no icon is found: draw a simple red square
            BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics g = fallback.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 16, 16);
            g.dispose();
            return fallback;
        }
    }
}
