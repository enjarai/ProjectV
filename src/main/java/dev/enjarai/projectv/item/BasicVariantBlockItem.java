package dev.enjarai.projectv.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

import java.util.function.Supplier;

public class BasicVariantBlockItem extends BlockItem implements VariantItem {
    private final Supplier<Item> original;

    public BasicVariantBlockItem(Block original, Block block, Settings settings) {
        super(block, settings);
        this.original = original::asItem;
    }

    @Override
    public Item getOriginal() {
        return original.get();
    }
}
