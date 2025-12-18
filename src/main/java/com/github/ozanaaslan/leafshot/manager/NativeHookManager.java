package com.github.ozanaaslan.leafshot.manager;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.ozanaaslan.leafshot.gui.ScreenshotWindow;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeHookManager implements NativeKeyListener {

    public void register() {
        try {
            // Disable JNativeHook logging to keep console clean
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }

    public void unregister() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN || (e.getKeyCode() == NativeKeyEvent.VC_F3)) {
            triggerScreenshot();
        }
    }

    public void triggerScreenshot() {
        SwingUtilities.invokeLater(() -> {
            ScreenshotWindow window = new ScreenshotWindow();
            window.setVisible(true);
            window.setAlwaysOnTop(true);
        });
    }
}
