package dev.enjarai.projectv.block;

import net.minecraft.block.Block;

public class BasicVariantBlock extends Block implements VariantBlock {
    private final Block baseBlock;

    public BasicVariantBlock(Settings settings, Block baseBlock) {
        super(settings);
        this.baseBlock = baseBlock;
    }

    @Override
    public Block getBaseBlock() {
        return baseBlock;
    }
}