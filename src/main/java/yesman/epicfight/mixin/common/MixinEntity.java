package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.NeoForge;
import yesman.epicfight.api.animation.property.AnimationProperty.ActionAnimationProperty;
import yesman.epicfight.api.neoevent.EntityRemoveEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = Entity.class)
public abstract class MixinEntity {
	@Shadow
	protected abstract void readAdditionalSaveData(CompoundTag compound);
	
	@Shadow
    protected abstract void addAdditionalSaveData(CompoundTag compound);
	
	@Inject(at = @At(value = "TAIL"), method = "onAddedToLevel()V", cancellable = true, remap = false)
	private void epicfight$onAddedToLevel(CallbackInfo callbackInfo) {
		Entity self = (Entity)((Object)this);
		EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, EntityPatch.class);
		
		if (entitypatch != null) {
			entitypatch.onAddedToLevel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "lerpMotion(DDD)V", cancellable = true)
	public void epicfight$lerpMotion(double pX, double pY, double pZ, CallbackInfo callback) {
		Entity self = (Entity)(Object)this;
		
		// Remove the delta movement from the server while playing animation with REMOVE_DELTA_MOVEMENT property set as true
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, LivingEntityPatch.class).ifPresent(entitypatch -> {
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
	 * Maintain this mixin until neoforge provides an event hook when entity is removed
	 * {@link Entity#remove(net.minecraft.world.entity.Entity.RemovalReason)
	 */
	@Inject(at = @At(value = "HEAD"), method = "remove(Lnet/minecraft/world/entity/Entity$RemovalReason;)V")
	public void epicfight$remove(Entity.RemovalReason reason, CallbackInfo callback) {
		Entity self = (Entity)(Object)this;
		NeoForge.EVENT_BUS.post(new EntityRemoveEvent(reason, self));
    }
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
		),
		method = "saveWithoutId(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;"
	)
	private void epicfight$saveWithoutId(Entity self, CompoundTag compoundTag) {
		this.addAdditionalSaveData(compoundTag);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, EntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.writeData(compoundTag);
		});
	}
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"
		),
		method = "load(Lnet/minecraft/nbt/CompoundTag;)V"
	)
	private void epicfight$load(Entity self, CompoundTag compoundTag) {
		this.readAdditionalSaveData(compoundTag);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, EntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.readData(compoundTag);
		});
	}
}