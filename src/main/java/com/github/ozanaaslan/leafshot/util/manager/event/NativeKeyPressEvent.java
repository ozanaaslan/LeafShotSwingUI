package com.github.ozanaaslan.leafshot.util.manager.event;

import com.github.ozanaaslan.leafshot.util.manager.EventManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NativeKeyPressEvent extends EventManager.Event {
    private String key;
}
