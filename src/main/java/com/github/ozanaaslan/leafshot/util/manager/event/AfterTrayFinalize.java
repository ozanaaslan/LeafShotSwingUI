package com.github.ozanaaslan.leafshot.util.manager.event;

import com.github.ozanaaslan.leafshot.manager.TrayManager;
import com.github.ozanaaslan.leafshot.util.manager.EventManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AfterTrayFinalize extends EventManager.Event {
    private TrayManager trayManager;
}
