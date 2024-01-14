package dev.enjarai.projectv.mixin.client;

import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private void reloadBlockVariantTextures(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        new BlockVariantTextureGenerator().reload(manager);
    }

}
