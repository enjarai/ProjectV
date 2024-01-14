package dev.enjarai.projectv.resource.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.projectv.resource.TextureVariantFactory;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record VariantTextureSettings(Optional<Identifier> baseTexture, TextureVariantFactory mergeFunction) {
    public static final Codec<VariantTextureSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("base").forGetter(VariantTextureSettings::baseTexture),
            TextureVariantFactory.CODEC.fieldOf("merge_function").forGetter(VariantTextureSettings::mergeFunction)
    ).apply(instance, VariantTextureSettings::new));
}
