package dev.enjarai.projectv.resource;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;

import java.util.List;

public interface PackAdderEvent {
    Event<PackAdderEvent> EVENT = EventFactory.createArrayBacked(PackAdderEvent.class, callbacks -> (type, packs) -> {
        for (var callback : callbacks) {
            callback.addPacks(type, packs);
        }
    });

    void addPacks(ResourceType managerType, List<ResourcePack> packs);
}
