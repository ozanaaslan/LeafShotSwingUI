package com.github.ozanaaslan.leafshot.util.manager.event;

import com.github.ozanaaslan.leafshot.util.manager.EventManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@AllArgsConstructor
@Getter
public class BeforeTrayFinalize extends EventManager.Event {
    private SystemTray tray;
    private PopupMenu popupMenu;
}
