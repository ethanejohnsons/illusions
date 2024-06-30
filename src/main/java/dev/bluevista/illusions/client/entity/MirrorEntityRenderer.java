package dev.bluevista.illusions.client.entity;

import dev.bluevista.illusions.client.MirrorRenderer;
import dev.bluevista.illusions.entity.MirrorEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class MirrorEntityRenderer extends EntityRenderer<MirrorEntity> {

	public MirrorEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	/**
	 * This is only for drawing the frame of the mirror.
	 * The reflective portion is drawn after all other world rendering at {@link MirrorRenderer#renderMirror(MirrorEntity, MatrixStack, float)}.
	 */
	public void render(MirrorEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light) {
		var backSprite = MinecraftClient.getInstance().getPaintingManager().getBackSprite();

		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.getFacing().asRotation()));

		drawFrame(
			matrices,
			vcp.getBuffer(RenderLayer.getEntitySolid(backSprite.getAtlasId())),
			entity,
			backSprite
		);

		matrices.pop();

		super.render(entity, yaw, tickDelta, matrices, vcp, light);
	}

	/**
	 * Based on {@link net.minecraft.client.render.entity.PaintingEntityRenderer#renderPainting(MatrixStack, VertexConsumer, PaintingEntity, int, int, Sprite, Sprite)} 
	 */
	private void drawFrame(MatrixStack matrices, VertexConsumer consumer, MirrorEntity entity, Sprite sprite) {
		int width = 1;
		int height = 2;

		var entry = matrices.peek();
		float f = (float)(-width) / 2.0F;
		float g = (float)(-height) / 2.0F;
		float i = sprite.getMinU();
		float j = sprite.getMaxU();
		float k = sprite.getMinV();
		float l = sprite.getMaxV();
		float m = sprite.getMinU();
		float n = sprite.getMaxU();
		float o = sprite.getMinV();
		float p = sprite.getFrameV(0.0625F);
		float q = sprite.getMinU();
		float r = sprite.getFrameU(0.0625F);
		float s = sprite.getMinV();
		float t = sprite.getMaxV();
		double d = 1.0 / (double)width;
		double e = 1.0 / (double)height;

		for (int u = 0; u < width; ++u) {
			for (int v = 0; v < height; ++v) {
				float w = f + (float)(u + 1);
				float x = f + (float)u;
				float y = g + (float)(v + 1);
				float z = g + (float)v;
				int aa = entity.getBlockX();
				int ab = MathHelper.floor(entity.getY() + (double)((y + z) / 2.0F));
				int ac = entity.getBlockZ();

				var direction = entity.getHorizontalFacing();
				if (direction == Direction.NORTH) aa = MathHelper.floor(entity.getX() + (double)((w + x) / 2.0F));
				if (direction == Direction.WEST) ac = MathHelper.floor(entity.getZ() - (double)((w + x) / 2.0F));
				if (direction == Direction.SOUTH) aa = MathHelper.floor(entity.getX() - (double)((w + x) / 2.0F));
				if (direction == Direction.EAST) ac = MathHelper.floor(entity.getZ() + (double)((w + x) / 2.0F));

				int ad = WorldRenderer.getLightmapCoordinates(entity.getWorld(), new BlockPos(aa, ab, ac));
				float ae = sprite.getFrameU((float)(d * (double)(width - u)));
				float af = sprite.getFrameU((float)(d * (double)(width - (u + 1))));
				float ag = sprite.getFrameV((float)(e * (double)(height - v)));
				float ah = sprite.getFrameV((float)(e * (double)(height - (v + 1))));
				vertex(entry, consumer, w, z, af, ag, -0.03125F, 0, 0, -1, ad);
				vertex(entry, consumer, x, z, ae, ag, -0.03125F, 0, 0, -1, ad);
				vertex(entry, consumer, x, y, ae, ah, -0.03125F, 0, 0, -1, ad);
				vertex(entry, consumer, w, y, af, ah, -0.03125F, 0, 0, -1, ad);
				vertex(entry, consumer, w, y, j, k, 0.03125F, 0, 0, 1, ad);
				vertex(entry, consumer, x, y, i, k, 0.03125F, 0, 0, 1, ad);
				vertex(entry, consumer, x, z, i, l, 0.03125F, 0, 0, 1, ad);
				vertex(entry, consumer, w, z, j, l, 0.03125F, 0, 0, 1, ad);
				vertex(entry, consumer, w, y, m, o, -0.03125F, 0, 1, 0, ad);
				vertex(entry, consumer, x, y, n, o, -0.03125F, 0, 1, 0, ad);
				vertex(entry, consumer, x, y, n, p, 0.03125F, 0, 1, 0, ad);
				vertex(entry, consumer, w, y, m, p, 0.03125F, 0, 1, 0, ad);
				vertex(entry, consumer, w, z, m, o, 0.03125F, 0, -1, 0, ad);
				vertex(entry, consumer, x, z, n, o, 0.03125F, 0, -1, 0, ad);
				vertex(entry, consumer, x, z, n, p, -0.03125F, 0, -1, 0, ad);
				vertex(entry, consumer, w, z, m, p, -0.03125F, 0, -1, 0, ad);
				vertex(entry, consumer, w, y, r, s, 0.03125F, -1, 0, 0, ad);
				vertex(entry, consumer, w, z, r, t, 0.03125F, -1, 0, 0, ad);
				vertex(entry, consumer, w, z, q, t, -0.03125F, -1, 0, 0, ad);
				vertex(entry, consumer, w, y, q, s, -0.03125F, -1, 0, 0, ad);
				vertex(entry, consumer, x, y, r, s, -0.03125F, 1, 0, 0, ad);
				vertex(entry, consumer, x, z, r, t, -0.03125F, 1, 0, 0, ad);
				vertex(entry, consumer, x, z, q, t, 0.03125F, 1, 0, 0, ad);
				vertex(entry, consumer, x, y, q, s, 0.03125F, 1, 0, 0, ad);
			}
		}
	}

	private void vertex(MatrixStack.Entry matrix, VertexConsumer vertexConsumer, float x, float y, float u, float v, float z, int nx, int ny, int nz, int light) {
		vertexConsumer
			.vertex(matrix, x, y, z)
			.color(Colors.WHITE)
			.texture(u, v)
			.overlay(OverlayTexture.DEFAULT_UV)
			.light(light)
			.normal(matrix, (float) nx, (float) ny, (float) nz);
	}

	@Override
	public Identifier getTexture(MirrorEntity entity) {
		return null;
	}

}
