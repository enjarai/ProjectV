package dev.enjarai.projectv.extend;

import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemStackExtender {
    /**
     * @return the true item of this stack, even if the count is 0
     */
    Item projectV$getTrueItem();

    void projectV$setItem(Item item);

    VariantItemStack projectV$toVariantItemStack();

    /**
     * @return true if the true item of the two stacks are the same, regardless of count
     */
    static boolean sameItem(ItemStack stack1, ItemStack stack2) {
        return ((ItemStackExtender) stack1).projectV$getTrueItem() == ((ItemStackExtender) stack2).projectV$getTrueItem();
    }

}
