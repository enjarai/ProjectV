package dev.enjarai.projectv;

import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import dev.enjarai.projectv.resource.TextureVariantFactory;
import net.fabricmc.api.ClientModInitializer;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockVariantTextureGenerator.registerTextureFactory(BlockMaterialGroup.PLANKS, TextureVariantFactory.dummy(0xffff8800));
	}
}