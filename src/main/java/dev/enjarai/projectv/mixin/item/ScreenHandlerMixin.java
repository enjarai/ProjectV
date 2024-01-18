package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.projectv.item.VariantItemStack;
import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @ModifyVariable(method = "setCursorStack", at = @At("HEAD"), argsOnly = true)
    private ItemStack limitMaxCount(ItemStack stack) {
        if(!(stack instanceof VariantItemStack variantItemStack)) return stack;

        int shownCount = variantItemStack.getCount();
        int variantCount = variantItemStack.getTotalCount() - shownCount;
        int maxCount = variantItemStack.getMaxCount();
        if(shownCount + variantCount > maxCount) {
            variantItemStack.setCount(maxCount - variantCount);
        }
        return variantItemStack;
    }

    @ModifyArg(
            method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;setStack(Lnet/minecraft/item/ItemStack;)V",
                    ordinal = 0
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/screen/ScreenHandler;calculateStackSize(Ljava/util/Set;ILnet/minecraft/item/ItemStack;)I"
                    )
            )
    )
    private ItemStack clearVariantsOfQuickDraggedItemStack(ItemStack original, @Local Slot currentSlot) {
        if(!(original instanceof VariantItemStack variantItemStack)) return original;

        Integer lastDraggedSlot = ThreadLocals.LAST_DRAGGED_SLOT_ID.get();
        if(currentSlot.id != lastDraggedSlot && lastDraggedSlot != -2) { // -2 indicates stacks were dragged by a creative player
            variantItemStack.clearVariants();
        }
        return variantItemStack;
    }
}
