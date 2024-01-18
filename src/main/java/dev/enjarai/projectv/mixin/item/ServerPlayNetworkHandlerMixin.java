package dev.enjarai.projectv.mixin.item;

import dev.enjarai.projectv.extend.ClickSlotC2SPacketExtender;
import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void readLastDraggedSlot(ClickSlotC2SPacket packet, CallbackInfo ci) {
        ThreadLocals.LAST_DRAGGED_SLOT_ID.set(((ClickSlotC2SPacketExtender) packet).projectV$getLastDraggedSlot());
    }
}
