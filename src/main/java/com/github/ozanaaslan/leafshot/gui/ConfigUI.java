package com.github.ozanaaslan.leafshot.gui;

import com.github.ozanaaslan.leafshot.util.Config;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUI extends JDialog {
    private final Config config;
    private final Map<String, JComponent> components = new HashMap<>();

    public ConfigUI(Config config) {
        this.config = config;
        setTitle("Configuration");
        setModal(true);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Properties props = config.getProperties();
        for (String key : props.stringPropertyNames()) {
            mainPanel.add(new JLabel(key + ":"));
            String value = props.getProperty(key);

            if (isBoolean(value)) {
                JCheckBox checkBox = new JCheckBox("", Boolean.parseBoolean(value));
                components.put(key, checkBox);
                mainPanel.add(checkBox);
            } else {
                JTextField field = new JTextField(value);
                components.put(key, field);
                mainPanel.add(field);
            }
        }

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            components.forEach((key, component) -> {
                if (component instanceof JCheckBox) {
                    config.getProperties().setProperty(key, String.valueOf(((JCheckBox) component).isSelected()));
                } else if (component instanceof JTextField) {
                    config.getProperties().setProperty(key, ((JTextField) component).getText());
                }
            });
            config.save();
            dispose();
        });
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private boolean isBoolean(String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"));
    }
}