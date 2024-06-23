package dev.bluevista.illusions.mixin.client;

import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
		return camera.isThirdPerson() || MirrorRenderer.IS_DRAWING;
	}

	@Redirect(
		method = "onResized",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/WorldRenderer;scheduleTerrainUpdate()V"
		)
	)
	public void illusions$onResized$scheduleTerrainUpdate(WorldRenderer worldRenderer) {
		if (MirrorRenderer.IS_DRAWING) {
			// NO-OP
		} else {
			worldRenderer.scheduleTerrainUpdate();
		}
	}

}
