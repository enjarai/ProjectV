package dev.enjarai.projectv.resource;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

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

    static TextureUsing paletted(Identifier paletteKeyLocation) {
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

    NativeImage createVariant(ResourceManager resourceManager, Identifier baseTextureId, Identifier materialTextureId) throws IOException;

    @FunctionalInterface
    interface TextureUsing extends TextureVariantFactory {
        @Override
        default NativeImage createVariant(ResourceManager resourceManager, Identifier baseTextureId, Identifier materialTextureId) throws IOException {
            return createVariantWithImages(
                    resourceManager,
                    NativeImage.read(resourceManager.getResource(baseTextureId).orElseThrow(() -> new IOException("Base texture could not be loaded: " + baseTextureId)).getInputStream()),
                    NativeImage.read(resourceManager.getResource(materialTextureId).orElseThrow(() -> new IOException("Material texture could not be loaded: " + materialTextureId)).getInputStream())
            );
        }

        NativeImage createVariantWithImages(ResourceManager resourceManager, NativeImage baseTexture, NativeImage materialTexture) throws IOException;
    }
}
