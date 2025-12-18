package com.github.ozanaaslan.leafshot.gui;

import com.github.ozanaaslan.leafshot.model.DrawingStroke;
import com.github.ozanaaslan.leafshot.model.Tool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ScreenshotPanel extends JPanel {
    private final ScreenshotWindow parent;
    private final int HANDLE_SIZE = 8;
    private int activeHandle = -1; // -1: none, 0-7: handles, 8: body
    private Point startPoint;
    private final Rectangle toolbarBounds = new Rectangle(0, 0, 120, 35);

    public ScreenshotPanel(ScreenshotWindow parent) {
        this.parent = parent;
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (parent.getSelection() != null && toolbarBounds.contains(e.getPoint())) {
                    handleToolbarClick(e.getPoint());
                    return;
                }

                if (parent.getCurrentTool() == Tool.CURSOR) {
                    startPoint = e.getPoint();
                    activeHandle = (parent.getSelection() != null) ? getHandleAt(e.getPoint()) : -1;
                    if (activeHandle == -1) {
                        parent.setSelection(new Rectangle(e.getX(), e.getY(), 0, 0));
                        parent.getStrokes().clear();
                        activeHandle = 7; // Bottom Right
                    }
                } else if (parent.getSelection() != null && parent.getSelection().contains(e.getPoint())) {
                    DrawingStroke currentStroke = (parent.getCurrentTool() == Tool.PEN) ?
                            new DrawingStroke(Color.RED, new BasicStroke(2f)) :
                            new DrawingStroke(new Color(255, 255, 0, 120), new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    currentStroke.path.moveTo(e.getX(), e.getY());
                    parent.setCurrentStroke(currentStroke);
                    parent.getStrokes().add(currentStroke);
                }
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (parent.getCurrentTool() == Tool.CURSOR && activeHandle != -1) {
                    int dx = e.getX() - startPoint.x;
                    int dy = e.getY() - startPoint.y;
                    if (activeHandle == 8) parent.getSelection().translate(dx, dy);
                    else resizeSelection(e.getPoint());
                    startPoint = e.getPoint();
                } else if (parent.getCurrentStroke() != null) {
                    parent.getCurrentStroke().path.lineTo(e.getX(), e.getY());
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                parent.setCurrentStroke(null);
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
        parent.setCurrentTool(Tool.values()[idx]);
        repaint();
    }

    private void updateCursorType(Point p) {
        if (parent.getSelection() != null && toolbarBounds.contains(p)) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (parent.getCurrentTool() == Tool.CURSOR) {
            int h = getHandleAt(p);
            setCursor(Cursor.getPredefinedCursor(getCursorForHandle(h)));
        } else {
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    private int getCursorForHandle(int h) {
        switch (h) {
            case 0: return Cursor.NW_RESIZE_CURSOR;
            case 1: return Cursor.N_RESIZE_CURSOR;
            case 2: return Cursor.NE_RESIZE_CURSOR;
            case 3: return Cursor.W_RESIZE_CURSOR;
            case 4: return Cursor.E_RESIZE_CURSOR;
            case 5: return Cursor.SW_RESIZE_CURSOR;
            case 6: return Cursor.S_RESIZE_CURSOR;
            case 7: return Cursor.SE_RESIZE_CURSOR;
            case 8: return Cursor.MOVE_CURSOR;
            default: return Cursor.CROSSHAIR_CURSOR;
        }
    }

    private int getHandleAt(Point p) {
        if (parent.getSelection() == null) return -1;
        Rectangle[] hs = getHandleRects();
        for (int i = 0; i < hs.length; i++) if (hs[i].contains(p)) return i;
        return parent.getSelection().contains(p) ? 8 : -1;
    }

    private Rectangle[] getHandleRects() {
        Rectangle selection = parent.getSelection();
        int x = selection.x, y = selection.y, w = selection.width, h = selection.height, s = HANDLE_SIZE;
        return new Rectangle[]{
                new Rectangle(x - s / 2, y - s / 2, s, s), new Rectangle(x + w / 2 - s / 2, y - s / 2, s, s), new Rectangle(x + w - s / 2, y - s / 2, s, s),
                new Rectangle(x - s / 2, y + h / 2 - s / 2, s, s), new Rectangle(x + w - s / 2, y + h / 2 - s / 2, s, s),
                new Rectangle(x - s / 2, y + h - s / 2, s, s), new Rectangle(x + w / 2 - s / 2, y + h - s / 2, s, s), new Rectangle(x + w - s / 2, y + h - s / 2, s, s)
        };
    }

    private void resizeSelection(Point p) {
        Rectangle selection = parent.getSelection();
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
        g2.drawImage(parent.getFullScreenshot(), 0, 0, null);
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRect(0, 0, getWidth(), getHeight());

        Rectangle selection = parent.getSelection();
        if (selection != null) {
            Shape oldClip = g2.getClip();
            g2.setClip(selection);
            g2.drawImage(parent.getFullScreenshot(), 0, 0, null);
            for (DrawingStroke s : parent.getStrokes()) {
                g2.setColor(s.color);
                g2.setStroke(s.stroke);
                g2.draw(s.path);
            }
            g2.setClip(oldClip);

            // Border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(selection);
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1f, 0, 0, 10f, new float[]{5f, 5f}, 0f));
            g2.draw(selection);

            // Handles
            g2.setStroke(new BasicStroke(1f));
            for (Rectangle r : getHandleRects()) {
                g2.setColor(Color.WHITE);
                g2.fill(r);
                g2.setColor(Color.BLACK);
                g2.draw(r);
            }

            // Info & Toolbar
            String dim = selection.width + " x " + selection.height;
            g2.setColor(Color.BLACK);
            g2.drawString(dim, selection.x + 1, selection.y - 9);
            g2.setColor(Color.WHITE);
            g2.drawString(dim, selection.x, selection.y - 10);

            toolbarBounds.setLocation(selection.x, selection.y + selection.height + 5);
            g2.setColor(new Color(40, 40, 40, 220));
            g2.fillRoundRect(toolbarBounds.x, toolbarBounds.y, toolbarBounds.width, toolbarBounds.height, 8, 8);

            String[] labels = {"Select", "Draw", "Highlight"};
            for (int i = 0; i < 3; i++) {
                int bx = toolbarBounds.x + i * (toolbarBounds.width / 3);
                if (parent.getCurrentTool().ordinal() == i) {
                    g2.setColor(new Color(100, 100, 100));
                    g2.fillRoundRect(bx + 2, toolbarBounds.y + 2, (toolbarBounds.width / 3) - 4, toolbarBounds.height - 4, 4, 4);
                }
                g2.setColor(Color.WHITE);
                g2.drawString(labels[i], bx + 5, toolbarBounds.y + 22);
            }
        }
    }
}
