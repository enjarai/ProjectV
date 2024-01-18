package dev.enjarai.projectv.event;

import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItem;
import dev.enjarai.projectv.pack.PackAdderEvent;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockApplyCallback;
import net.minecraft.resource.ResourceType;

public class EventManager {
    public static void init() {
        registerPackAdderEvent();
        registerPickBlockEvent();
    }

    private static void registerPackAdderEvent() {
        PackAdderEvent.EVENT.register((managerType, packs) -> {
            if (managerType == ResourceType.CLIENT_RESOURCES) {
                packs.add(BlockVariantTextureGenerator.PACK);
            }
        });
    }

    private static void registerPickBlockEvent() {
        ClientPickBlockApplyCallback.EVENT.register((player, result, stack) -> {
            if(stack.getItem() instanceof VariantItem) {
                return ((ItemStackExtender) stack).projectV$toVariantItemStack();
            }
            return stack;
        });
    }
}
