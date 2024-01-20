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
     * <br><br>
     * Bamboo planks are categorised under this material group as well.
     */
    public static final BlockMaterialGroup PLANKS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "planks"));
    /**
     * A type of stone or naturally occurring mineral from any dimension.
     */
    public static final BlockMaterialGroup STONES = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "stones"));
    /**
     * Natural (non-stripped) logs of what can be considered a tree or tree-like plant.
     * <br><br>
     * Nether fungi hyphae are categorised under this material group as well, bamboo should not due to its special shape and properties.
     */
    public static final BlockMaterialGroup NATURAL_LOGS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "logs"));

    /**
     * Compressed block of anything that counts as a metal, usually made out of ingots of that metal.
     * <br><br>
     * Usually metals are quite expensive, so this material group could be used for balancing.
     * Minerals like diamond, redstone or lapis lazuli should not be found here, but netherite for example is due to having an ingot.
     */
    public static final BlockMaterialGroup REFINED_METALS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "refined_metals"));

    /**
     * Types of what can be considered wool with varying colors and perhaps origins.
     */
    public static final BlockMaterialGroup WOOLS = new BlockMaterialGroup(new Identifier(ProjectV.MOD_ID, "wools"));
}
