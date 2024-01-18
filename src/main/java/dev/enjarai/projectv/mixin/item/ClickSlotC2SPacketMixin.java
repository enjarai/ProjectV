package dev.enjarai.projectv.mixin.item;

import dev.enjarai.projectv.extend.ClickSlotC2SPacketExtender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClickSlotC2SPacket.class)
public abstract class ClickSlotC2SPacketMixin implements ClickSlotC2SPacketExtender {
    @Unique
    private int projectV$lastDraggedSlot;

    @Override
    public void projectV$setLastDraggedSlot(int slot) {
        this.projectV$lastDraggedSlot = slot;
    }

    @Override
    public int projectV$getLastDraggedSlot() {
        return this.projectV$lastDraggedSlot;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void readLastDraggedSlot(PacketByteBuf buf, CallbackInfo ci) {
        this.projectV$lastDraggedSlot = buf.readShort();
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void writeLastDraggedSlot(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeShort(this.projectV$lastDraggedSlot);
    }
}
