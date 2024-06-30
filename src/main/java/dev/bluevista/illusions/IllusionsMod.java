package dev.bluevista.illusions;

import dev.bluevista.illusions.client.MirrorRenderer;
import dev.bluevista.illusions.client.entity.MirrorEntityRenderer;
import dev.bluevista.illusions.entity.MirrorEntity;
import dev.bluevista.illusions.item.MirrorItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IllusionsMod implements ModInitializer {

	public static final String MODID = "illusions";
	public static final Logger LOGGER = LogManager.getLogger("Illusions");

	public static final EntityType<MirrorEntity> MIRROR_ENTITY = Registry.register(
		Registries.ENTITY_TYPE,
		Identifier.of(MODID, "mirror"),
		EntityType.Builder.create(MirrorEntity::new, SpawnGroup.MISC)
			.dimensions(0.5F, 0.5F)
			.maxTrackingRange(10)
			.trackingTickInterval(Integer.MAX_VALUE)
			.build()
	);

	public static final Item NO_DISTORTION_MIRROR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MODID, "normal_mirror"), new MirrorItem(DistortionType.NORMAL));
	public static final Item SWIRL_DISTORTION_MIRROR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MODID, "swirl_mirror"), new MirrorItem(DistortionType.SWIRL));
	public static final Item VERTICAL_DISTORTION_MIRROR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MODID, "vertical_mirror"), new MirrorItem(DistortionType.VERTICAL));
	public static final Item WIGGLE_DISTORTION_MIRROR_ITEM = Registry.register(Registries.ITEM, Identifier.of(MODID, "wiggle_mirror"), new MirrorItem(DistortionType.WIGGLE));

	@Override
	public void onInitialize() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.add(NO_DISTORTION_MIRROR_ITEM);
			content.add(SWIRL_DISTORTION_MIRROR_ITEM);
			content.add(VERTICAL_DISTORTION_MIRROR_ITEM);
			content.add(WIGGLE_DISTORTION_MIRROR_ITEM);
		});

		LOGGER.info("Mirror, mirror...");
	}


}
