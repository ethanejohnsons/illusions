package dev.bluevista.illusions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bluevista.illusions.IllusionsMod;
import dev.bluevista.illusions.entity.MirrorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.ladysnake.satin.api.managed.ManagedCoreShader;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1i;

/**
 * Over all not the best...
 * but also not that bad for a <24 hour turnaround time
 */
@Environment(EnvType.CLIENT)
public class MirrorRenderer {

	public static final ManagedCoreShader SHADER = ShaderEffectManager.getInstance().manageCoreShader(Identifier.of(IllusionsMod.MODID, "warp"), VertexFormats.POSITION_TEXTURE);
	public static final Uniform1i DISTORTION_TYPE = SHADER.findUniform1i("DistortionType");
	public static final int MAX_MIRRORS = 16;

	public static Framebuffer FB;
	public static boolean IS_DRAWING;
	public static int MIRRORS_THIS_FRAME;

	public static void onResize(int width, int height) {
		FB = new SimpleFramebuffer(width, height, true, false);
	}

	public static void onRenderWorld(WorldRenderContext ctx) {
		if (IS_DRAWING) return;

		MIRRORS_THIS_FRAME = 0;

		for (var entity : ctx.world().getEntities()) {
			if (entity instanceof MirrorEntity mirror) {
				renderMirror(
					mirror,
					ctx.matrixStack(),
					ctx.consumers(),
					ctx.camera(),
					ctx.tickCounter().getTickDelta(false)
				);
			}
		}
	}

	private static void renderMirror(MirrorEntity entity, MatrixStack matrices, VertexConsumerProvider vcp, Camera camera, float tickDelta) {
		int tex = renderWorld(entity);
		if (tex == -1) return;

		RenderSystem.setShader(SHADER::getProgram);
		RenderSystem.setShaderTexture(0, tex);
		RenderSystem.enableDepthTest();

		DISTORTION_TYPE.set(entity.getDistortionType().ordinal());

		var entityPos = entity.getLerpedPos(tickDelta);
		var cameraPos = camera.getPos();
		var translation = entityPos.subtract(cameraPos);

		matrices.push();
		matrices.translate(translation.x, translation.y, translation.z);
		matrices.multiply(entity.getFacing().getRotationQuaternion());
		matrices.translate(0, 0.032, 0);

		var buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		float width = 0.2f;
		float height = 0.5f;
		buffer
			.vertex(matrices.peek().getPositionMatrix(), -0.45f, 0.0f, -0.95f)
			.texture(0.5f + width, 0.5f + height);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), -0.45f, 0.0f, 0.95f)
			.texture(0.5f + width, 0.5f - height);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), 0.45f, 0.0f, 0.95f)
			.texture(0.5f - width,  0.5f - height);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), 0.45f, 0.0f, -0.95f)
			.texture(0.5f - width, 0.5f + height);
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		matrices.pop();
	}

	/**
	 * blah
	 */
	private static int renderWorld(MirrorEntity entity) {
		if (FB == null) return -1;
		if (MIRRORS_THIS_FRAME >= MAX_MIRRORS) return -1;
		if (MinecraftClient.getInstance().options.getGraphicsMode().getValue() == GraphicsMode.FABULOUS) return -1;

		MIRRORS_THIS_FRAME++;

		FB.clear(MinecraftClient.IS_SYSTEM_MAC);

		var client = MinecraftClient.getInstance();
		var camera = client.gameRenderer.getCamera();
		var position = entity.getCenterPos();
		var direction = entity.getFacing();

		try {
			// Keep some original values for later
			var oldModelViewStack = RenderSystem.getModelViewStack();
			var oldModelViewMat = new Matrix4f(RenderSystem.getModelViewMatrix());
			var prevProjMat = new Matrix4f(RenderSystem.getProjectionMatrix());
			var oldFrustum = client.worldRenderer.frustum;
			var oldPos = camera.pos;
			float oldYaw = camera.getYaw();
			float oldPitch = camera.getPitch();
			int oldFboWidth = client.getWindow().getFramebufferWidth();
			int oldFboHeight = client.getWindow().getFramebufferHeight();

			// TODO angle is not correct

			// Set camera position and rotation
			camera.pos = new Vec3d(position);
			camera.setRotation(-direction.asRotation(), 0);
			var cameraRotation = camera.getRotation();

			// Set up frustum
			var rotMat = new Matrix4f().rotate(cameraRotation);
			var projMat = client.gameRenderer.getBasicProjectionMatrix(client.options.getFov().getValue());
			client.worldRenderer.setupFrustum(camera.getPos(), rotMat, projMat);
			RenderSystem.viewport(0, 0, FB.textureWidth, FB.textureHeight);

			// Draw the world
			IS_DRAWING = true;
			FB.beginWrite(true);
			client.getWindow().setFramebufferWidth(FB.textureWidth);
			client.getWindow().setFramebufferHeight(FB.textureHeight);
			client.gameRenderer.loadProjectionMatrix(projMat);
			RenderSystem.modelViewStack = new Matrix4fStack(16);
			client.worldRenderer.render(client.getRenderTickCounter(), false, camera, client.gameRenderer, client.gameRenderer.getLightmapTextureManager(), rotMat, projMat);
			IS_DRAWING = false;

			// Restore original values
			client.getFramebuffer().beginWrite(false);
			camera.pos = oldPos;
			camera.setRotation(oldYaw, oldPitch);
			client.worldRenderer.frustum = oldFrustum;
			client.gameRenderer.loadProjectionMatrix(prevProjMat);
			RenderSystem.modelViewStack = oldModelViewStack;
			RenderSystem.modelViewMatrix = oldModelViewMat;
			client.getWindow().setFramebufferWidth(oldFboWidth);
			client.getWindow().setFramebufferHeight(oldFboHeight);
			RenderSystem.viewport(0, 0, client.getWindow().getFramebufferWidth(), client.getWindow().getFramebufferHeight());

			// Return the texture ID
			return FB.getColorAttachment();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
