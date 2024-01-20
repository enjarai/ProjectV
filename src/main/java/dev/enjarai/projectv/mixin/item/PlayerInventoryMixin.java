package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItem;
import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Unique
    private final ThreadLocal<ItemStack> remainder = ThreadLocal.withInitial(() -> null);

    @WrapOperation(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemConvertible;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack createVariantStack(ItemConvertible item, int count, Operation<ItemStack> original) {
        ItemStack result = original.call(item, count);
        if(item instanceof VariantItem) {
            return ((ItemStackExtender) result).projectV$toVariantItemStack();
        }
        return result;
    }

    @WrapOperation(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;increment(I)V"))
    private void acceptVariantItem(ItemStack instance, int amount, Operation<Void> original, int slot, ItemStack adding) {
        if(instance instanceof VariantItemStack variantItemStack) {
            remainder.set(variantItemStack.accept(adding));
        } else {
            original.call(instance, amount);
        }
    }

    @ModifyReturnValue(method = "addStack(ILnet/minecraft/item/ItemStack;)I", at = @At("TAIL"))
    private int getRemainder(int original) {
        ItemStack remainder = this.remainder.get();
        if(remainder == null) {
            return original;
        }
        this.remainder.remove();
        return remainder.getCount();
    }

    @ModifyExpressionValue(method = "canStackAddMore", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int getTotalCount(int original, ItemStack existing, ItemStack adding) {
        if(existing instanceof VariantItemStack variantItemStack) {
            return variantItemStack.getTotalCount();
        }
        return original;
    }
}
