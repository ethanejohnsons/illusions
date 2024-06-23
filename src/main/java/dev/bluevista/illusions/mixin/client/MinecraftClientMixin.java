package dev.bluevista.illusions.mixin.client;

import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Inject(method = "getFramebuffer", at = @At("HEAD"), cancellable = true)
	public void illusions$getFramebuffer(CallbackInfoReturnable<Framebuffer> cir) {
		if (MirrorRenderer.IS_DRAWING) {
			cir.setReturnValue(MirrorRenderer.FB);
		}
	}

}
