package dev.enjarai.projectv.block;

import net.minecraft.block.Block;

public class BasicVariantBlock extends Block implements VariantBlock {
    private final Block materialBlock;
    private final Block baseBlock;

    public BasicVariantBlock(Settings settings, Block materialBlock, Block baseBlock) {
        super(settings);
        this.materialBlock = materialBlock;
        this.baseBlock = baseBlock;
    }

    @Override
    public Block getBaseBlock() {
        return baseBlock;
    }
}
