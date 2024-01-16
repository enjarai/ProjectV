package dev.enjarai.projectv.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class VariantTorchBlock extends TorchBlock implements VariantBlock {
    public VariantTorchBlock(Settings settings, ParticleEffect particle) {
        super(settings, particle);
    }

    @Override
    public Block getBaseBlock() {
        return Blocks.TORCH;
    }
}
