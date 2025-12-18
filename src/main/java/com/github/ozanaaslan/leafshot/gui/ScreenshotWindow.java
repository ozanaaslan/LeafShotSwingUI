package com.github.ozanaaslan.leafshot.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotWindow extends javax.swing.JFrame {
    private BufferedImage fullScreenshot;
    private Rectangle selection = null;
    private Rectangle screenBounds;

    private enum Tool { CURSOR, PEN, HIGHLIGHTER }
    private Tool currentTool = Tool.CURSOR;
    private final List<DrawingStroke> strokes = new ArrayList<>();
    private DrawingStroke currentStroke = null;

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
        add(new ScreenshotPanel());

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) copySelectionToClipboard();
                else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_U) uploadSelection();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
    }

    private void uploadSelection(){

    }

    private void copySelectionToClipboard() {
        if (selection == null || selection.width <= 2 || selection.height <= 2) return;

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

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(finalImg), null);
        dispose();
    }

    private static class DrawingStroke {
        Path2D path = new Path2D.Float();
        Color color;
        Stroke stroke;
        DrawingStroke(Color c, Stroke s) { this.color = c; this.stroke = s; }
    }

    private class ScreenshotPanel extends JPanel {
        private final int HANDLE_SIZE = 8;
        private int activeHandle = -1; // -1: none, 0-7: handles, 8: body
        private Point startPoint;
        private final Rectangle toolbarBounds = new Rectangle(0, 0, 120, 35);

        public ScreenshotPanel() {
            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (selection != null && toolbarBounds.contains(e.getPoint())) {
                        handleToolbarClick(e.getPoint());
                        return;
                    }

                    if (currentTool == Tool.CURSOR) {
                        startPoint = e.getPoint();
                        activeHandle = (selection != null) ? getHandleAt(e.getPoint()) : -1;
                        if (activeHandle == -1) {
                            selection = new Rectangle(e.getX(), e.getY(), 0, 0);
                            strokes.clear();
                            activeHandle = 7; // Bottom Right
                        }
                    } else if (selection != null && selection.contains(e.getPoint())) {
                        currentStroke = (currentTool == Tool.PEN) ?
                                new DrawingStroke(Color.RED, new BasicStroke(2f)) :
                                new DrawingStroke(new Color(255, 255, 0, 120), new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        currentStroke.path.moveTo(e.getX(), e.getY());
                        strokes.add(currentStroke);
                    }
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (currentTool == Tool.CURSOR && activeHandle != -1) {
                        int dx = e.getX() - startPoint.x;
                        int dy = e.getY() - startPoint.y;
                        if (activeHandle == 8) selection.translate(dx, dy);
                        else resizeSelection(e.getPoint());
                        startPoint = e.getPoint();
                    } else if (currentStroke != null) {
                        currentStroke.path.lineTo(e.getX(), e.getY());
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    currentStroke = null;
                    updateCursorType(e.getPoint());
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    updateCursorType(e.getPoint());
                }
            };
            addMouseListener(adapter);
            addMouseMotionListener(adapter);
        }

        private void handleToolbarClick(Point p) {
            int idx = (p.x - toolbarBounds.x) / (toolbarBounds.width / 3);
            currentTool = Tool.values()[idx];
            repaint();
        }

        private void updateCursorType(Point p) {
            if (selection != null && toolbarBounds.contains(p)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (currentTool == Tool.CURSOR) {
                int h = getHandleAt(p);
                setCursor(Cursor.getPredefinedCursor(getCursorForHandle(h)));
            } else {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            }
        }

        private int getCursorForHandle(int h) {
            switch(h) {
                case 0: return Cursor.NW_RESIZE_CURSOR; case 1: return Cursor.N_RESIZE_CURSOR;
                case 2: return Cursor.NE_RESIZE_CURSOR; case 3: return Cursor.W_RESIZE_CURSOR;
                case 4: return Cursor.E_RESIZE_CURSOR;  case 5: return Cursor.SW_RESIZE_CURSOR;
                case 6: return Cursor.S_RESIZE_CURSOR;  case 7: return Cursor.SE_RESIZE_CURSOR;
                case 8: return Cursor.MOVE_CURSOR;      default: return Cursor.CROSSHAIR_CURSOR;
            }
        }

        private int getHandleAt(Point p) {
            if (selection == null) return -1;
            Rectangle[] hs = getHandleRects();
            for (int i = 0; i < hs.length; i++) if (hs[i].contains(p)) return i;
            return selection.contains(p) ? 8 : -1;
        }

        private Rectangle[] getHandleRects() {
            int x = selection.x, y = selection.y, w = selection.width, h = selection.height, s = HANDLE_SIZE;
            return new Rectangle[]{
                    new Rectangle(x-s/2, y-s/2, s, s), new Rectangle(x+w/2-s/2, y-s/2, s, s), new Rectangle(x+w-s/2, y-s/2, s, s),
                    new Rectangle(x-s/2, y+h/2-s/2, s, s), new Rectangle(x+w-s/2, y+h/2-s/2, s, s),
                    new Rectangle(x-s/2, y+h-s/2, s, s), new Rectangle(x+w/2-s/2, y+h-s/2, s, s), new Rectangle(x+w-s/2, y+h-s/2, s, s)
            };
        }

        private void resizeSelection(Point p) {
            int x1 = selection.x, y1 = selection.y, x2 = selection.x + selection.width, y2 = selection.y + selection.height;
            if (activeHandle == 0 || activeHandle == 3 || activeHandle == 5) x1 = p.x;
            if (activeHandle == 2 || activeHandle == 4 || activeHandle == 7) x2 = p.x;
            if (activeHandle == 0 || activeHandle == 1 || activeHandle == 2) y1 = p.y;
            if (activeHandle == 5 || activeHandle == 6 || activeHandle == 7) y2 = p.y;
            selection.setBounds(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(fullScreenshot, 0, 0, null);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (selection != null) {
                Shape oldClip = g2.getClip();
                g2.setClip(selection);
                g2.drawImage(fullScreenshot, 0, 0, null);
                for (DrawingStroke s : strokes) {
                    g2.setColor(s.color); g2.setStroke(s.stroke); g2.draw(s.path);
                }
                g2.setClip(oldClip);

                // Border
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(1f)); g2.draw(selection);
                g2.setColor(Color.BLACK); g2.setStroke(new BasicStroke(1f, 0, 0, 10f, new float[]{5f, 5f}, 0f)); g2.draw(selection);

                // Handles
                g2.setStroke(new BasicStroke(1f));
                for (Rectangle r : getHandleRects()) {
                    g2.setColor(Color.WHITE); g2.fill(r); g2.setColor(Color.BLACK); g2.draw(r);
                }

                // Info & Toolbar
                String dim = selection.width + " x " + selection.height;
                g2.setColor(Color.BLACK); g2.drawString(dim, selection.x + 1, selection.y - 9);
                g2.setColor(Color.WHITE); g2.drawString(dim, selection.x, selection.y - 10);

                toolbarBounds.setLocation(selection.x, selection.y + selection.height + 5);
                g2.setColor(new Color(40, 40, 40, 220));
                g2.fillRoundRect(toolbarBounds.x, toolbarBounds.y, toolbarBounds.width, toolbarBounds.height, 8, 8);

                String[] labels = {"Select", "Draw", "Highlight"};
                for (int i = 0; i < 3; i++) {
                    int bx = toolbarBounds.x + i * (toolbarBounds.width / 3);
                    if (currentTool.ordinal() == i) {
                        g2.setColor(new Color(100, 100, 100));
                        g2.fillRoundRect(bx + 2, toolbarBounds.y + 2, (toolbarBounds.width / 3) - 4, toolbarBounds.height - 4, 4, 4);
                    }
                    g2.setColor(Color.WHITE);
                    g2.drawString(labels[i], bx + 5, toolbarBounds.y + 22);
                }
            }
        }
    }

    private static class TransferableImage implements Transferable {
        private final Image image;
        public TransferableImage(Image i) { this.image = i; }
        public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{DataFlavor.imageFlavor}; }
        public boolean isDataFlavorSupported(DataFlavor f) { return DataFlavor.imageFlavor.equals(f); }
        public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f);
            return image;
        }
    }
}