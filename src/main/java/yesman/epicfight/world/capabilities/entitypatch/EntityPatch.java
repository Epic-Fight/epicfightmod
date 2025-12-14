package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import yesman.epicfight.api.client.forgeevent.ProcessEntityPairingPacketEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.network.server.SPEntityPairingPacket;

public abstract class EntityPatch<T extends Entity> {
	protected T original;
	protected boolean initialized = false;
	
	public void onOldPosUpdate() {
	}
	
	public void onAddedToWorld() {
	}
	
	public abstract boolean overrideRender();
	
	public void onStartTracking(ServerPlayer trackingPlayer) {
	}
	
	public void onStopTracking(ServerPlayer trackingPlayer) {
	}
	
	public void onConstructed(T entity) {
		this.original = entity;
	}
	
	public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
		this.initialized = true;
	}

	public void onDeath(LivingDeathEvent event) {
	}

	public final T getOriginal() {
		return this.original;
	}
	
	public boolean isInitialized() {
		return this.initialized;
	}
	
	public boolean isLogicalClient() {
		return this.original.level().isClientSide();
	}
	
	public OpenMatrix4f getMatrix(float partialTick) {
		return MathUtils.getModelMatrixIntegral(0, 0, 0, 0, 0, 0, this.original.xRotO, this.original.getXRot(), this.original.yRotO, this.original.getYRot(), partialTick, 1, 1, 1);
	}
	
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
	
	public abstract OpenMatrix4f getModelMatrix(float partialTick);
	
	@OnlyIn(Dist.CLIENT)
	public void fireEntityPairingEvent(SPEntityPairingPacket msg) {
		ProcessEntityPairingPacketEvent pairingPacketEvent = new ProcessEntityPairingPacketEvent(this, msg);
		MinecraftForge.EVENT_BUS.post(pairingPacketEvent);
		
		if (!pairingPacketEvent.isCanceled()) {
			this.entityPairing(msg);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public void entityPairing(SPEntityPairingPacket packet) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isOutlineVisible(LocalPlayer player) {
		return true;
	}
}