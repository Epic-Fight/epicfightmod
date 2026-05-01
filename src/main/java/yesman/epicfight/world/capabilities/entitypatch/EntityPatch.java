package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.entity.ProcessEntityPairingPacketEvent;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.entity.HandleEntityDataEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.network.server.SPEntityPairingPacket;

public abstract class EntityPatch<T extends Entity> {
    @NotNull
	protected T original;
	protected boolean initialized = false;

	public EntityPatch(@NotNull T original) {
		this.original = original;
	}

	public void onConstructed(T original) {
	}

	public void onAddedToLevel() {
	}

	public abstract boolean overrideRender();

	public void onStartTracking(ServerPlayer trackingPlayer) {
	}

	public void onStopTracking(ServerPlayer trackingPlayer) {
	}

	public void onJoinWorld(T original, Level level, boolean worldgenSpawn) {
		this.initialized = true;
	}

	public void preTick() {
	}

	public void preTickClient() {
	}

	public void preTickServer() {
	}

	public void postTick() {
	}

	public void postTickClient() {
	}

	public void postTickServer() {
	}

	public void writeData(CompoundTag compound) {
        EpicFightEventHooks.Entity.NBT_SAVE.post(new HandleEntityDataEvent.Save(this, compound));
	}

	public void readData(CompoundTag compound) {
        EpicFightEventHooks.Entity.NBT_LOAD.post(new HandleEntityDataEvent.Load(this, compound));
	}

	public final T getOriginal() {
		return this.original;
	}

	public final Level getLevel() {
		return this.original.level();
	}

	public final int getId() {
		return this.original.getId();
	}

	public boolean uninitialized() {
		return !this.initialized;
	}

	public boolean isLogicalClient() {
		return this.original.level().isClientSide();
	}

	public boolean isFakeEntity() {
		return false;
	}

	public OpenMatrix4f getMatrix(float partialTicks) {
		return MathUtils.getModelMatrixIntegral(0, 0, 0, 0, 0, 0, this.original.xRotO, this.original.getXRot(), this.original.yRotO, this.original.getYRot(), partialTicks, 1, 1, 1);
	}

	public abstract OpenMatrix4f getModelMatrix(float partialTicks);

	public double getAngleTo(Entity entity) {
		Vec3 a = this.original.getLookAngle();
		Vec3 b = new Vec3(entity.getX() - this.original.getX(), entity.getY() - this.original.getY(), entity.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);

		return Math.toDegrees(Math.acos(cos));
	}

	public double getAngleToHorizontal(Entity entity) {
		Vec3 a = this.original.getLookAngle();
		Vec3 b = new Vec3(entity.getX() - this.original.getX(), 0.0D, entity.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);

		return Math.toDegrees(Math.acos(cos));
	}

	public Vec3 getViewVector(float partialTick) {
		return this.original.getViewVector(partialTick);
	}

	@ClientOnly
	public void fireEntityPairingEvent(SPEntityPairingPacket packet) {
        ProcessEntityPairingPacketEvent pairingPacketEvent = new ProcessEntityPairingPacketEvent(this, packet);
        EpicFightClientEventHooks.Entity.HANDLE_ENTITY_PAIRING_PACKET.post(pairingPacketEvent);

		if (!pairingPacketEvent.isCanceled()) {
			this.entityPairing(packet);
		}
	}

    @ClientOnly
	public void entityPairing(SPEntityPairingPacket packet) {
	}

    @ClientOnly
	public boolean isOutlineVisible(LocalPlayer player) {
		return true;
	}
}