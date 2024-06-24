package dev.bluevista.illusions.mixin.client;

import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows the client player to be seen in mirrors.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Redirect(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"
		)
	)
	public boolean illusions$render$isThirdPerson(Camera camera) {
		return camera.isThirdPerson() || MirrorRenderer.isDrawing();
	}

}
