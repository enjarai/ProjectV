package dev.enjarai.projectv.resource;

import dev.enjarai.projectv.resource.palette.Palette;
import net.minecraft.client.texture.NativeImage;
import org.jetbrains.annotations.Contract;

public class ImageUtils {
    @Contract("_, _ -> param1")
    public static NativeImage paletteifyImage(NativeImage source, Palette palette) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                var currentColor = source.getColor(x, y);
                var palettedColor = palette.lookUp(currentColor);

                if (currentColor != palettedColor) {
                    source.setColor(x, y, palettedColor);
                }
            }
        }
        return source;
    }

    @Contract("_, _ -> param1")
    public static NativeImage overlayImageWithTransparency(NativeImage source, NativeImage overlay) {
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                var sourceColor = source.getColor(x, y);
                var overlayColor = overlay.getColor(x, y);

                var blended = overlay(sourceColor, overlayColor);
                source.setColor(x, y, blended);
            }
        }
        return source;
    }

    private static int overlay(int baseColor, int overlayColor) {
        int baseAlpha = (baseColor >> 24) & 0xFF;
        int overlayAlpha = (overlayColor >> 24) & 0xFF;

        int blendedAlpha = overlayAlpha == 0xFF ? overlayAlpha : Math.min(0xFF, baseAlpha + overlayAlpha);

        int baseRed = baseColor & 0xFF;
        int baseGreen = (baseColor >> 8) & 0xFF;
        int baseBlue = (baseColor >> 16) & 0xFF;

        int overlayRed = overlayColor & 0xFF;
        int overlayGreen = (overlayColor >> 8) & 0xFF;
        int overlayBlue = (overlayColor >> 16) & 0xFF;

        int blendedRed = baseRed + ((overlayRed - baseRed) * overlayAlpha >> 8);
        int blendedGreen = baseGreen + ((overlayGreen - baseGreen) * overlayAlpha >> 8);
        int blendedBlue = baseBlue + ((overlayBlue - baseBlue) * overlayAlpha >> 8);

        return (blendedAlpha << 24) | (blendedBlue << 16) | (blendedGreen << 8) | blendedRed;
    }
}
