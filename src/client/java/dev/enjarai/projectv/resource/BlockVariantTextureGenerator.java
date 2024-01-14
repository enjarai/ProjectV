package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.pack.RuntimePack;
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
    public static final RuntimePack PACK = RuntimePack.create("Project V: Block Variants", ResourceType.CLIENT_RESOURCES);
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

                var baseTextureId = Registries.BLOCK.getId(baseBlock).withPrefixedPath("projectv/base_texture/block/").withSuffixedPath(".png");
                var materialTextureId = Registries.BLOCK.getId(materialBlock).withPrefixedPath("projectv/material/block/").withSuffixedPath(".png");

                try {
                    var variantBlockId = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);

                    try (var generatedTexture = holder.textureFactory().createVariant(manager, baseTextureId, materialTextureId)) {
                        mapBuilder.put(variantBlockId.withPrefixedPath("textures/block/").withSuffixedPath(".png"), generatedTexture.getBytes());
                    }
                    // TODO multiple textures per block

                    var variantModelId = variantBlockId.withPrefixedPath("models/block/");
                    var generatedModelJson = Models.CUBE_ALL.createJson(variantModelId, Map.of(TextureKey.ALL, variantBlockId.withPrefixedPath("block/")));
                    mapBuilder.put(variantModelId.withSuffixedPath(".json"), generatedModelJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO multiple models, not just cube all

                    var variantItemModelId = variantBlockId.withPrefixedPath("models/item/");
                    var generatedItemModelJson = new Model(Optional.of(variantBlockId.withPrefixedPath("block/")), Optional.empty()).createJson(variantItemModelId, Map.of());
                    mapBuilder.put(variantItemModelId.withSuffixedPath(".json"), generatedItemModelJson.toString().getBytes(StandardCharsets.UTF_8));
                    System.out.println(generatedItemModelJson);

                    var generatedStateJson = VariantsBlockStateSupplier.create(Registries.BLOCK.get(variantBlockId),
                            BlockStateVariant.create().put(VariantSettings.MODEL, variantBlockId.withPrefixedPath("block/"))).get();
                    mapBuilder.put(variantBlockId.withPrefixedPath("blockstates/").withSuffixedPath(".json"), generatedStateJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO yea you get it, need to make this dynamic

                } catch (Exception e) {
                    ProjectV.LOGGER.error(String.format("Failed to apply variant %s to %s:", materialTextureId, baseTextureId), e);
                }
            });
        });
        var map =  mapBuilder.buildKeepingLast();
        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }

    private record TextureVariantHolder(TextureVariantFactory textureFactory) {}
}
