package dev.enjarai.projectv.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.enjarai.projectv.block.VariantBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockMixin {
    @Shadow public abstract Block getBlock();

    @ModifyReturnValue(
            method = "isOf",
            at = @At("RETURN")
    )
    public boolean isOfVariantAware(boolean original, Block block) {
        return original || isVariantOf(block);
    }

    private boolean isVariantOf(Block block) {
        Block block1 = block;
        if(block1 instanceof VariantBlock variantBlock) {
            block1 = variantBlock.getBaseBlock();
        }
        Block block2 = this.getBlock();
        if(block2 instanceof VariantBlock variantBlock) {
            block2 = variantBlock.getBaseBlock();
        }
        return block1 == block2;
    }
}
