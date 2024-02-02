package dev.enjarai.projectv.mixin.client.chest;

import dev.enjarai.projectv.ProjectV;
import dev.enjarai.projectv.block.VariantChestBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TexturedRenderLayers.class)
public abstract class TexturedRenderLayersMixin {
    @Shadow @Final public static Identifier CHEST_ATLAS_TEXTURE;

    @Inject(
            method = "getChestTextureId(Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/block/enums/ChestType;Z)Lnet/minecraft/client/util/SpriteIdentifier;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void addVariantChestTextures(BlockEntity blockEntity, ChestType type, boolean christmas, CallbackInfoReturnable<SpriteIdentifier> cir) {
        // Is the block we're rendering a variant chest?
        if (blockEntity instanceof ChestBlockEntity chestEntity && chestEntity.getCachedState().getBlock() instanceof VariantChestBlock chestBlock) {
            var variantBlockId = Registries.BLOCK.getId(chestBlock);
            // (projectv:some_chest -> projectv:entity/chest/some_chest_normal)
            var chestTextureId = variantBlockId.withPrefixedPath("entity/chest/").withSuffixedPath("_normal");

            // Find the textures we assume to be in these locations, I sure hope that json wasn't modified!
            cir.setReturnValue(switch (type) {
                case SINGLE -> new SpriteIdentifier(CHEST_ATLAS_TEXTURE, chestTextureId);
                case LEFT -> new SpriteIdentifier(CHEST_ATLAS_TEXTURE, chestTextureId.withSuffixedPath("_left"));
                case RIGHT -> new SpriteIdentifier(CHEST_ATLAS_TEXTURE, chestTextureId.withSuffixedPath("_right"));
            });
        }
    }
}
