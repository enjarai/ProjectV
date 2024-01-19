package dev.enjarai.projectv.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.enjarai.projectv.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

/**
 * General mixin to modify blockentity types to support our variants.
 */
@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Shadow
    @Final
    private Set<Block> blocks;

    @ModifyReturnValue(
            method = "supports",
            at = @At("RETURN")
    )
    private boolean addSupportedBlocks(boolean original, BlockState state) {
        return original || state.getBlock() instanceof VariantBlock variantBlock && this.blocks.contains(variantBlock.getBaseBlock());
    }
}
