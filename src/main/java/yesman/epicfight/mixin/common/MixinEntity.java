package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = Entity.class)
public abstract class MixinEntity {
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