package dev.enjarai.projectv.block;

import dev.enjarai.projectv.ProjectV;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public final class BlockVariantGenerator {

    // Unused default constructor
    private BlockVariantGenerator() {}

    private static final Map<BlockMaterialGroup, Set<BlockVariantHolder<?, ?>>> HOLDERS = new HashMap<>();
    private static final Map<BlockMaterialGroup, Set<Block>> MATERIALS = new HashMap<>();
    private static boolean hasRegistered = false;

    public static <O extends Block, V extends Block & VariantBlock> void addVariant(O original, VariantBlockFactory<V> factory, BlockMaterialGroup materialGroup) {
        if (hasRegistered) {
            throw new IllegalStateException("Attempting to add a variant when already registered variants");
        }

        HOLDERS.computeIfAbsent(materialGroup, ignored -> new HashSet<>()).add(new BlockVariantHolder<>(original, factory));
    }

    public static void addMaterials(BlockMaterialGroup group, Block... blocks) {
        Collections.addAll(MATERIALS.computeIfAbsent(group, ignored -> new HashSet<>()), blocks);
    }

    @ApiStatus.Internal
    public static void addDefaultVariants() {
        addMaterials(BlockMaterialGroup.PLANKS, Blocks.ACACIA_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BIRCH_PLANKS, Blocks.CHERRY_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.WARPED_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.SPRUCE_PLANKS);

        addVariant(Blocks.CRAFTING_TABLE, VariantCraftingTableBlock::new, BlockMaterialGroup.PLANKS);
    }

    // TODO should ideally run after mod init so all modded blocks are registered as well
    @ApiStatus.Internal
    public static void registerVariants() {
        hasRegistered = true;

        for (var entry : HOLDERS.entrySet()) {
            for (var materialBlock :  MATERIALS.get(entry.getKey())) {
                for (var holder : entry.getValue()) {
                    registerVariant(materialBlock, holder);
                }
            }
        }
    }

    private static void registerVariant(Block materialBlock, BlockVariantHolder<?, ?> holder) {
        var block = holder.factory.create(FabricBlockSettings.copyOf(materialBlock));
        var identifier = ProjectV.constructVariantIdentifier(Registries.BLOCK, holder.original, materialBlock);

        Registry.register(Registries.BLOCK, identifier, block);
        Registry.register(Registries.ITEM, identifier, new BlockItem(block, new FabricItemSettings()));
    }

    @ApiStatus.Internal
    public static void iterateOverVariants(BlockMaterialGroup materialGroup, VariantConsumer consumer) {
        var materialBlocks = MATERIALS.get(materialGroup);
        var holders = HOLDERS.get(materialGroup);

        for (var holder : holders) {
            for (var material : materialBlocks) {
                consumer.consume(holder.original, material);
            }
        }
    }

    @FunctionalInterface
    public interface VariantBlockFactory<V extends Block & VariantBlock> {
        V create(FabricBlockSettings settings);
    }

    @FunctionalInterface
    public interface VariantConsumer {
        void consume(Block baseBlock, Block materialBlock);
    }

    private record BlockVariantHolder<O extends Block, V extends Block & VariantBlock>(O original, VariantBlockFactory<V> factory) {}
}
