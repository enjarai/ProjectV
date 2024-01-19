package dev.enjarai.projectv.mixin;

import dev.enjarai.projectv.block.VariantBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * General mixin to modify blockentity types to support our variants.
 */
@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin {
    @Shadow
    @Final
    private Set<Block> blocks;

    @Inject(
            method = "supports",
            at = @At("HEAD"),
            cancellable = true
    )
    private void addSupportedBlocks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof VariantBlock variantBlock && blocks.contains(variantBlock.getBaseBlock())) {
            cir.setReturnValue(true);
        }
    }
}
