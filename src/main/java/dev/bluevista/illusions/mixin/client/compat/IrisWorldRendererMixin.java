package dev.bluevista.illusions.mixin.client.compat;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class, priority = 1500)
public class IrisWorldRendererMixin {

	@Shadow(remap = false) WorldRenderingPipeline pipeline;

	@SuppressWarnings("CancellableInjectionUsage")
	@TargetHandler(
		mixin = "net.irisshaders.iris.mixin.MixinLevelRenderer",
		name = "Lnet/irisshaders/iris/mixin/MixinLevelRenderer;iris$endLevelRender(Lnet/minecraft/client/render/RenderTickCounter;ZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
	)
	@Inject(method = "@MixinSquared:Handler", at = @At(value = "HEAD"), cancellable = true)
	private void whenThePipelineIsNull(RenderTickCounter deltaTracker, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightTexture, Matrix4f modelMatrix, Matrix4f matrix4f2, CallbackInfo theirCi, CallbackInfo myCi) {
		if (this.pipeline == null) {
			myCi.cancel();
		}
	}
}
