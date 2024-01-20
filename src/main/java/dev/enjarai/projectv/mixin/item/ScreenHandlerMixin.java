package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.enjarai.projectv.item.VariantItemStack;
import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Debug(export = true)
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow public abstract ItemStack getCursorStack();

    @Shadow @Final public DefaultedList<Slot> slots;

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
        if (currentSlot.id != lastDraggedSlot && lastDraggedSlot != -2) { // -2 indicates stacks were dragged by a creative player
            variantItemStack.clearVariants();
        }
        return variantItemStack;
    }

    @ModifyArg(
            method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setCount(I)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/screen/ScreenHandler;calculateStackSize(Ljava/util/Set;ILnet/minecraft/item/ItemStack;)I"
                    )
            )
    )
    private int postDragSetCursor(int count) {
        if (count == 0) return count;

        // clear variants of the last dragged stack as the cursor stack is not empty
        int lastDraggedSlot = ThreadLocals.LAST_DRAGGED_SLOT_ID.get();
        if (lastDraggedSlot == -2) return count;

        for (Slot slot : slots) {
            if (slot.id != lastDraggedSlot) continue;

            ItemStack stack = slot.getStack();
            if (stack instanceof VariantItemStack variantItemStack) {
                variantItemStack.clearVariants();
            }
            break;
        }
        return count;
    }

    @WrapOperation(
            method = "internalOnSlotClick",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;getCount()I",
                            ordinal = 0
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;getCount()I",
                            ordinal = 1
                    )
            },
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            opcode = Opcodes.GETSTATIC,
                            target = "Lnet/minecraft/util/ClickType;LEFT:Lnet/minecraft/util/ClickType;"
                    )
            )
    )
    private int getTotalVariantCount(ItemStack instance, Operation<Integer> original) {
        if(instance instanceof VariantItemStack variantItemStack) {
            return variantItemStack.getTotalCount();
        }
        return original.call(instance);
    }

    @WrapOperation(
            method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/screen/slot/Slot;takeStackRange(IILnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/item/ItemStack;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            opcode = Opcodes.GETSTATIC,
                            target = "Lnet/minecraft/screen/slot/SlotActionType;PICKUP_ALL:Lnet/minecraft/screen/slot/SlotActionType;"
                    )
            )
    )
    private ItemStack adjustStackRangeForVariantItemStack(Slot instance, int min, int max, PlayerEntity player, Operation<ItemStack> original, @Share("taken")LocalRef<ItemStack> taken) {
        ItemStack cursorStack = getCursorStack();
        ItemStack result;
        if(cursorStack instanceof VariantItemStack variantItemStack) {
            result = original.call(instance, min, variantItemStack.getMaxCount() - variantItemStack.getTotalCount(), player);
        } else {
            result = original.call(instance, min, max, player);
        }
        taken.set(result);
        return result;
    }

    @WrapOperation(
            method = "internalOnSlotClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;increment(I)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "FIELD",
                            opcode = Opcodes.GETSTATIC,
                            target = "Lnet/minecraft/screen/slot/SlotActionType;PICKUP_ALL:Lnet/minecraft/screen/slot/SlotActionType;"
                    )
            )
    )
    private void acceptVariantItemStack(ItemStack instance, int amount, Operation<Void> original, @Share("taken") LocalRef<ItemStack> taken) {
        if(instance instanceof VariantItemStack variantItemStack) {
            variantItemStack.accept(taken.get());
        } else {
            original.call(instance, amount);
        }
    }
}
