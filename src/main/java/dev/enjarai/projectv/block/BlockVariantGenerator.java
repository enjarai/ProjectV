package dev.enjarai.projectv.block;

import dev.enjarai.projectv.ProjectV;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

import static net.minecraft.registry.tag.BlockTags.WALL_POST_OVERRIDE;

public final class BlockVariantGenerator {

    // Unused default constructor
    private BlockVariantGenerator() {}

    private static final Map<BlockMaterialGroup, Set<BlockVariantHolder<?, ?>>> HOLDERS = new HashMap<>();
    private static final Map<BlockMaterialGroup, Set<Block>> MATERIALS = new HashMap<>();
    private static final Map<TagKey<Block>, Set<Block>> BLOCK_TAGS = new HashMap<>();
    private static boolean hasRegistered = false;

    @SafeVarargs
    public static <O extends Block, V extends Block & VariantBlock> void addVariant(O original, VariantBlockFactory<V> factory, BlockMaterialGroup materialGroup, TagKey<Block>... tags) {
        if (hasRegistered) {
            throw new IllegalStateException("Attempting to add a variant when already registered variants");
        }

        HOLDERS.computeIfAbsent(materialGroup, ignored -> new HashSet<>()).add(new BlockVariantHolder<>(original, factory, tags));
    }

    @SafeVarargs
    public static <O extends Block, V extends Block & VariantBlock> void addVariant(O original, ExtendedVariantBlockFactory<O, V> factory, BlockMaterialGroup materialGroup, TagKey<Block>... tags) {
        addVariant(original, settings -> factory.create(settings, original), materialGroup, tags);
    }

    public static void addMaterials(BlockMaterialGroup group, Block... blocks) {
        Collections.addAll(MATERIALS.computeIfAbsent(group, ignored -> new HashSet<>()), blocks);
    }

    @ApiStatus.Internal
    public static void addDefaultVariants() {
        addMaterials(BlockMaterialGroup.PLANKS, Blocks.ACACIA_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.BIRCH_PLANKS, Blocks.CHERRY_PLANKS, Blocks.BAMBOO_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.CRIMSON_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.WARPED_PLANKS, Blocks.MANGROVE_PLANKS, Blocks.SPRUCE_PLANKS);
        addMaterials(BlockMaterialGroup.STONE, Blocks.STONE, Blocks.BLACKSTONE, Blocks.DEEPSLATE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.NETHERRACK, Blocks.END_STONE, Blocks.TUFF, Blocks.BASALT); // TODO prolly more?

        addVariant(Blocks.CRAFTING_TABLE, VariantCraftingTableBlock::new, BlockMaterialGroup.PLANKS,
                getTag(new Identifier("c", "workbench")),
                getTag(new Identifier("c", "crafting_tables")),
                getTag(new Identifier("minecraft", "mineable/axe")));
        addVariant(Blocks.LECTERN, VariantLecternBlock::new, BlockMaterialGroup.PLANKS,
                getTag(new Identifier("c", "lecterns")),
                getTag(new Identifier("minecraft", "mineable/axe")));
        addVariant(Blocks.TORCH, settings -> new VariantTorchBlock(settings, ParticleTypes.FLAME), BlockMaterialGroup.PLANKS,
                WALL_POST_OVERRIDE,
                //TODO: Does this exist?
                getTag(new Identifier("c", "torches")));

        addVariant(Blocks.DIAMOND_ORE, BasicVariantBlock::new, BlockMaterialGroup.STONE,
                getTag(new Identifier("minecraft", "diamond_ores")),
                getTag(new Identifier("c", "ores")),
                getTag(new Identifier("minecraft", "mineable/pickaxe")));
    }

    private static TagKey<Block> getTag(Identifier id) {
        return TagKey.of(RegistryKeys.BLOCK, id);
    }

    // TODO should ideally run after mod init so all modded blocks are registered as well
    @ApiStatus.Internal
    public static void registerVariants() {
        hasRegistered = true;

        for (var entry : HOLDERS.entrySet()) {
            for (var materialBlock : MATERIALS.get(entry.getKey())) {
                for (var holder : entry.getValue()) {
                    registerVariant(materialBlock, holder);
                }
            }
        }
    }

    private static void registerVariant(Block materialBlock, BlockVariantHolder<?, ?> holder) {
        var block = holder.factory.create(
                FabricBlockSettings.copyOf(holder.original).sounds(materialBlock.getSoundGroup(materialBlock.getDefaultState()))
        );
        var identifier = ProjectV.constructVariantIdentifier(Registries.BLOCK, holder.original, materialBlock);

        for (var tag : holder.tags) {
            BLOCK_TAGS.computeIfAbsent(tag, ignored -> new HashSet<>()).add(block);
        }

        Registry.register(Registries.BLOCK, identifier, block);
        Registry.register(Registries.ITEM, identifier, new BlockItem(block, new FabricItemSettings()));

        for (var poiTypeKey : block.getPoiTypes()) {
            var poiTypeEntry = Registries.POINT_OF_INTEREST_TYPE.getEntry(poiTypeKey)
                    .orElseThrow(() -> new IllegalArgumentException("Incorrect POI type registry key given by variant block: " + identifier));
            var poiType = poiTypeEntry.value();

            // Get all possible blockstates for our block
            var allStates = block.getStateManager().getStates();

            // Carefully modify a record field to add our states :trolley:
            var poiStates = new HashSet<>(poiType.blockStates);
            poiStates.addAll(allStates);
            poiType.blockStates = poiStates;

            // Add the same states to the poitypes reverse lookup hashmap
            for (var state : allStates) {
                PointOfInterestTypes.POI_STATES_TO_TYPE.put(state, poiTypeEntry);
            }
        }
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

    @ApiStatus.Internal
    public static void iterateOverBlockTags(BlocksConsumer consumer) {
        for (var entry : BLOCK_TAGS.entrySet()) {
            consumer.consume(entry.getValue(), entry.getKey());
        }
    }

    @ApiStatus.Internal
    public static void iterateOverGroups(GroupConsumer consumer) {
        for (var key : MATERIALS.keySet()) {
            consumer.consume(key);
        }
    }

    @FunctionalInterface
    public interface VariantBlockFactory<V extends Block & VariantBlock> {
        V create(FabricBlockSettings settings);
    }

    @FunctionalInterface
    public interface ExtendedVariantBlockFactory<O extends Block, V extends Block & VariantBlock> {
        V create(FabricBlockSettings settings, O original);
    }

    @FunctionalInterface
    public interface VariantConsumer {
        void consume(Block baseBlock, Block materialBlock);
    }

    @FunctionalInterface
    public interface BlocksConsumer {
        void consume(Set<Block> blocks, TagKey<Block> tag);
    }

    @FunctionalInterface
    public interface GroupConsumer {
        void consume(BlockMaterialGroup materialGroup);
    }

    private record BlockVariantHolder<O extends Block, V extends Block & VariantBlock>(O original, VariantBlockFactory<V> factory, TagKey<Block>[] tags) { }
}
