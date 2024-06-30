package dev.bluevista.illusions;

import dev.bluevista.illusions.client.MirrorRenderer;
import dev.bluevista.illusions.client.entity.MirrorEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class IllusionsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(IllusionsMod.MIRROR_ENTITY, MirrorEntityRenderer::new);
		WorldRenderEvents.LAST.register(MirrorRenderer::onRenderWorld);
	}
}
