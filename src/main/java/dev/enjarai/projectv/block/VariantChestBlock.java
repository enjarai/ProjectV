package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntityType;

public class VariantChestBlock extends ChestBlock implements VariantBlock {
    private final Block materialBlock;

    public VariantChestBlock(Settings settings, Block materialBlock, Block original) {
        super(settings, () -> BlockEntityType.CHEST);
        this.materialBlock = materialBlock;
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.CHEST;
    }

    public Block getMaterialBlock() {
        return materialBlock;
    }
}
