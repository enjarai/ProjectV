package dev.enjarai.projectv.mixin.client.item;

import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void renderId(DrawContext context, Slot slot, CallbackInfo ci) {
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of(String.valueOf(slot.id)), slot.x, slot.y, 0xAAAAAA, true);
    }

    @ModifyArg(method = "mouseDragged", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private <E> E captureLastDraggedSlot(E slot) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player != null && player.isCreative()) {
            ThreadLocals.LAST_DRAGGED_SLOT_ID.set(-2);
        } else {
            ThreadLocals.LAST_DRAGGED_SLOT_ID.set(((Slot) slot).id);
        }
        return slot;
    }
}
