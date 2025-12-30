package com.github.ozanaaslan.leafshot.gui;

import com.github.ozanaaslan.leafshot.LeafShot;
import com.github.ozanaaslan.leafshot.model.DrawingStroke;
import com.github.ozanaaslan.leafshot.model.Tool;
import com.github.ozanaaslan.leafshot.util.KeybindingUtil;
import com.github.ozanaaslan.leafshot.util.LeafShotConfig;
import com.github.ozanaaslan.leafshot.util.TransferableImage;
import com.github.ozanaaslan.leafshot.util.http.DefaultUploadHandler;
import com.github.ozanaaslan.leafshot.util.http.IUploadHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotWindow extends javax.swing.JFrame {
    @Getter private BufferedImage fullScreenshot;
    @Setter @Getter private Rectangle selection = null;
    private Rectangle screenBounds;

    @Setter @Getter private Tool currentTool = Tool.CURSOR;
    @Getter private final List<DrawingStroke> strokes = new ArrayList<>();
    @Setter @Getter private DrawingStroke currentStroke = null;

    private long cachedSelectionSize = -1;
    private Rectangle cachedSelectionBounds = null;


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
                String key = KeybindingUtil.normalize(e);
                if (key.equals(LeafShot.getLeafShot().getLeafShotConfig().get("keybinding.copy", "CTRL+C"))) {copySelectionToClipboard();}
                else if (key.equals(LeafShot.getLeafShot().getLeafShotConfig().get("keybinding.upload", "CTRL+U"))) { uploadSelection();}
                else if (key.equals(LeafShot.getLeafShot().getLeafShotConfig().get("keybinding.save", "CTRL+S"))) { saveSelection();}
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
    }

    @SneakyThrows
    private void saveSelection(){
        BufferedImage finalImg = getSelectedImage();
        if (finalImg == null) return;

        File file = new File(LeafShot.getLeafShot().getSaveDestination(), "screenshot.png");
        String name = "screenshot_";
        String ext = "png";
        int counter = 1;

        while (file.exists()) {
            file = new File(LeafShot.getLeafShot().getSaveDestination(),name + counter + "." + ext);
            counter++;
        }

        ImageIO.write(finalImg, ext, file);
        dispose();
    }

    private void uploadSelection(){
        BufferedImage img = getSelectedImage();
        if (img != null) {
            new Thread(() -> {
                String imageUrl = LeafShot.getLeafShot().getUploadHandler().upload(img);

                if (imageUrl != null) {
                    SwingUtilities.invokeLater(() -> {
                        JTextField textField = new JTextField(imageUrl);
                        textField.setEditable(false);
                        textField.setFont(new Font(textField.getFont().getName(), Font.PLAIN, 16));
                        textField.requestFocusInWindow();
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

    public void updateSelectionSizeEstimate() {
        if (selection == null) {
            cachedSelectionSize = -1;
            cachedSelectionBounds = null;
            return;
        }

        if (cachedSelectionBounds != null &&
                cachedSelectionBounds.equals(selection)) {
            return; // already up to date
        }

        BufferedImage img = getSelectedImage();
        if (img == null) return;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            cachedSelectionSize = baos.size();
            cachedSelectionBounds = new Rectangle(selection);
        } catch (IOException ignored) {}
    }

    public String getCachedSelectionSizeText() {
        if (cachedSelectionSize <= 0) return "";
        return formatBytes(cachedSelectionSize);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }


}