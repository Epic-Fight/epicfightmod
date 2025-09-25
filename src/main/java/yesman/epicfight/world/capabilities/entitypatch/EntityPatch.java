package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import yesman.epicfight.api.client.neoevent.ProcessEntityPairingPacketEvent;
import yesman.epicfight.api.neoevent.HandleEntityDataEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.server.SPEntityPairingPacket;

public abstract class EntityPatch<T extends Entity> {
	protected T original;
	protected boolean initialized = false;
	
	public EntityPatch(T original) {
		this.original = original;
	}
	
	public void onConstructed(EntityEvent.EntityConstructing event) {
	}
	
	public void onAddedToLevel() {
	}
	
	public abstract boolean overrideRender();
	
	public void onStartTracking(ServerPlayer trackingPlayer) {
	}
	
	public void onStopTracking(ServerPlayer trackingPlayer) {
	}
	
	public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
		this.initialized = true;
	}
	
	public void preTick(EntityTickEvent.Pre event) {
	}
	
	public void preTickClient(EntityTickEvent.Pre event) {
	}
	
	public void preTickServer(EntityTickEvent.Pre event) {
	}
	
	public void postTick(EntityTickEvent.Post event) {
	}
	
	public void postTickClient(EntityTickEvent.Post event) {
	}
	
	public void postTickServer(EntityTickEvent.Post event) {
	}
	
	public void writeData(CompoundTag compound) {
		NeoForge.EVENT_BUS.post(new HandleEntityDataEvent.Save(this, compound));
	}
	
	public void readData(CompoundTag compound) {
		NeoForge.EVENT_BUS.post(new HandleEntityDataEvent.Load(this, compound));
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
	
	public boolean isInitialized() {
		return this.initialized;
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
	
	@OnlyIn(Dist.CLIENT)
	public void fireEntityPairingEvent(SPEntityPairingPacket msg) {
		ProcessEntityPairingPacketEvent pairingPacketEvent = new ProcessEntityPairingPacketEvent(this, msg);
		NeoForge.EVENT_BUS.post(pairingPacketEvent);
		
		if (!pairingPacketEvent.isCanceled()) {
			this.entityPairing(msg);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void entityPairing(SPEntityPairingPacket packet) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isOutlineVisible(LocalPlayerPatch player) {
		return true;
	}
}