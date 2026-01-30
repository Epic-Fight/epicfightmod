package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = Entity.class)
public abstract class MixinEntity {
	@Shadow
	private boolean onGround;
	
	/**
	 * Stores when {@link #onGround} was lastly true
	 * 
	 * 'onGround' has a noise data while ticking, that it judges the entity is not on a ground
	 * due to floating point errors. So the raw data is unreliable. Thankfully, I found
	 * the "not-on-ground" noise wouldn't persist for 2 or even a tick so I could apply a guard ticks
	 * to distinguish the value is caused by actual player jumps or the noise.
	 * 
	 * Normally, when a player jumps, the variable becomes false and persists for around 5-6 ticks.
	 * I thought the reasonable compromise was 4 ticks. In internal tests, it worked as intended
	 * but we need to gather more exclusive cases, or wait until Minecraft provides reliable
	 * access when player touches a around after jumping
	 */
	@Unique
	private int lastOnGroundTick;
	
	@Inject(at = @At(value = "HEAD"), method = "setOldPosAndRot()V", cancellable = true)
	private void epicfight_setOldPosAndRot(CallbackInfo callbackInfo) {
		Entity self = (Entity)((Object)this);
		EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, EntityPatch.class);
		
		if (entitypatch != null) {
			entitypatch.onOldPosUpdate();
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "onAddedToWorld()V", cancellable = true, remap = false)
	private void epicfight_onAddedToWorld(CallbackInfo callbackInfo) {
		Entity self = (Entity)((Object)this);
		EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, EntityPatch.class);
		
		if (entitypatch != null) {
			entitypatch.onAddedToWorld();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "lerpMotion(DDD)V", cancellable = true)
	private void lerpMotion(double pX, double pY, double pZ, CallbackInfo callback) {
		Entity e = (Entity)(Object)this;
		
		// Remove the delta movement from the server while playing animation with REMOVE_DELTA_MOVEMENT property set as true
		EpicFightCapabilities.getUnparameterizedEntityPatch(e, LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (entitypatch.getAnimator().getPlayerFor(null).getRealAnimation().get().getProperty(ActionAnimationProperty.REMOVE_DELTA_MOVEMENT).orElse(false)) {
				callback.cancel();
			}
		});
	}
	
	@ModifyVariable(method = "turn(DD)V", at = @At("HEAD"), ordinal = 0)
	private double epicfight$turnParam1(double yRot) {
		Entity e = (Entity)(Object)this;
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(e, PlayerPatch.class);
		
		if (playerpatch != null) {
			return playerpatch.checkYTurn(yRot);
		}
		
		return yRot;
	}
	
	@ModifyVariable(method = "turn(DD)V", at = @At("HEAD"), ordinal = 1)
	private double epicfight$turnParam2(double xRot) {
		Entity e = (Entity)(Object)this;
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(e, PlayerPatch.class);
		
		if (playerpatch != null) {
			return playerpatch.checkXTurn(xRot);
		}
		
		return xRot;
	}
	
	/**
	 * Called when setting {@link Entity#onGround} according to the player's movement
	 * 
	 * the onGround variable is synced from a server to a client. {@link ServerGamePacketListenerImpl#handleMovePlayer}
	 */
	@Inject(at = @At(value = "HEAD"), method = "setOnGroundWithKnownMovement(ZLnet/minecraft/world/phys/Vec3;)V")
	public void epicfight$setOnGroundWithKnownMovement(boolean pOnGround, Vec3 pMovement, CallbackInfo callbackInfo) {
		Entity self = (Entity)(Object)this;
		
		if (onGround) lastOnGroundTick = self.tickCount;
		
		if (!onGround && pOnGround) { // When a player touches a ground from air.
			if (self.tickCount - lastOnGroundTick >= 4) { // 4 ticks are noise guard for floating point calculation error
				EpicFightCapabilities.<LivingEntity, LivingEntityPatch<LivingEntity>>getParameterizedEntityPatch(self, LivingEntity.class, LivingEntityPatch.class).ifPresent(entitypatch -> {
					entitypatch.onFall(new LivingFallEvent(entitypatch.getOriginal(), entitypatch.getOriginal().fallDistance, 1.0F));
				});
			}
		}
	}
	
	/**
	 * Useful mixin to debug y rotation, especially for action animations
	@Inject(at = @At(value = "HEAD"), method = "setYRot()V")
	private void epicfight$setYRot(float pYRot, CallbackInfo callbackInfo) {
		if (Float.isFinite(pYRot)) {
			if (!Minecraft.getInstance().isPaused()) {
				System.out.println("set YRot " + pYRot + ((Entity)(Object)this).level().isClientSide());
				new Exception().printStackTrace();
			}
		}
	}
	**/
}