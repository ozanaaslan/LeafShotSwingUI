package com.github.ozanaaslan.leafshot.util;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class TransferableImage implements Transferable {
    private final Image image;

    public TransferableImage(Image i) {
        this.image = i;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor f) {
        return DataFlavor.imageFlavor.equals(f);
    }

    public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(f)) {
            throw new UnsupportedFlavorException(f);
        }
        return image;
    }
}
