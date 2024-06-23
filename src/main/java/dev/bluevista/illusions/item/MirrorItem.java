package dev.bluevista.illusions.item;

import dev.bluevista.illusions.DistortionType;
import dev.bluevista.illusions.entity.MirrorEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;

public class MirrorItem extends Item {

	private final DistortionType distortionType;

	public MirrorItem(DistortionType distortionType) {
		super(new Item.Settings());
		this.distortionType = distortionType;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		var playerEntity = ctx.getPlayer();
		if (playerEntity != null && !canPlaceOn(playerEntity, ctx.getSide(), ctx.getStack(), ctx.getBlockPos().offset(ctx.getSide()))) {
			return ActionResult.FAIL;
		}

		var world = ctx.getWorld();
		var entity = MirrorEntity.place(world, ctx.getBlockPos().offset(ctx.getSide()), ctx.getSide(), distortionType);

		if (entity.canStayAttached()) {
			if (!world.isClient) {
				entity.onPlace();
				world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, entity.getPos());
				world.spawnEntity(entity);
			}

			ctx.getStack().decrement(1);
			return ActionResult.success(world.isClient);
		} else {
			return ActionResult.CONSUME;
		}
	}

	protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
		return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
	}

}
