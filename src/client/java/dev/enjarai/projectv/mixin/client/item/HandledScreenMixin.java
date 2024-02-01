package dev.enjarai.projectv.mixin.client.item;

import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Debug(export = true)
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @ModifyArg(method = "mouseDragged", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z", remap = false))
    private <E> E captureLastDraggedSlot(E slot) {
        ThreadLocals.LAST_DRAGGED_SLOT_INDEX.set(((Slot) slot).getIndex());
        return slot;
    }
}
