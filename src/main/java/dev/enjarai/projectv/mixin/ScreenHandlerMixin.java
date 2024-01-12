package dev.enjarai.projectv.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.enjarai.projectv.block.VariantCraftingTableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @WrapOperation(
            method = "method_17696",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
            )
    )
    private static boolean allowVariantCraftingTablesToBeUsed(BlockState instance, Block expectedBlock, Operation<Boolean> original) {
        return original.call(instance, expectedBlock) || (expectedBlock == Blocks.CRAFTING_TABLE && instance.getBlock() instanceof VariantCraftingTableBlock);
    }

}
