package dev.enjarai.projectv;

import dev.enjarai.projectv.block.VariantBlock;
import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PackAdderEvent.EVENT.register((managerType, packs) -> {
			if (managerType == ResourceType.CLIENT_RESOURCES) {
				packs.add(BlockVariantTextureGenerator.PACK);
			}
		});

		Registries.BLOCK.forEach(block -> {
			if (block instanceof VariantBlock variantBlock) {
				BlockRenderLayerMap.INSTANCE.putBlock(block,
						RenderLayers.getBlockLayer(variantBlock.getBaseBlock().getDefaultState()));
			}
		});
	}
}