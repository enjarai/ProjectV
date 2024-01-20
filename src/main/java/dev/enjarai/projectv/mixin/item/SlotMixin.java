package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow public abstract ItemStack getStack();

    @WrapOperation(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int getTotalVariantCount(ItemStack instance, Operation<Integer> original) {
        if(instance instanceof VariantItemStack variantItemStack) {
            return variantItemStack.getTotalCount();
        }
        return original.call(instance);
    }

    @WrapWithCondition(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private boolean dontDecrementAcceptableStack(ItemStack instance, int amount) {
        return !(getStack() instanceof VariantItemStack);
    }

    @WrapOperation(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"))
    private void acceptVariantItemStack(ItemStack instance, int amount, Operation<Void> original, ItemStack stack, int count, @Share("result") LocalRef<ItemStack> result) {
        if(instance instanceof VariantItemStack variantItemStack) {
            result.set(variantItemStack.accept(stack));
        } else {
            original.call(instance, amount);
        }
    }

    @ModifyReturnValue(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private ItemStack returnResult(ItemStack original, @Share("result") LocalRef<ItemStack> result) {
        if(result.get() != null) return result.get();
        return original;
    }
}
