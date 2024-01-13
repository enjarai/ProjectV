package dev.enjarai.projectv;

import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import dev.enjarai.projectv.resource.TextureVariantFactory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		//ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new BlockVariantTextureGenerator());

		BlockVariantTextureGenerator.registerTextureFactory(BlockMaterialGroup.PLANKS, TextureVariantFactory.dummy(0xff8800));
	}
}