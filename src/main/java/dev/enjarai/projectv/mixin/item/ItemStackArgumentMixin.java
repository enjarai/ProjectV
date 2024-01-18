package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItem;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStackArgument.class)
public abstract class ItemStackArgumentMixin {
    @WrapOperation(method = "createStack", at = @At(value = "NEW", target = "(Lnet/minecraft/registry/entry/RegistryEntry;I)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack createVariantStack(RegistryEntry<Item> itemEntry, int count, Operation<ItemStack> original) {
        ItemStack result = original.call(itemEntry, count);
        if(itemEntry.value() instanceof VariantItem) {
            return ((ItemStackExtender) result).projectV$toVariantItemStack();
        }
        return result;
    }
}
