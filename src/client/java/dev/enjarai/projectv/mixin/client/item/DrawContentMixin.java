package dev.enjarai.projectv.mixin.client.item;

import dev.enjarai.projectv.item.VariantItemStack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContentMixin {
    @Shadow @Final private MatrixStack matrices;

    @Shadow public abstract int drawText(TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow);

    @Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemBarVisible()Z"))
    private void drawVariantItemCount(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
        if (!(stack instanceof VariantItemStack variantStack)) return;

        int adjustedX = x + textRenderer.getWidth(countOverride == null ? String.valueOf(stack.getCount()) : countOverride);
        String totalCount = String.valueOf(variantStack.getTotalCount());
        matrices.push();
        matrices.scale(0.5f, 0.5f, 1);
        matrices.translate(adjustedX - 4, y - 4, 0);
        drawText(textRenderer, totalCount, adjustedX - textRenderer.getWidth(totalCount), y - 12, 0xFF00FFFF, true);
        matrices.pop();
    }
}
