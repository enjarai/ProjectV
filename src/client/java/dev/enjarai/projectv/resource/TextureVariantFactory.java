package dev.enjarai.projectv.resource;

import com.mojang.serialization.Codec;
import dev.enjarai.projectv.ProjectV;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Map;
import java.util.function.IntUnaryOperator;

@FunctionalInterface
public interface TextureVariantFactory {
    // Codecs here dont support encoding, cause we dont need it and i really cba
    Map<Identifier, Codec<? extends TextureVariantFactory>> ALL = Map.of(
            ProjectV.id("dummy"), Codec.INT.fieldOf("color").xmap(TextureVariantFactory::dummy, f -> null).codec(),
            ProjectV.id("palette"), Identifier.CODEC.fieldOf("key").xmap(TextureVariantFactory::paletted, f -> null).codec(),
            ProjectV.id("original_material_texture"), Codec.unit(originalMaterialTexture()),
            ProjectV.id("overlay"), Codec.unit(overlay())
    );
    Codec<TextureVariantFactory> CODEC = Identifier.CODEC.dispatch(f -> null, ALL::get);

    static TextureVariantFactory dummy(int color) {
        return (resourceManager, baseTexture, materialTextureSupplier) -> {
            var texture = new NativeImage(16, 16, true);
            texture.fillRect(0, 0, 16, 16, color);
            return texture;
        };
    }

    static TextureUsing paletted(Identifier paletteKeyLocation) {
        return (resourceManager, baseTexture, materialTextureSupplier) -> {
            var materialTexture = materialTextureSupplier.get("palette");

            var paletteKeyResource = resourceManager.getResource(paletteKeyLocation.withPrefixedPath("projectv/").withSuffixedPath(".png"));
            if (paletteKeyResource.isEmpty()) {
                throw new RuntimeException("Could not find palette key '" + paletteKeyLocation + "'.");
            }
            var paletteKeyTexture = NativeImage.read(paletteKeyResource.get().getInputStream());

            var resultTexture = baseTexture.applyToCopy(IntUnaryOperator.identity());
            return ImageUtils.paletteifyImage(resultTexture, paletteKeyTexture, materialTexture);
        };
    }

    static TextureVariantFactory originalMaterialTexture() {
        return (resourceManager, baseTextureId, materialTextureIdSupplier) -> {
            var materialTextureId = materialTextureIdSupplier.get("base");
            return NativeImage.read(resourceManager.getResource(materialTextureId).orElseThrow(() -> new IOException("Material texture could not be loaded: " + materialTextureId)).getInputStream());
        };
    }

    static TextureUsing overlay() {
        return (resourceManager, baseTexture, materialTextureSupplier) -> {
            var materialTexture = materialTextureSupplier.get("palette");

            var resultTexture = materialTexture.applyToCopy(IntUnaryOperator.identity());
            return ImageUtils.overlayImageWithTransparency(resultTexture, baseTexture);
        };
    }


    NativeImage createVariant(ResourceManager resourceManager, Identifier baseTextureId, StringToSomething<Identifier> materialTextureIdSupplier) throws IOException;

    @FunctionalInterface
    interface TextureUsing extends TextureVariantFactory {
        @Override
        default NativeImage createVariant(ResourceManager resourceManager, Identifier baseTextureId, StringToSomething<Identifier> materialTextureIdSupplier) throws IOException {
            return createVariantWithImages(
                    resourceManager,
                    NativeImage.read(resourceManager.getResource(baseTextureId).orElseThrow(() -> new IOException("Base texture could not be loaded: " + baseTextureId)).getInputStream()),
                    s -> {
                        var textureId = materialTextureIdSupplier.get(s);
                        return NativeImage.read(resourceManager.getResource(textureId)
                                .orElseThrow(() -> new IOException("Material texture could not be loaded: " + textureId)).getInputStream());
                    }
            );
        }

        NativeImage createVariantWithImages(ResourceManager resourceManager, NativeImage baseTexture, StringToSomething<NativeImage> materialTextureSupplier) throws IOException;
    }

    @FunctionalInterface
    interface StringToSomething<T> {
        T get(String string) throws IOException;
    }
}