package dev.enjarai.projectv;

import dev.enjarai.projectv.block.BlockMaterialGroup;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import dev.enjarai.projectv.resource.TextureVariantFactory;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockVariantTextureGenerator.registerTextureFactory(BlockMaterialGroup.PLANKS, TextureVariantFactory.paletted(new Identifier("material/block/oak_planks")));
	}
}