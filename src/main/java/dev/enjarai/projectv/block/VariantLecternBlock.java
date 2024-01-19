package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Set;

public class VariantLecternBlock extends LecternBlock implements VariantBlock {
    public VariantLecternBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.LECTERN;
    }

    @Override
    public Set<RegistryKey<PointOfInterestType>> getPoiTypes() {
        return Set.of(PointOfInterestTypes.LIBRARIAN);
    }
}
