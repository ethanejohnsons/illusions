package dev.bluevista.illusions.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.bluevista.illusions.client.MirrorRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows the client player to be seen in mirrors.
 */
@Debug(export = true)
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
	public boolean illusions$render$isThirdPerson(boolean original) {
		return original || MirrorRenderer.isDrawing();
	}

}
