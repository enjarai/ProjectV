package dev.enjarai.projectv.mixin.client;

import dev.enjarai.projectv.pack.PackAdderEvent;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycledResourceManagerImplMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static List<ResourcePack> registerARRPs(List<ResourcePack> packs, ResourceType type, List<ResourcePack> packs0) {
        List<ResourcePack> copy = new ArrayList<>(packs);
        PackAdderEvent.EVENT.invoker().addPacks(type, copy);
        return copy;
    }
}
