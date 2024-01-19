package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;

public class VariantCraftingTableBlock extends CraftingTableBlock implements VariantBlock {
    public VariantCraftingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.CRAFTING_TABLE;
    }
}
