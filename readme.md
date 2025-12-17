<p align="center">
  <img src="img.png" alt="LeafShot Preview" width="160">
</p>
<h3 align="center">LeafShot</h3>

<p align="center">
    <strong>Capture, Annotate, Copy, and Share.</strong><br>
    A lightweight FLOSS, cross-platform screenshot utility that feels familiar.
</p>

---

## Features

*   **Global Hotkey**: Press `Print Screen` to trigger capture mode instantly, no matter which app you're in.
*   **Multi-Monitor Support**: Seamlessly captures across all connected displays and virtual desktops.
*   **Precision Selection**:
    *   Draggable and resizable selection area with 8 distinct anchor points.
    *   Real-time pixel dimensions display.
    *   High-contrast "marching ants" border for perfect visibility.
*   **Built-in Annotation Tools**:
    *    **Pen**: Quick freehand notes in solid red.
    *    **Highlighter**: Emphasize text with opaque yellowish ink.
    *    **Cursor**: Switch back to resize or move your selection area.
*   **Instant Export**: Press `Ctrl + C` to save the selection (including annotations) directly to your clipboard as a PNG.
*   **Instant Upload**: Press `Ctrl + U` to upload the selection (including annotations) directly to your remote host.
*   **Minimalist Background Operation**: Runs silently in the Windows System Tray or macOS Menu Bar.

---

## Prerequisites & Setup

*   **Runtime**: Java SDK 8 or higher.
*   **Build Tool**: Maven.

### Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/ozanaaslan/LeafShotSwingUI.git
    cd LeafShotSwingUI
    ```

2.  **Build the project**:
    ```bash
    mvn clean install
    ```

3.  **Run**:
    ```bash
    mvn exec:java -Dexec.mainClass="com.github.ozanaaslan.leafshot.LeafShot"
    ```

---

## How to Use

1.  **Launch**: Start LeafShot. It will hide in your system's task menu.
2.  **Trigger**: Press the **Print Screen** key.
3.  **Select**: Left-click and drag to define your capture area.
4.  **Annotate**: Use the toolbar at the bottom of the selection to pick a drawing tool.
5.  **Finish**: Press **Ctrl + C** to copy to clipboard, or **Esc** to discard.

---

## Platform Specifics

| Platform | Behavior |
| :--- | :--- |
| **macOS** | Operates as a `UIElement` (Menu Bar only, no Dock icon). |
| **Windows** | Resides in the System Tray / Notification Area. |

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
```