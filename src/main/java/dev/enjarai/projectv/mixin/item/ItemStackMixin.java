package dev.enjarai.projectv.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.extend.ItemStackExtender;
import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackExtender {
    @Shadow @Final @Mutable
    private @Nullable Item item;

    @Shadow
    private int count;

    @Override
    public Item projectV$getTrueItem() {
        return item;
    }

    @Override
    public void projectV$setItem(Item item) {
        this.item = item;
    }

    @Override
    public VariantItemStack projectV$toVariantItemStack() {
        if((Object) this instanceof VariantItemStack self) return self;
        if(item == null) return VariantItemStack.EMPTY;

        return new VariantItemStack(item, count);
    }

    @WrapOperation(method = "fromNbt", at = @At(value = "NEW", target = "(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack fromVariantNbt(NbtCompound nbt, Operation<ItemStack> original) {
        if(nbt.contains("Variants")) {
            return VariantItemStack.fromNbt(nbt);
        }
        return original.call(nbt);
    }

    @ModifyReturnValue(method = "areEqual", at = @At("RETURN"))
    private static boolean areVariantStacksEqual(boolean original, ItemStack left, ItemStack right) {
        if(!original) return false;

        if(left instanceof VariantItemStack leftVariant && right instanceof VariantItemStack rightVariant) {
            return leftVariant.equals(rightVariant);
        }
        return true;
    }

    @ModifyExpressionValue(method = "split", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I"))
    private int useTotalCount(int original) {
        if((Object) this instanceof VariantItemStack self) {
            return self.getTotalCount();
        }
        return original;
    }
}
