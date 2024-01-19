package dev.enjarai.projectv;

import dev.enjarai.projectv.event.EventManager;
import net.fabricmc.api.ClientModInitializer;

public class ProjectVClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EventManager.init();
	}
}