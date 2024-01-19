package dev.enjarai.projectv.mixin.item;

import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    @Shadow public abstract void setStack(ItemStack stack);

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V", shift = At.Shift.AFTER))
    private void convertContainedStack(CallbackInfo ci) {
        if(getStack() instanceof VariantItemStack variantItemStack && variantItemStack.getVariants().isEmpty()) {
            setStack(variantItemStack.toItemStack());
        }
    }

    @Inject(method = "canMerge(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private static void canMerge(ItemStack stack1, ItemStack stack2, CallbackInfoReturnable<Boolean> cir) {
        if(stack1 instanceof VariantItemStack || stack2 instanceof VariantItemStack) {
            cir.setReturnValue(false);
        }
    }
}