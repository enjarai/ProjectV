package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonParser;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.resource.json.VariantBaseSettings;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Generates all block variant textures and stores them in its pack.
 * The pack is added to any created client_resource ResourceManagers.
 * <p>
 * TODO implement concurrency here?
 */
public class BlockVariantTextureGenerator implements SimpleSynchronousResourceReloadListener {
    public static final RuntimeResourcePack PACK = new RuntimeResourcePack("Project V: Block Variants");
    static {
        PackAdderEvent.EVENT.register((managerType, packs) -> packs.add(PACK));
    }
    private static final HashMap<BlockMaterialGroup, TextureVariantHolder> HOLDERS = new HashMap<>();
    public static final Identifier IDENTIFIER = new Identifier(ProjectV.MOD_ID, "block_variant_generator");


    public static void registerTextureFactory(BlockMaterialGroup materialGroup, TextureVariantFactory textureFactory) {
        HOLDERS.put(materialGroup, new TextureVariantHolder(textureFactory));
    }

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
        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        HOLDERS.forEach((materialGroup, holder) -> {
            BlockVariantGenerator.iterateOverVariants(materialGroup, (baseBlock, materialBlock) -> {

                var baseId = Registries.BLOCK.getId(baseBlock);
                var materialId = Registries.BLOCK.getId(materialBlock);

                try {
                    var variantBlockId = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);
                    var baseModelId = baseId.withPrefixedPath("block/"); // TODO do this for every model in blockstate definition

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
                    var variantModelId = variantBlockId.withPrefixedPath("models/block/");
                    var properlyKeyedTextures = new HashMap<TextureKey, Identifier>();
                    textures.forEach((k, v) -> properlyKeyedTextures.put(TextureKey.of(k), v)); // Its stupid but we gotta do this
                    var generatedModelJson = new Model(Optional.of(baseModelId), Optional.empty())
                            .createJson(variantModelId, properlyKeyedTextures);
                    mapBuilder.put(variantModelId.withSuffixedPath(".json"), generatedModelJson.toString().getBytes(StandardCharsets.UTF_8));

                    // Generate item model
                    var variantItemModelId = variantBlockId.withPrefixedPath("models/item/");
                    var generatedItemModelJson = new Model(Optional.of(variantBlockId.withPrefixedPath("block/")), Optional.empty()).createJson(variantItemModelId, Map.of());
                    mapBuilder.put(variantItemModelId.withSuffixedPath(".json"), generatedItemModelJson.toString().getBytes(StandardCharsets.UTF_8));
                    System.out.println(generatedItemModelJson);

                    // Generate state definition
                    var generatedStateJson = VariantsBlockStateSupplier.create(Registries.BLOCK.get(variantBlockId),
                            BlockStateVariant.create().put(VariantSettings.MODEL, variantBlockId.withPrefixedPath("block/"))).get();
                    mapBuilder.put(variantBlockId.withPrefixedPath("blockstates/").withSuffixedPath(".json"), generatedStateJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO yea you get it, need to make this dynamic

                } catch (Exception e) {
                    ProjectV.LOGGER.error(String.format("Failed to apply variant %s to %s:", materialId, baseId), e);
                }
            });
        });
        var map =  mapBuilder.buildKeepingLast();
        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }

    private record TextureVariantHolder(TextureVariantFactory textureFactory) {}
}
