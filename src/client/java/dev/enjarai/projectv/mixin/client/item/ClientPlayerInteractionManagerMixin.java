package dev.enjarai.projectv.mixin.client.item;

import dev.enjarai.projectv.extend.ClickSlotC2SPacketExtender;
import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @ModifyArg(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private Packet<?> attachLastDraggedSlot(Packet<?> packet) {
        ((ClickSlotC2SPacketExtender) packet).projectV$setLastDraggedSlot(ThreadLocals.LAST_DRAGGED_SLOT_ID.get());
        return packet;
    }
}
