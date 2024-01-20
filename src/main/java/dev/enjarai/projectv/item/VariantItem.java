package dev.enjarai.projectv.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

public interface VariantItem extends ItemConvertible {
    Item getOriginal();
}
