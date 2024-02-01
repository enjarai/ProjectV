package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItemStack;
import dev.enjarai.projectv.util.ThreadLocals;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;


@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow public abstract ItemStack getCursorStack();

    @Shadow @Final public DefaultedList<Slot> slots;

    @Shadow @Final private static Logger LOGGER;

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

        Integer lastDraggedSlot = ThreadLocals.LAST_DRAGGED_SLOT_INDEX.get();
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
        int lastDraggedSlotIndex = ThreadLocals.LAST_DRAGGED_SLOT_INDEX.get();
        for (Slot slot : slots) {
            if (slot.getIndex() != lastDraggedSlotIndex) continue;

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
                            target = "Lnet/minecraft/util/ClickType;LEFT:Lnet/minecraft/util/ClickType;",
                            ordinal = 0
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
    private ItemStack variantItemStackPickupAll(Slot instance, int min, int max, PlayerEntity player, Operation<ItemStack> original) {
        ItemStack stackInOtherSlot = instance.getStack();
        ItemStack stackInCursor = getCursorStack();
        if(stackInOtherSlot instanceof VariantItemStack variantStackInOtherSlot && stackInCursor instanceof VariantItemStack variantStackInCursor) {
            ItemStack taken = instance.takeStackRange(variantStackInOtherSlot.getTotalCount(), variantStackInCursor.getMaxCount() - variantStackInCursor.getTotalCount(), player);
            ItemStack remainder = variantStackInCursor.accept(taken);
            if(!remainder.isEmpty()) instance.setStack(remainder);
            return ItemStack.EMPTY;
        }
        return original.call(instance, min, max, player);
    }

    @WrapWithCondition(
            method = "insertItem",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;setCount(I)V",
                            ordinal = 0
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;decrement(I)V"
                    )
            },
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;canCombine(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
                    )
            )
    )
    private boolean dontDecrementVariantItemStack(ItemStack instance, int amount) {
        return !(instance instanceof VariantItemStack);
    }

    @WrapOperation(
            method = "insertItem",
            at = {
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;setCount(I)V",
                            ordinal = 1
                    ),
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;setCount(I)V",
                            ordinal = 2
                    )
            },
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/item/ItemStack;canCombine(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"
                    )
            )
    )
    private void acceptVariantItemStackOnInsert(ItemStack instance, int amount, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) ItemStack insertingStack) {
        if (instance instanceof VariantItemStack variantStack) {
            ItemStack reminader = variantStack.accept(insertingStack);
            if(insertingStack instanceof VariantItemStack insertingVariantStack) {
                insertingVariantStack.copyFrom(reminader);
            } else {
                insertingStack.setCount(reminader.getCount());
                ((ItemStackExtender) insertingStack).projectV$setItem(reminader.getItem());
            }
        } else {
            original.call(instance, amount);
        }
    }

    @WrapOperation(method = "insertItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int getVariantCount(ItemStack instance, Operation<Integer> original) {
        if(instance instanceof VariantItemStack variantItemStack) {
            return variantItemStack.getTotalCount();
        }
        return original.call(instance);
    }
}
