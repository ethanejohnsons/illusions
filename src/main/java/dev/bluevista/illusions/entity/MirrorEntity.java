package dev.bluevista.illusions.entity;

import dev.bluevista.illusions.DistortionType;
import dev.bluevista.illusions.IllusionsMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class MirrorEntity extends AbstractDecorationEntity {

	public static final TrackedData<Integer> DISTORTION = DataTracker.registerData(MirrorEntity.class, TrackedDataHandlerRegistry.INTEGER);

	public static MirrorEntity place(World world, BlockPos pos, Direction facing, DistortionType distortionType) {
		var entity = IllusionsMod.MIRROR_ENTITY.create(world);
		entity.attachedBlockPos = pos;
		entity.setFacing(facing);
		entity.setDistortionType(distortionType);
		return entity;
	}

	public MirrorEntity(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}

	public void setDistortionType(DistortionType type) {
		getDataTracker().set(DISTORTION, type.ordinal());
	}

	public DistortionType getDistortionType() {
		return DistortionType.values()[getDataTracker().get(DISTORTION)];
	}

	public Vector3f getCenterPos() {
		return getBoundingBox().getCenter().toVector3f();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putByte("facing", (byte) facing.getHorizontal());
		nbt.putInt("distortion", getDistortionType().ordinal());
		super.writeCustomDataToNbt(nbt);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		facing = Direction.fromHorizontal(nbt.getByte("facing"));
		setFacing(facing);
		setDistortionType(DistortionType.values()[nbt.getInt("distortion")]);
		super.readCustomDataFromNbt(nbt);
	}

	@Override
	protected Box calculateBoundingBox(BlockPos pos, Direction side) {
		var p = Vec3d.ofCenter(pos).offset(side, -0.46875).offset(Direction.DOWN, 0.5);
		float thickness = 0.05f;
		float width = 1.0f;
		float height = 2.0f;
		return Box.of(p, side.getOffsetX() == 0 ? width : thickness, height, side.getOffsetZ() == 0 ? width : thickness);
	}

	@Override
	public void onBreak(@Nullable Entity breaker) {
		if (getWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
			playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.0F);

			if (breaker instanceof PlayerEntity playerEntity && playerEntity.isInCreativeMode()) {
				return;
			}

			dropItem(IllusionsMod.NO_DISTORTION_MIRROR_ITEM);
		}
	}

	@Override
	public void onPlace() {
		playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		builder.add(DISTORTION, 0);
	}

	@Override
	public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
		setPosition(x, y, z);
	}

	@Override
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
		setPosition(x, y, z);
	}

	@Override
	public Vec3d getSyncedPos() {
		return Vec3d.of(attachedBlockPos);
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
		return new EntitySpawnS2CPacket(this, facing.getId(), getAttachedBlockPos());
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		setFacing(Direction.byId(packet.getEntityData()));
	}

	@Override
	public ItemStack getPickBlockStack() {
		return switch (getDistortionType().ordinal()) {
			case 0 -> new ItemStack(IllusionsMod.NO_DISTORTION_MIRROR_ITEM);
			case 1 -> new ItemStack(IllusionsMod.SWIRL_DISTORTION_MIRROR_ITEM);
			case 2 -> new ItemStack(IllusionsMod.VERTICAL_DISTORTION_MIRROR_ITEM);
			case 3 -> new ItemStack(IllusionsMod.WIGGLE_DISTORTION_MIRROR_ITEM);
			default -> ItemStack.EMPTY;
		};
	}

}
