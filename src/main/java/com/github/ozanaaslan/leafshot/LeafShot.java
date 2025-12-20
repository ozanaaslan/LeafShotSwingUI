package com.github.ozanaaslan.leafshot;

import com.github.ozanaaslan.leafshot.gui.ScreenshotWindow;
import com.github.ozanaaslan.leafshot.manager.NativeHookManager;
import com.github.ozanaaslan.leafshot.manager.TrayManager;
import com.github.ozanaaslan.leafshot.util.LeafShotConfig;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class LeafShot {

    @Getter private LeafShotConfig leafShotConfig;

    @Getter private static LeafShot leafShot;

    public LeafShot() {
        leafShot = this;
        init();
    }

    private void init(){
        this.leafShotConfig = new LeafShotConfig();

        NativeHookManager nativeHookManager = new NativeHookManager();
        nativeHookManager.register();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            requestMacPermissions();
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        TrayManager trayManager = new TrayManager(nativeHookManager);
        SwingUtilities.invokeLater(trayManager::setup);

        System.out.println("LeafShot is running in background. Press '" + getLeafShotConfig().get("keybinding.screenshot") + "' to capture.");

    }

    private void requestMacPermissions() {
        try {
            String[] script = {
                    "osascript",
                    "-e",
                    "tell application \"System Events\" to set isProcessTrusted to UI elements enabled"
            };
            Runtime.getRuntime().exec(script);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Robot robot = new Robot();
            robot.createScreenCapture(new Rectangle(0, 0, 1, 1));
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        new LeafShot();
    }
}
