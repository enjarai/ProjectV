package dev.enjarai.projectv;

import dev.enjarai.projectv.block.VariantBlock;
import dev.enjarai.projectv.event.EventManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.registry.Registries;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EventManager.init();

		Registries.BLOCK.forEach(block -> {
			if (block instanceof VariantBlock variantBlock) {
				BlockRenderLayerMap.INSTANCE.putBlock(block,
						RenderLayers.getBlockLayer(variantBlock.getBaseBlock().getDefaultState()));
			}
		});
	}
}