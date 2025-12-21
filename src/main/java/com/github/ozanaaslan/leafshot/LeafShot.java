package com.github.ozanaaslan.leafshot;

import com.github.ozanaaslan.leafshot.manager.NativeHookManager;
import com.github.ozanaaslan.leafshot.manager.TrayManager;
import com.github.ozanaaslan.leafshot.util.LeafShotConfig;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LeafShot {

    @Getter private LeafShotConfig leafShotConfig;
    @Getter private File saveDestination;
    @Getter private static LeafShot leafShot;

    public LeafShot(String[] args) {
        leafShot = this;
        init(args);
    }

    private void init(String[] args){
        this.leafShotConfig = new LeafShotConfig();
        this.leafShotConfig.applyArgOverrides(args);

        this.saveDestination = new File((String)leafShotConfig.get("save.destination", System.getProperty("user.home") + "/leafshot/"));
        if(!saveDestination.exists())
            saveDestination.mkdir();

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
        new LeafShot(args);
    }
}
