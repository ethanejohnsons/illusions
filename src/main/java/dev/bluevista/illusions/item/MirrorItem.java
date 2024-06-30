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
		var player = ctx.getPlayer();
		var stack = ctx.getStack();
		var blockPos = ctx.getBlockPos();
		var side = ctx.getSide();
		var world = ctx.getWorld();

		if (player != null && !canPlaceOn(player, side, stack, blockPos.offset(ctx.getSide()))) {
			return ActionResult.FAIL;
		}

		var entity = MirrorEntity.place(world, blockPos.offset(side), side, distortionType);

		if (entity.canStayAttached()) {
			if (!world.isClient) {
				entity.onPlace();
				world.emitGameEvent(player, GameEvent.ENTITY_PLACE, entity.getPos());
				world.spawnEntity(entity);
			}

			stack.decrement(1);
			return ActionResult.success(world.isClient);
		} else {
			return ActionResult.CONSUME;
		}
	}

	protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
		return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
	}

}
