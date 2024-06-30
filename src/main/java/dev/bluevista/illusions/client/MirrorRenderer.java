package dev.bluevista.illusions.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.bluevista.illusions.compat.Iris;
import dev.bluevista.illusions.entity.MirrorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.ladysnake.satin.api.managed.ManagedCoreShader;
import org.ladysnake.satin.api.managed.ShaderEffectManager;
import org.ladysnake.satin.api.managed.uniform.Uniform1i;

import static dev.bluevista.illusions.IllusionsMod.MODID;

/**
 * Over all not the best...
 * but also not that bad for a <24 hour turnaround time
 */
@Environment(EnvType.CLIENT)
public class MirrorRenderer {

	public static final ManagedCoreShader SHADER = ShaderEffectManager.getInstance().manageCoreShader(Identifier.of(MODID, "warp"), VertexFormats.POSITION_TEXTURE);
	public static final Uniform1i DISTORTION_TYPE = SHADER.findUniform1i("DistortionType");
	public static final Identifier MIRROR_PLACEHOLDER_TEXTURE = Identifier.of(MODID, "textures/mirror_placeholder.png");
	public static final int MAX_MIRRORS_DEEP = 3;

	private static final Framebuffer[] framebuffers = new Framebuffer[MAX_MIRRORS_DEEP];
	private static int mirrorsDeep;

	public static boolean isDrawing() {
		return mirrorsDeep > 0;
	}

	public static boolean canDraw() {
		boolean fabulous = MinecraftClient.getInstance().options.getGraphicsMode().getValue() == GraphicsMode.FABULOUS;
		boolean iris = Iris.isInstalled();

		return !iris && !fabulous && mirrorsDeep < MAX_MIRRORS_DEEP;
	}

	@Nullable
	public static Framebuffer getFramebuffer() {
		if (!canDraw()) return null;
		return framebuffers[mirrorsDeep - 1];
	}

	public static void onResize(int width, int height) {
		for (int i = 0; i < framebuffers.length; i++) {
			framebuffers[i] = new SimpleFramebuffer(width, height, true, false);
		}
	}

	public static void onRenderWorld(WorldRenderContext ctx) {
		for (var entity : ctx.world().getEntities()) {
			if (entity instanceof MirrorEntity mirror) {
				var cameraPos = ctx.camera().getPos();
				var entityPos = entity.getPos();
				float cameraYaw = ctx.camera().getYaw();
				float entityYaw = mirror.getYaw();
				float epsilon = 180;

				// Check if the camera can see the surface of the mirror.
				// No sense drawing the entire viewport again for mirrors (or even recursive ones) that you can't see.
				if (
					entityPos.distanceTo(cameraPos) < 128.0 &&
					entity.getWorld().raycast(new RaycastContext(cameraPos, entityPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS &&
					Math.abs(entityYaw - cameraYaw) % 360 < 180 + epsilon && Math.abs(entityYaw - cameraYaw) % 360 > 180 - epsilon
				) {

					renderMirror(
						mirror,
						ctx.matrixStack(),
						ctx.tickCounter().getTickDelta(false)
					);

				}
			}
		}
	}

	private static void renderMirror(MirrorEntity entity, MatrixStack matrices, float tickDelta) {
		int tex = renderWorld(entity);
		if (tex == -1) {
			tex = MinecraftClient.getInstance().getTextureManager().getTexture(MIRROR_PLACEHOLDER_TEXTURE).getGlId();
		}

		RenderSystem.setShader(SHADER::getProgram);
		RenderSystem.setShaderTexture(0, tex);
		RenderSystem.enableDepthTest();

		DISTORTION_TYPE.set(entity.getDistortionType().ordinal());

		var camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		var entityPos = entity.getLerpedPos(tickDelta);
		var cameraPos = camera.getPos();
		var translation = entityPos.subtract(cameraPos);

		matrices.push();
		matrices.translate(translation.x, translation.y, translation.z);
		matrices.multiply(entity.getFacing().getRotationQuaternion());
		matrices.translate(0, 0.033, 0);

		var buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		float tWidth = 0.2f;
		float tHeight = 0.5f;
		float vWidth = 0.45f;
		float vHeight = 0.95f;
		buffer
			.vertex(matrices.peek().getPositionMatrix(), -vWidth, 0.0f, -vHeight)
			.texture(0.5f + tWidth, 0.5f + tHeight);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), -vWidth, 0.0f, vHeight)
			.texture(0.5f + tWidth, 0.5f - tHeight);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), vWidth, 0.0f, vHeight)
			.texture(0.5f - tWidth,  0.5f - tHeight);
		buffer
			.vertex(matrices.peek().getPositionMatrix(), vWidth, 0.0f, -vHeight)
			.texture(0.5f - tWidth, 0.5f + tHeight);
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		matrices.pop();
	}

	/**
	 * blah
	 */
	private static int renderWorld(MirrorEntity entity) {
		if (!canDraw()) return -1;

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
			camera.setRotation(direction.asRotation() , 0);
			var cameraRotation = camera.getRotation();

			// Set up frustum
			var rotMat = new Matrix4f().rotate(cameraRotation.conjugate(new Quaternionf()));
			var projMat = client.gameRenderer.getBasicProjectionMatrix(70f);
			client.worldRenderer.setupFrustum(camera.getPos(), rotMat, projMat);
			RenderSystem.viewport(
				0, 0,
				MinecraftClient.getInstance().getFramebuffer().textureWidth,
				MinecraftClient.getInstance().getFramebuffer().textureHeight
			);


			// Draw the world
			mirrorsDeep++;

			var framebuffer = getFramebuffer();
			if (framebuffer == null) {
				mirrorsDeep--;
				return -1;
			}
			framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);

			framebuffer.beginWrite(true);
			client.getWindow().setFramebufferWidth(framebuffer.textureWidth);
			client.getWindow().setFramebufferHeight(framebuffer.textureHeight);
			client.gameRenderer.loadProjectionMatrix(projMat);
			RenderSystem.modelViewStack = new Matrix4fStack(16);
			client.worldRenderer.render(client.getRenderTickCounter(), false, camera, client.gameRenderer, client.gameRenderer.getLightmapTextureManager(), rotMat, projMat);
			mirrorsDeep--;


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
			return framebuffer.getColorAttachment();
		} catch (Exception ex) {
			ex.printStackTrace();
			return -1;
		}
	}

}
