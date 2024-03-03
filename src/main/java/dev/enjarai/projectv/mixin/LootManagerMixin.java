package dev.enjarai.projectv.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.enjarai.projectv.data.BlockVariantLootTableGenerator;
import net.minecraft.loot.LootManager;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(LootManager.class)
public class LootManagerMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private void reloadBlockVariantLootTables(CallbackInfoReturnable<CompletableFuture<Void>> cir, @Local(argsOnly = true) ResourceManager manager) {
        BlockVariantLootTableGenerator.reload(manager);
    }

}
