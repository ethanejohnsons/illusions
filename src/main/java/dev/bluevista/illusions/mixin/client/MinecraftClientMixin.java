package dev.bluevista.illusions.mixin.client;

import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When drawing a mirror, always use the mirror's framebuffer instead of the normal one.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
	public void illusions$getFramebuffer$HEAD(CallbackInfoReturnable<Framebuffer> cir) {
		if (MirrorRenderer.isDrawing()) {
			cir.setReturnValue(MirrorRenderer.getFramebuffer());
		}
	}

}
