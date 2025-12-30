package com.github.ozanaaslan.leafshot;

import com.github.ozanaaslan.leafshot.manager.NativeHookManager;
import com.github.ozanaaslan.leafshot.manager.TrayManager;
import com.github.ozanaaslan.leafshot.util.LeafShotConfig;
import com.github.ozanaaslan.leafshot.util.http.DefaultUploadHandler;
import com.github.ozanaaslan.leafshot.util.http.IUploadHandler;
import com.github.ozanaaslan.leafshot.util.manager.EventManager;
import com.github.ozanaaslan.leafshot.util.manager.ModuleManager;
import com.github.ozanaaslan.leafshot.util.manager.event.AfterTrayFinalize;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LeafShot {

    @Getter private LeafShotConfig leafShotConfig;
    @Getter private File saveDestination;
    @Getter private static LeafShot leafShot;

    @Getter private ModuleManager moduleManager;
    @Getter private EventManager eventManager;
    @Getter private TrayManager trayManager;

    @Getter private IUploadHandler uploadHandler;

    public LeafShot(String[] args) {
        leafShot = this;
        init(args);
    }

    private void init(String[] args){
        this.leafShotConfig = new LeafShotConfig();
        this.eventManager = new EventManager();
        this.moduleManager = new ModuleManager(new File(this.leafShotConfig.getFile().getParentFile(), "modules"));
        this.moduleManager.loadModules();

        this.moduleManager.invokePrimaries();

        this.leafShotConfig.applyArgOverrides(args);


        this.saveDestination = new File((String)leafShotConfig.get("save.destination", System.getProperty("user.home") + "/leafshot/"));
        if(!saveDestination.exists())
            saveDestination.mkdir();

        this.uploadHandler = new DefaultUploadHandler(getLeafShotConfig().getRemoteHost());

        this.moduleManager.invokeSecondaries();

        NativeHookManager nativeHookManager = new NativeHookManager();
        nativeHookManager.register();

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            requestMacPermissions();
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }


        this.trayManager = new TrayManager(nativeHookManager);
        getEventManager().dispatch(new AfterTrayFinalize(this.trayManager));
        SwingUtilities.invokeLater(this.trayManager::setup);


        System.out.println("LeafShot is running in background. Press '" + getLeafShotConfig().get("keybinding.screenshot") + "' to capture.");

        this.moduleManager.invokeTertiaries();
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
