package dev.enjarai.projectv.resource.json;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record VariantBaseSettings(Map<String, Either<String, VariantTextureSettings>> textures) {
    public static final Codec<VariantBaseSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.either(Codec.STRING, VariantTextureSettings.CODEC)).fieldOf("textures").forGetter(VariantBaseSettings::textures)
    ).apply(instance, VariantBaseSettings::new));

    public static VariantBaseSettings fromJsonOrThrow(JsonElement json) {
        return CODEC.decode(JsonOps.INSTANCE, json).result().orElseThrow().getFirst();
    }

    public Map<String, VariantTextureSettings> getNormalizedTextures() {
        var result = ImmutableMap.<String, VariantTextureSettings>builder();

        textures.forEach((key, either) -> {
            either.ifLeft(left -> {
                result.put(key, textures.get(left).right().orElseThrow());
            }).ifRight(right -> {
                result.put(key, right);
            });
        });

        return result.buildKeepingLast();
    }
}
