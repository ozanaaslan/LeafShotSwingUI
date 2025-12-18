package com.github.ozanaaslan.leafshot.gui;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.ozanaaslan.leafshot.model.DrawingStroke;
import com.github.ozanaaslan.leafshot.model.Tool;
import com.github.ozanaaslan.leafshot.util.TransferableImage;
import com.github.ozanaaslan.leafshot.util.UploadHandler;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotWindow extends javax.swing.JFrame {
    @Getter private BufferedImage fullScreenshot;
    @Setter @Getter private Rectangle selection = null;
    private Rectangle screenBounds;

    @Setter @Getter private Tool currentTool = Tool.CURSOR;
    @Getter private final List<DrawingStroke> strokes = new ArrayList<>();
    @Setter @Getter private DrawingStroke currentStroke = null;

    public ScreenshotWindow() {
        captureAllScreens();
        initComponents();
    }

    private void captureAllScreens() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        screenBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            screenBounds = screenBounds.union(screen.getDefaultConfiguration().getBounds());
        }
        try {
            Robot robot = new Robot();
            fullScreenshot = robot.createScreenCapture(screenBounds);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setUndecorated(true);
        setBounds(screenBounds);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(new ScreenshotPanel(this));

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C || (e.isActionKey() && e.getKeyCode() == NativeKeyEvent.VC_C)) copySelectionToClipboard();
                else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_U || (e.isActionKey() && e.getKeyCode() == NativeKeyEvent.VC_U)) uploadSelection();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
    }

    private void uploadSelection(){
        BufferedImage img = getSelectedImage();
        if (img != null) {
            new Thread(() -> {
                String remoteHost = com.github.ozanaaslan.leafshot.LeafShot.getLeafShot().getLeafShotConfig().getRemoteHost();
                String baseUrl = "http://" + remoteHost + ":8091";
                UploadHandler handler = new UploadHandler(baseUrl);
                String imageUrl = handler.uploadImage(img);

                if (imageUrl != null) {
                    SwingUtilities.invokeLater(() -> {
                        JTextField textField = new JTextField(imageUrl);
                        textField.setEditable(false);
                        JOptionPane.showMessageDialog(null, textField, "Upload Successful - Copy Link", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Upload failed. Check console for details.", "Upload Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
            dispose();
        }
    }

    private BufferedImage getSelectedImage() {
        if (selection == null || selection.width <= 2 || selection.height <= 2) return null;

        BufferedImage finalImg = new BufferedImage(selection.width, selection.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = finalImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw cropped screenshot
        g2.drawImage(fullScreenshot, 0, 0, selection.width, selection.height,
                selection.x - screenBounds.x, selection.y - screenBounds.y,
                selection.x - screenBounds.x + selection.width, selection.y - screenBounds.y + selection.height, null);

        // Draw strokes with relative offset
        g2.translate(-selection.x, -selection.y);
        for (DrawingStroke s : strokes) {
            g2.setColor(s.color);
            g2.setStroke(s.stroke);
            g2.draw(s.path);
        }
        g2.dispose();
        return finalImg;
    }

    private void copySelectionToClipboard() {
        BufferedImage finalImg = getSelectedImage();
        if (finalImg == null) return;

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(finalImg), null);
        dispose();
    }
}