package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntityType;

public class VariantChestBlock extends ChestBlock implements VariantBlock {
    public VariantChestBlock(Settings settings) {
        super(settings, () -> BlockEntityType.CHEST);
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.CHEST;
    }
}
