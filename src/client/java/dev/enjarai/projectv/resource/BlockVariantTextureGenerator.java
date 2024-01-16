package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.pack.RuntimePack;
import dev.enjarai.projectv.resource.json.VariantBaseSettings;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Generates all block variant textures and stores them in its pack.
 * The pack is added to any created client_resource ResourceManagers.
 * <p>
 * TODO implement concurrency here?
 */
public class BlockVariantTextureGenerator implements SimpleSynchronousResourceReloadListener {
    public static final RuntimePack PACK = RuntimePack.create("Project V: Block Variants", ResourceType.CLIENT_RESOURCES);

    static {
        PackAdderEvent.EVENT.register((managerType, packs) -> packs.add(PACK));
    }

    public static final Identifier IDENTIFIER = new Identifier(ProjectV.MOD_ID, "block_variant_generator");


    @Override
    public Identifier getFabricId() {
        return IDENTIFIER;
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(ResourceReloadListenerKeys.MODELS, ResourceReloadListenerKeys.TEXTURES);
    }

    @Override
    public void reload(ResourceManager manager) {
        ProjectV.LOGGER.info("Generating variant textures and models...");
        var startTime = System.currentTimeMillis();

        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        BlockVariantGenerator.iterateOverGroups(materialGroup -> {
            BlockVariantGenerator.iterateOverVariants(materialGroup, (baseBlock, materialBlock) -> {

                var baseId = Registries.BLOCK.getId(baseBlock);
                var materialId = Registries.BLOCK.getId(materialBlock);

                try {
                    var variantBlockId = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);

                    var originalStateJson = JsonParser.parseReader(manager.openAsReader(baseId.withPrefixedPath("blockstates/").withSuffixedPath(".json")));
                    AtomicReference<Identifier> firstModel = new AtomicReference<>();

                    var generatedStateJson = mapBlockStateModelsCached(originalStateJson, baseModelId -> {
                        var settings = VariantBaseSettings.fromJsonOrThrow(JsonParser.parseReader(
                                manager.openAsReader(baseModelId.withPrefixedPath("projectv/model_settings/").withSuffixedPath(".json"))));

                        var textures = new HashMap<String, Identifier>();
                        // First iteration to generate textures
                        for (var textureEntry : settings.textures().entrySet()) {
                            var right = textureEntry.getValue().right();
                            if (right.isPresent()) {
                                var textureSettings = right.get();
                                try (var generatedTexture = textureSettings.mergeFunction().createVariant(
                                        manager, textureSettings.baseTexture().map(id -> id.withPrefixedPath("projectv/base_texture/").withSuffixedPath(".png")).orElse(null),
                                        s -> materialId.withPrefixedPath("projectv/material/block/" + s + "/").withSuffixedPath(".png")
                                )) {
                                    var textureId = variantBlockId.withPrefixedPath("block/").withSuffixedPath("_" + textureEntry.getKey());
                                    mapBuilder.put(textureId.withPrefixedPath("textures/").withSuffixedPath(".png"), generatedTexture.getBytes());
                                    textures.put(textureEntry.getKey(), textureId);
                                }
                            }
                        }
                        // Second iteration to handle shortcut references
                        for (var textureEntry : settings.textures().entrySet()) {
                            textureEntry.getValue().ifLeft(pointsTo -> {
                                textures.put(textureEntry.getKey(), textures.get(pointsTo));
                            });
                        }

                        // Generate model
                        var variantModelId = variantBlockId.withPrefixedPath("block/");
                        var properlyKeyedTextures = new HashMap<TextureKey, Identifier>();
                        textures.forEach((k, v) -> properlyKeyedTextures.put(TextureKey.of(k), v)); // Its stupid but we gotta do this
                        var generatedModelJson = new Model(Optional.of(baseModelId), Optional.empty())
                                .createJson(variantModelId, properlyKeyedTextures);
                        mapBuilder.put(variantModelId.withPrefixedPath("models/").withSuffixedPath(".json"), generatedModelJson.toString().getBytes(StandardCharsets.UTF_8));

                        // TODO Find a better way to deal with item models
                        if (firstModel.get() == null) {
                            firstModel.set(variantModelId);
                        }

                        return variantModelId;
                    });

                    // Generate state definition
                    mapBuilder.put(variantBlockId.withPrefixedPath("blockstates/").withSuffixedPath(".json"), generatedStateJson.toString().getBytes(StandardCharsets.UTF_8));

                    // Generate item model
                    var variantItemModelId = variantBlockId.withPrefixedPath("models/item/");
                    var generatedItemModelJson = new Model(Optional.of(firstModel.get()), Optional.empty()).createJson(variantItemModelId, Map.of());
                    mapBuilder.put(variantItemModelId.withSuffixedPath(".json"), generatedItemModelJson.toString().getBytes(StandardCharsets.UTF_8));

                } catch (Exception e) {
                    ProjectV.LOGGER.error(String.format("Failed to apply variant %s to %s", materialId, baseId), e);
                }
            });
        });
        var map = mapBuilder.buildKeepingLast();

        var timeElapsed = System.currentTimeMillis() - startTime;
        ProjectV.LOGGER.info("Done! Generated {} files, took {} ms.", map.size(), timeElapsed);

        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }

    private JsonElement mapBlockStateModelsCached(JsonElement blockStateJson, ModelIdMapper mapper) {
        var cache = new HashMap<Identifier, Identifier>();
        ModelIdMapper internalMapper = id -> {
            if (cache.containsKey(id)) return cache.get(id);

            var result = mapper.apply(id);
            cache.put(id, result);
            return result;
        };

        try {
            var blockStateObj = blockStateJson.getAsJsonObject();
            if (blockStateObj.has("variants")) {
                var variants = blockStateObj.get("variants").getAsJsonObject();
                for (var entry : variants.entrySet()) {
                    var variant = entry.getValue();
                    if (variant.isJsonArray()) {
                        for (var variantPart : variant.getAsJsonArray()) {
                            var model = variantPart.getAsJsonObject();
                            model.addProperty("model", internalMapper.apply(new Identifier(model.get("model").getAsString())).toString());
                        }
                    } else {
                        var model = variant.getAsJsonObject();
                        model.addProperty("model", internalMapper.apply(new Identifier(model.get("model").getAsString())).toString());
                    }
                }
            } else {
                var multipart = blockStateObj.get("multipart").getAsJsonArray();
                for (var part : multipart) {
                    var apply = part.getAsJsonObject().get("apply");
                    if (apply.isJsonArray()) {
                        for (var variantPart : apply.getAsJsonArray()) {
                            var model = variantPart.getAsJsonObject();
                            model.addProperty("model", internalMapper.apply(new Identifier(model.get("model").getAsString())).toString());
                        }
                    } else {
                        var model = apply.getAsJsonObject();
                        model.addProperty("model", internalMapper.apply(new Identifier(model.get("model").getAsString())).toString());
                    }
                }
            }
        } catch (Exception e) {
            throw new JsonParseException("Error modifying blockstate model references", e);
        }
        return blockStateJson;
    }

    @FunctionalInterface
    interface ModelIdMapper {
        Identifier apply(Identifier modelId) throws IOException;
    }
}
