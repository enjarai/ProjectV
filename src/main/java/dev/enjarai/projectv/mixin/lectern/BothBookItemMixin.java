package dev.enjarai.projectv.mixin.lectern;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.projectv.block.VariantBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.WritableBookItem;
import net.minecraft.item.WrittenBookItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({WritableBookItem.class, WrittenBookItem.class})
public class BothBookItemMixin {
    @ModifyExpressionValue(
            method = "useOnBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
            )
    )
    private boolean allowCustomLecterns(boolean original, @Local BlockState blockState) {
        return original
                || blockState.getBlock() instanceof VariantBlock variantBlock
                && variantBlock.getBaseBlock() == Blocks.LECTERN;
    }
}
