package dev.enjarai.projectv.mixin;

import dev.enjarai.projectv.data.BlockVariantTagGenerator;
import net.minecraft.registry.tag.TagManagerLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(TagManagerLoader.class)
public class TagManagerLoaderMixin {

    @Inject(method = "reload", at = @At("HEAD"))
    private void reloadBlockVariantTags(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        BlockVariantTagGenerator.reload();
    }

}
