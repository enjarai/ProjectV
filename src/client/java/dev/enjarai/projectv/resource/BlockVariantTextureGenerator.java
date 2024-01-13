package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

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
        PackAdderEvent.EVENT.register((managerType, packs) -> {

            packs.add(PACK)
        });
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

                var baseTextureId = Registries.BLOCK.getId(baseBlock).withPrefixedPath("textures/projectv/base_texture/block/");
                var materialTextureId = Registries.BLOCK.getId(materialBlock).withPrefixedPath("textures/projectv/material/block/");

                try {
                    var variantBlockId = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);

                    // TODO make base textures optional?
//                    var baseTexture = NativeImage.read(manager.getResource(baseTextureId).orElseThrow().getInputStream());
//                    var materialTexture = NativeImage.read(manager.getResource(materialTextureId).orElseThrow().getInputStream());

                    try (var generatedTexture = holder.textureFactory().createVariant(manager, null, null)) {
                        mapBuilder.put(variantBlockId.withPrefixedPath("textures/block/").withSuffixedPath(".png"), generatedTexture.getBytes());
                    }
                    // TODO multiple textures per block

                    var variantModelId = variantBlockId.withPrefixedPath("models/block/");
                    var generatedModelJson = Models.CUBE_ALL.createJson(variantModelId, Map.of(TextureKey.ALL, variantBlockId.withPrefixedPath("block/")));
                    mapBuilder.put(variantModelId.withSuffixedPath(".json"), generatedModelJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO multiple models, not just cube all

                    var variantItemModelId = variantBlockId.withPrefixedPath("models/item");
                    var generatedItemModelJson = new Model(Optional.of(variantModelId), Optional.empty()).createJson(variantItemModelId, Map.of());
                    mapBuilder.put(variantItemModelId.withSuffixedPath(".json"), generatedItemModelJson.toString().getBytes(StandardCharsets.UTF_8));

                    var generatedStateJson = VariantsBlockStateSupplier.create(Registries.BLOCK.get(variantBlockId),
                            BlockStateVariant.create().put(VariantSettings.MODEL, variantBlockId.withPrefixedPath("block/"))).get();
                    mapBuilder.put(variantBlockId.withPrefixedPath("blockstates/").withSuffixedPath(".json"), generatedStateJson.toString().getBytes(StandardCharsets.UTF_8));
                    ProjectV.LOGGER.info(generatedStateJson.toString());
                    // TODO yea you get it, need to make this dynamic

                } catch (Exception e) {
                    ProjectV.LOGGER.error(String.format("Failed to generate variant %s for %s:", materialTextureId, baseTextureId), e);
                }
            });
        });
        var map =  mapBuilder.buildKeepingLast();
        PACK.clear();
        map.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }

    private record TextureVariantHolder(TextureVariantFactory textureFactory) {}
}
