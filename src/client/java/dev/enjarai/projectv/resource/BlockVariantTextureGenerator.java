package dev.enjarai.projectv.resource;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

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



    @Override
    protected Map<Identifier, byte[]> prepare(ResourceManager manager, Profiler profiler) {
        var mapBuilder = ImmutableMap.<Identifier, byte[]>builder();
        // TODO
        return mapBuilder.buildKeepingLast();
    }

    @Override
    protected void apply(Map<Identifier, byte[]> prepared, ResourceManager manager, Profiler profiler) {
        PACK.clear();
        prepared.forEach((key, value) -> PACK.addFileContents(ResourceType.CLIENT_RESOURCES, key, value));
    }
}
