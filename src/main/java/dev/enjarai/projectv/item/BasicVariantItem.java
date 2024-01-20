package dev.enjarai.projectv.item;

import net.minecraft.item.Item;

public class BasicVariantItem extends Item implements VariantItem {
    private final Item original;

    public BasicVariantItem(Item original, Settings settings) {
        super(settings);
        this.original = original;
    }

    @Override
    public Item getOriginal() {
        return original;
    }
}
