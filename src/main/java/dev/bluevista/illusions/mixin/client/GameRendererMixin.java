package dev.bluevista.illusions.mixin.client;

import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Inject(method = "onResized", at = @At(value = "HEAD"))
	private void illusions$onResized$HEAD(int width, int height, CallbackInfo ci) {
		MirrorRenderer.onResize(width, height);
	}

}
