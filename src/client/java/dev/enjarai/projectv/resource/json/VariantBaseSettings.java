package dev.enjarai.projectv.resource.json;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record VariantBaseSettings(List<VariantTextureSettings> textures) {
    public static final Codec<VariantBaseSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariantTextureSettings.CODEC.listOf().fieldOf("textures").forGetter(VariantBaseSettings::textures)
    ).apply(instance, VariantBaseSettings::new));

    public static VariantBaseSettings fromJsonOrThrow(JsonElement json) {
        return CODEC.decode(JsonOps.INSTANCE, json).result().orElseThrow().getFirst();
    }
}
