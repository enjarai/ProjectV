package dev.enjarai.projectv.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.enjarai.projectv.resource.BlockVariantTextureGenerator;
import net.fabricmc.fabric.mixin.resource.loader.client.KeyedResourceReloadListenerClientMixin;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedMixin") // might need it again at some point? feel free to remove though
@Debug(export = true)
@Mixin(value = TextureManager.class, priority = 1500)
public class TextureManagerMixin {

    @SuppressWarnings({"ReferenceToMixin", "UnstableApiUsage"})
    @Dynamic(value = "Added by fabric api", mixin = KeyedResourceReloadListenerClientMixin.class)
    @ModifyExpressionValue(method = "getFabricDependencies", at = @At(value = "INVOKE", target = "Ljava/util/Collections;emptyList()Ljava/util/List;"))
    private List<Identifier> makeVanillaDependOnBlockVariantTextureGenerator(List<Identifier> original) {
        var list = new ArrayList<>(original);
        list.add(BlockVariantTextureGenerator.IDENTIFIER);
        return List.copyOf(list);
    }

}
