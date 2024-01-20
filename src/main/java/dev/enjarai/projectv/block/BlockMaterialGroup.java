package dev.enjarai.projectv.block;

import dev.enjarai.projectv.ProjectV;
import net.minecraft.util.Identifier;

/**
 * Marker class to represent a collection of blocks that share a similar attribute. Similar to Minecraft's system of tags ({@link net.minecraft.registry.tag.TagKey}), but those have not been initialized yet.
 * <br>
 * TODO maybe read the raw data from the tags available at the time?
 */
public record BlockMaterialGroup(Identifier identifier) {

    /**
     * Planks of any type of wood or wood-like material.
     * <br>
     * Bamboo planks are categorised under this material group as well.
     */
    public static final BlockMaterialGroup PLANKS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "planks"));
    /**
     * A type of stone or naturally occurring mineral from any dimension.
     */
    public static final BlockMaterialGroup STONES = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "stone"));
    /**
     * Natural (non-stripped) logs of what can be considered a tree or tree-like plant.
     * <br>
     * Nether fungi hyphae are categorised under this material group as well, bamboo is not due to its special shape and properties.
     */
    public static final BlockMaterialGroup NATURAL_LOGS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "logs"));

}
