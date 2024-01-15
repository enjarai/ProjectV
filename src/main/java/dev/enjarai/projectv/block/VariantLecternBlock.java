package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;

public class VariantLecternBlock extends LecternBlock implements VariantBlock {
    public VariantLecternBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.LECTERN;
    }
}
