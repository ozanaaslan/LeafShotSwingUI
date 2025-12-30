package com.github.ozanaaslan.leafshot.util;

import com.github.ozanaaslan.leafshot.LeafShot;
import com.github.ozanaaslan.leafshot.gui.ConfigUI;
import com.github.ozanaaslan.leafshot.util.manager.event.OnAppStartupArgs;
import lombok.Getter;

public class LeafShotConfig extends Config{
    @Getter private String remoteHost;

    public LeafShotConfig() {
        super("leafshot");
        init();
    }
    private void init(){
        if(!existing("setup") || !(Boolean.parseBoolean((String) get("setup")))){
            set("keybinding.screenshot", "F12");
            set("keybinding.copy", "CTRL+C");
            set("keybinding.upload", "CTRL+U");
            set("keybinding.save", "CTRL+S");
            set("keybinding.settings", "CTRL+ALT+S");
            set("keybinding.open.save.destination", "CTRL+ALT+O");
            set("remote", "http\\://127.0.0.1\\:8091");
            set("save.destination", System.getProperty("user.home") + "/leafshot/screenshots");
            set("data.destination", System.getProperty("user.home") + "/leafshot/");
            set("setup", "true");
        }


        this.remoteHost = (String) get("remote", "127.0.0.1");
    }

    public void applyArgOverrides(String[] args) {
        if (args == null) return;
        LeafShot.getLeafShot().getEventManager().dispatch(new OnAppStartupArgs(args));
        for (String arg : args) {
            if (arg.startsWith("-") && arg.contains("=")) {
                String[] parts = arg.substring(1).split("=", 2);
                String key = parts[0];
                String value = parts[1];
                set(key, value);
            }
        }
    }


    public void showGui(){
        new ConfigUI(this).setVisible(true);
    }
}
