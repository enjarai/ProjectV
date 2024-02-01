package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @WrapOperation(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private void decrementAndGoNextIfVariantStack(ItemStack instance, int amount, Operation<Void> original) {
        original.call(instance, amount);
        if(instance instanceof VariantItemStack variantItemStack && variantItemStack.isEmpty()) {
            variantItemStack.next();
        }
    }
}
