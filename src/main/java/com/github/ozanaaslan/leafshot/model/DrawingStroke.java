package com.github.ozanaaslan.leafshot.model;

import java.awt.*;
import java.awt.geom.Path2D;

public class DrawingStroke {
    public Path2D path = new Path2D.Float();
    public Color color;
    public Stroke stroke;

    public DrawingStroke(Color c, Stroke s) {
        this.color = c;
        this.stroke = s;
    }
}
