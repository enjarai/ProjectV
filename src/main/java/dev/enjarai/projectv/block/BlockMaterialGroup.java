package dev.enjarai.projectv.block;

import dev.enjarai.projectv.ProjectV;
import net.minecraft.util.Identifier;

/**
 * Marker class
 */
public record BlockMaterialGroup(Identifier identifier) {

    public static final BlockMaterialGroup PLANKS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "planks"));
    public static final BlockMaterialGroup STONE = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "stone"));

}
