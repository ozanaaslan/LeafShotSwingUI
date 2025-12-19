package com.github.ozanaaslan.leafshot.util;

import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public final class KeybindingUtil {

    private static final Map<String, Integer> NATIVE_KEY_MAP = new HashMap<>();

    static {
        // Build string -> NativeKeyEvent.VC_* map via reflection
        for (Field field : NativeKeyEvent.class.getDeclaredFields()) {
            if (field.getName().startsWith("VC_")) {
                try {
                    String name = field.getName().substring(3);
                    NATIVE_KEY_MAP.put(name, field.getInt(null));
                } catch (IllegalAccessException ignored) {}
            }
        }
    }

    private KeybindingUtil() {
        // utility class
    }

    /* ============================================================
       =============== SWING KEY CAPTURE SECTION ==================
       ============================================================ */

    public static void attachKeybindingField(
            JTextField field,
            String configKey,
            Map<String, JComponent> components
    ) {
        field.setEditable(false);
        field.setFocusTraversalKeysEnabled(false);
        field.setBackground(new Color(245, 245, 245));

        KeyboardFocusManager manager =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();

        KeyEventDispatcher dispatcher = e -> {
            if (!field.isFocusOwner()) return false;
            if (e.getID() != KeyEvent.KEY_PRESSED) return true;

            // ignore pure modifier presses
            if (isModifierOnly(e.getKeyCode())) return true;

            String combo = toCanonicalCombo(e);
            field.setText(combo);

            return true; // consume
        };

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBackground(new Color(220, 235, 255));
                manager.addKeyEventDispatcher(dispatcher);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBackground(Color.WHITE);
                manager.removeKeyEventDispatcher(dispatcher);
            }
        });
    }

    private static boolean isModifierOnly(int keyCode) {
        return keyCode == KeyEvent.VK_CONTROL
            || keyCode == KeyEvent.VK_SHIFT
            || keyCode == KeyEvent.VK_ALT
            || keyCode == KeyEvent.VK_META;
    }

    private static String toCanonicalCombo(KeyEvent e) {
        StringBuilder sb = new StringBuilder();

        if (e.isControlDown()) sb.append("CTRL+");
        if (e.isAltDown())     sb.append("ALT+");
        if (e.isShiftDown())   sb.append("SHIFT+");
        if (e.isMetaDown())    sb.append("META+");

        sb.append(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
        return sb.toString();
    }

    /* ============================================================
       ============ JNATIVEHOOK MATCHING SECTION ==================
       ============================================================ */

    public static boolean matchesNativeEvent(
            NativeKeyEvent event,
            String storedCombo
    ) {
        ParsedCombo combo = parseCombo(storedCombo);
        if (combo == null) return false;

        int mods = event.getModifiers();

        if (((mods & NativeInputEvent.CTRL_MASK)  != 0) != combo.ctrl)  return false;
        if (((mods & NativeInputEvent.ALT_MASK)   != 0) != combo.alt)   return false;
        if (((mods & NativeInputEvent.SHIFT_MASK) != 0) != combo.shift) return false;
        if (((mods & NativeInputEvent.META_MASK)  != 0) != combo.meta)  return false;

        return event.getKeyCode() == combo.keyCode;
    }

    /* ============================================================
       ================== PARSING SECTION =========================
       ============================================================ */

    private static ParsedCombo parseCombo(String combo) {
        if (combo == null || combo.isEmpty()) return null;

        ParsedCombo parsed = new ParsedCombo();

        for (String part : combo.split("\\+")) {
            switch (part) {
                case "CTRL":  parsed.ctrl = true; break;
                case "ALT":   parsed.alt = true; break;
                case "SHIFT": parsed.shift = true; break;
                case "META":  parsed.meta = true; break;
                default:
                    Integer code = NATIVE_KEY_MAP.get(part);
                    if (code == null) return null;
                    parsed.keyCode = code;
            }
        }

        return parsed;
    }

    private static final class ParsedCombo {
        boolean ctrl;
        boolean alt;
        boolean shift;
        boolean meta;
        int keyCode;
    }
    public static String normalize(KeyEvent e) {
        StringBuilder sb = new StringBuilder();

        if (e.isControlDown()) sb.append("CTRL+");
        if (e.isAltDown()) sb.append("ALT+");
        if (e.isShiftDown()) sb.append("SHIFT+");

        sb.append(KeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
        return sb.toString();
    }

    public static String normalize(NativeKeyEvent e) {
        StringBuilder sb = new StringBuilder();

        int m = e.getModifiers();
        if ((m & NativeKeyEvent.CTRL_MASK) != 0) sb.append("CTRL+");
        if ((m & NativeKeyEvent.ALT_MASK) != 0) sb.append("ALT+");
        if ((m & NativeKeyEvent.SHIFT_MASK) != 0) sb.append("SHIFT+");

        sb.append(NativeKeyEvent.getKeyText(e.getKeyCode()).toUpperCase());
        return sb.toString();
    }
}
