package dev.enjarai.projectv.resource;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface TextureVariantFactory {
    static TextureVariantFactory dummy(int color) {
        return (resourceManager, baseTexture, materialTexture) -> {
            var texture = new NativeImage(16, 16, true);
            texture.fillRect(0, 0, 16, 16, color);
            return texture;
        };
    }

    static TextureVariantFactory paletted(Identifier paletteKeyLocation) {
        return (resourceManager, baseTexture, materialTexture) -> {
            var paletteKeyResource = resourceManager.getResource(paletteKeyLocation);
            if (paletteKeyResource.isEmpty()) {
                throw new RuntimeException("Could not find palette key '" + paletteKeyLocation + "'.");
            }
            var paletteKeyTexture = NativeImage.read(paletteKeyResource.get().getInputStream());

            var resultTexture = baseTexture.applyToCopy(IntUnaryOperator.identity());
            return ImageUtils.paletteifyImage(resultTexture, paletteKeyTexture, materialTexture);
        };
    }

    @Contract("_, _, _ -> new")
    NativeImage createVariant(ResourceManager resourceManager, NativeImage baseTexture, NativeImage materialTexture) throws IOException;
}
