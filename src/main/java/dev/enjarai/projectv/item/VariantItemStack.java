package dev.enjarai.projectv.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.extend.ItemStackExtender;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class VariantItemStack extends ItemStack {
    public static final Codec<VariantItemStack> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Registries.ITEM.getCodec().fieldOf("id").forGetter(VariantItemStack::getItem),
                    Codec.INT.fieldOf("Count").forGetter(ItemStack::getCount),
                    NbtCompound.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.getNbt())),
                    Codec.list(ItemStack.CODEC).fieldOf("Variants").forGetter(VariantItemStack::getVariants)
            ).apply(instance, VariantItemStack::new)
    );

    public static final VariantItemStack EMPTY = new VariantItemStack((Void) null);

    /**
     * Contains the variants contained with in the stack<br>
     * The {@link ItemStack#item} is the currently selected variant<br>
     * The {@link ItemStack#count} is the amount of the currently selected variant
     */
    @SuppressWarnings("JavadocReference")
    private final VariantList variants;

    public VariantItemStack(ItemConvertible item) {
        this(item, 1);
    }

    public VariantItemStack(RegistryEntry<Item> entry) {
        this(entry, 1);
    }

    public VariantItemStack(RegistryEntry<Item> itemEntry, int count) {
        this(itemEntry.value(), count);
    }

    public VariantItemStack(ItemConvertible item, int count) {
        super(item, count);
        variants = new VariantList();
        validate();
    }

    private VariantItemStack(ItemConvertible item, Integer count, Optional<NbtCompound> nbt, List<ItemStack> variants) {
        super(item, count, nbt);
        this.variants = VariantList.of(variants);
        validate();
    }

    private VariantItemStack(Void void_) {
        super(void_ );
        variants = new VariantList();
    }

    private VariantItemStack(NbtCompound nbt) {
        super(nbt);
        variants = new VariantList();
        NbtList variantsNbt = nbt.getList("Variants", 10);
        variantsNbt.forEach((variantNbt) -> variants.add(ItemStack.fromNbt((NbtCompound) variantNbt)));
        validate();
    }

    public ItemStack toItemStack() {
        return new ItemStack(getItem(), getCount());
    }

    private void validate() {
        if(isEmpty()) return;
        if(!(getItem() instanceof VariantItem)) {
            throw new IllegalStateException("VariantItemStack must be a VariantItem");
        }
    }

    private boolean sameItem(ItemStack other) {
        return ItemStackExtender.sameItem(this, other);
    }

    public static VariantItemStack fromNbt(NbtCompound nbt) {
        try {
            return new VariantItemStack(nbt);
        } catch (RuntimeException e) {
            ProjectV.LOGGER.debug("Tried to load invalid variant item: {}", nbt, e);
            return VariantItemStack.EMPTY;
        }
    }

    @Override
    public boolean isEmpty() {
        return this == EMPTY || super.isEmpty();
    }

    @Override
    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            VariantItemStack itemStack = ((ItemStackExtender) super.copy()).projectV$toVariantItemStack();
            itemStack.variants.addAll(variants);
            return itemStack;
        }
    }

    @Override
    public ItemStack copyWithCount(int count) {
        return super.copyWithCount(Math.min(count, getCount()));
    }

    @Override
    public ItemStack split(int amount) {
        int mainCount = getCount();
        int totalCount = getTotalCount();
        VariantItemStack split = (VariantItemStack) super.split(amount);
        int splitCount = split.getTotalCount();
        if(!isEmpty() || mainCount == amount) {
            split.clearVariants();
        }
        if (isEmpty() && !variants.isEmpty() && !variants.equals(split.variants) && splitCount == totalCount) {
            ItemStack nextStack = variants.next();
            if(!nextStack.isEmpty()) {
                setItem(nextStack.getItem());
                setCount(nextStack.getCount());
            }
        }
        return split;
    }

    public VariantItem getVariantItem() {
        return (VariantItem) getItem();
    }

    public Item getOriginal() {
        return getVariantItem().getOriginal();
    }

    public List<ItemStack> getVariants() {
        return variants;
    }

    public void clearVariants() {
        variants.clear();
    }

    public int getTotalCount() {
        return variants.getTotalCount() + getCount();
    }

    @Override
    public List<Text> getTooltip(@Nullable PlayerEntity player, TooltipContext context) {
        List<Text> tooltip = super.getTooltip(player, context);
        if(isEmpty()) return tooltip;

        tooltip.add(Text.of("Variants:"));
        for (ItemStack variant : variants) {
            tooltip.add(Text.of(Registries.ITEM.getId(variant.getItem()) + " " + variant.getCount()));
        }
        return tooltip;
    }

    @Override
    public boolean isOf(Item item) {
        if(super.isOf(item)) return true;
        if(this.isEmpty()) return item == Items.AIR;
        if(!(item instanceof VariantItem variantItem)) return false;

        return variantItem.getOriginal() == getOriginal();
    }

    /**
     * Accepts an item stack and if it is a variant of the original item, it will add it to the variants list<br>
     * if the item is the original item, it will add it to the count of the original item
     * @return the remaining amount of the item stack that was not accepted
     */
    public ItemStack accept(ItemStack stack) {
        if(stack instanceof VariantItemStack variantItemStack) {
            return accept(variantItemStack);
        }

        int inCount = stack.getCount();
        int maxCount = getMaxCount();
        int totalCount = getTotalCount();
        int space = maxCount - totalCount;
        if(space == 0) return stack;

        Item stackItem = stack.getItem();
        if(!(stackItem instanceof VariantItem variantItem)) return stack;
        if(!isEmpty() && variantItem.getOriginal() != getOriginal()) return stack;

        if (stackItem == getItem() || isEmpty()) {
            if (space >= inCount) {
                increment(inCount);
                return ItemStack.EMPTY;
            } else {
                increment(space);
                return stack.split(inCount - space);
            }
        } else {
            ItemStack existing = variants.getVariant(stackItem);
            if(existing == null) {
                if (space >= inCount) {
                    variants.add(stack.copy());
                    return ItemStack.EMPTY;
                } else {
                    variants.add(stack.split(inCount - space));
                    return stack;
                }
            } else {
                if (space >= inCount) {
                    existing.increment(inCount);
                    return ItemStack.EMPTY;
                } else {
                    existing.increment(space);
                    return stack.split(inCount - space);
                }
            }
        }
    }

    public ItemStack accept(VariantItemStack variantItemStack) {
        List<ItemStack> variants = variantItemStack.getVariants();
        VariantItemStack result = ((ItemStackExtender) accept(variantItemStack.getItem(), variantItemStack.getCount())).projectV$toVariantItemStack();
        if(variants.isEmpty()) return result;

        List<ItemStack> acceptResults = new ArrayList<>();
        for (ItemStack variant : variants) {
            acceptResults.add(accept(variant.copy()));
        }
        acceptResults.removeIf(ItemStack::isEmpty);
        result.getVariants().addAll(acceptResults);
        return result;
    }

    public ItemStack accept(Item item, int count) {
        return accept(new ItemStack(item, count));
    }

    public void setItem(Item item) {
        if(item == getItem()) return;
        if(!(item instanceof VariantItem variantItem)) {
            throw new IllegalArgumentException("Item must be a VariantItem");
        }
        if(!isEmpty() && variantItem.getOriginal() != getOriginal()) {
            throw new IllegalArgumentException("Item must be a variant of the original item");
        }
        ((ItemStackExtender) this).projectV$setItem(item);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList variantsNbt = new NbtList();
        variants.forEach((variant) -> variantsNbt.add(variant.writeNbt(new NbtCompound())));
        nbt.put("Variants", variantsNbt);
        return super.writeNbt(nbt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VariantItemStack other)) return false;
        if (!sameItem(other)) return false;
        return variants.equals(other.variants);
    }

    /**
     * List that only accepts {@link ItemStack}s that are not {@link VariantItemStack}s<br>
     * To add a {@link VariantItemStack} to this list, deconstruct it to it's variants and add them individually
     */
    static class VariantList extends ArrayList<ItemStack> {
        private static final IllegalArgumentException EXCEPTION = new IllegalArgumentException("VariantItemStacks are not allowed");

        public static VariantList of(List<ItemStack> variants) {
            VariantList list = new VariantList();
            list.addAll(variants);
            return list;
        }

        public int getTotalCount() {
            return stream().mapToInt(ItemStack::getCount).sum();
        }

        public ItemStack getVariant(Item item) {
            return stream().filter((stack) -> stack.getItem() == item).findFirst().orElse(null);
        }

        public ItemStack next() {
            if(isEmpty()) return ItemStack.EMPTY;
            return remove(0);
        }

        @Override
        public boolean add(ItemStack itemStack) {
            validate(itemStack);
            return super.add(itemStack);
        }

        @Override
        public void add(int index, ItemStack element) {
            validate(element);
            super.add(index, element);
        }

        @Override
        public boolean addAll(Collection<? extends ItemStack> c) {
            for(ItemStack itemStack : c) {
                validate(itemStack);
            }
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends ItemStack> c) {
            for(ItemStack itemStack : c) {
                validate(itemStack);
            }
            return super.addAll(index, c);
        }

        @Override
        public ItemStack set(int index, ItemStack element) {
            validate(element);
            return super.set(index, element);
        }

        private static void validate(ItemStack itemStack) {
            if(itemStack instanceof VariantItemStack) {
                throw EXCEPTION;
            }
        }
    }
}
