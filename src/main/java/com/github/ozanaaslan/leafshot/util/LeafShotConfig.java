package com.github.ozanaaslan.leafshot.util;

import com.github.ozanaaslan.leafshot.gui.ConfigUI;
import lombok.Getter;

public class LeafShotConfig extends Config{
    @Getter private String remoteHost;

    public LeafShotConfig() {
        super("leafshot");
        init();
    }
    private void init(){
        if(!existing("setup")){
            set("remote", "127.0.0.1");
            set("setup", "true");
        }
        this.remoteHost = (String) get("remote", "127.0.0.1");
    }

    public void showGui(){
        new ConfigUI(this).setVisible(true);
    }
}
