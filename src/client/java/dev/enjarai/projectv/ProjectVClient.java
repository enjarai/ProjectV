package dev.enjarai.projectv;

import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.ResourceType;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PackAdderEvent.EVENT.register((managerType, packs) -> {
			if (managerType == ResourceType.CLIENT_RESOURCES) {
				packs.add(BlockVariantTextureGenerator.PACK);
			}
		});
	}
}