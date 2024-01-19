package dev.enjarai.projectv.block;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.Set;

public interface VariantBlock {
    Block getBaseBlock();

    default Set<RegistryKey<PointOfInterestType>> getPoiTypes() {
        return Set.of();
    }
}
