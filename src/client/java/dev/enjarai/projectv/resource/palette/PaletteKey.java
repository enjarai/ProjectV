package dev.enjarai.projectv.resource.palette;

public class PaletteKey {
    private final int[] keyColors;

    public PaletteKey(int[] keyColors) {
        this.keyColors = keyColors;
    }

    public Palette createPalette(int[] colors) {
        if (colors.length != keyColors.length) {
            throw new IllegalArgumentException("Amount of colors in palette must match amount of available keys, got " + colors.length + " expected " + keyColors.length);
        }
        return new Palette(this, colors);
    }

    public int getIndex(int color) {
        for (int i = 0; i < keyColors.length; i++) {
            if (color == keyColors[i]) {
                return i;
            }
        }
        return -1;
    }
}
