package dev.enjarai.projectv.resource.palette;

public class Palette {
    private final PaletteKey key;
    private final int[] colors;

    Palette(PaletteKey key, int[] colors) {
        this.key = key;
        this.colors = colors;
    }

    public int lookUp(int keyColor) {
        var index = key.getIndex(keyColor);
        if (index == -1) {
            return keyColor;
        } else {
            return colors[index];
        }
    }
}
