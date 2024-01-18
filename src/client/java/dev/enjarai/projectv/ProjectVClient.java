package dev.enjarai.projectv;

import dev.enjarai.projectv.event.EventManager;
import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.resource.ResourceType;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EventManager.init();
	}
}