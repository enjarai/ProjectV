package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public abstract class ItemMixin {
    @WrapOperation(method = "getDefaultStack", at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack getDefaultVariantStack(ItemConvertible item, Operation<ItemStack> original) {
        ItemStack result = original.call(item);
        if(item instanceof VariantItem) {
            return ((ItemStackExtender) result).projectV$toVariantItemStack();
        }
        return result;
    }
}
