package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.block.BlockVariantGenerator;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates all block variant textures and stores them in its pack.
 * The pack is added to any created client_resource ResourceManagers.
 * <p>
 * TODO implement concurrency here?
 */
public class BlockVariantTextureGenerator extends SinglePreparationResourceReloader<Map<Identifier, byte[]>> {
    public static final RuntimeResourcePack PACK = new RuntimeResourcePack("Project V: Block Variants");
    static {
        PackAdderEvent.EVENT.register((managerType, packs) -> packs.add(PACK));
    }
    private static final HashMap<BlockMaterialGroup, TextureVariantHolder> HOLDERS = new HashMap<>();


    public static void registerTextureFactory(BlockMaterialGroup materialGroup, TextureVariantFactory textureFactory) {
        HOLDERS.put(materialGroup, new TextureVariantHolder(textureFactory));
    }


    @Override
    protected Map<Identifier, byte[]> prepare(ResourceManager manager, Profiler profiler) {
        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        HOLDERS.forEach((materialGroup, holder) -> {
            BlockVariantGenerator.iterateOverVariants(materialGroup, (baseBlock, materialBlock) -> {

                var baseTextureId = Registries.BLOCK.getId(baseBlock).withPrefixedPath("textures/projectv/base_texture/block/");
                var materialTextureId = Registries.BLOCK.getId(materialBlock).withPrefixedPath("textures/projectv/material/block/");

                try {
                    var variantBlockId = ProjectV.constructVariantIdentifier(Registries.BLOCK, baseBlock, materialBlock);

                    var baseTexture = NativeImage.read(manager.getResource(baseTextureId).orElseThrow().getInputStream());
                    var materialTexture = NativeImage.read(manager.getResource(materialTextureId).orElseThrow().getInputStream());

                    var generatedTexture = holder.textureFactory().createVariant(manager, baseTexture, materialTexture);
                    mapBuilder.put(variantBlockId.withPrefixedPath("textures/block/"), generatedTexture.getBytes());
                    // TODO multiple textures per block

                    var variantModelId = variantBlockId.withPrefixedPath("models/block/");
                    var generatedModelJson = Models.CUBE_ALL.createJson(variantModelId, Map.of(TextureKey.ALL, variantBlockId.withPrefixedPath("block/")));
                    mapBuilder.put(variantModelId, generatedModelJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO multiple models, not just cube all

                    var generatedStateJson = VariantsBlockStateSupplier.create(Registries.BLOCK.get(variantBlockId),
                            BlockStateVariant.create().put(VariantSettings.MODEL, variantBlockId.withPrefixedPath("block/"))).get();
                    mapBuilder.put(variantBlockId.withPrefixedPath("blockstates/"), generatedStateJson.toString().getBytes(StandardCharsets.UTF_8));
                    // TODO yea you get it, need to make this dynamic

                } catch (Exception e) {
                    ProjectV.LOGGER.error(String.format("Failed to generate variant %s for %s:", materialTextureId, baseTextureId), e);
                }
            });
        });
        return mapBuilder.buildKeepingLast();
    }

    @Override
    protected void apply(Map<Identifier, byte[]> prepared, ResourceManager manager, Profiler profiler) {
        PACK.clear();
        prepared.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }

    private record TextureVariantHolder(TextureVariantFactory textureFactory) {}
}
