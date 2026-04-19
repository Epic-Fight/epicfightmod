package yesman.epicfight.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.projectile.ThrownTridentPatch;

@Mixin(value = ThrownTrident.class)
public abstract class MixinThrownTrident extends AbstractArrow {
	protected MixinThrownTrident(EntityType<? extends AbstractArrow> p_36721_, Level p_36722_) {
		super(p_36721_, p_36722_);
	}
	
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;setPosRaw(DDD)V"), method = "tick()V")
	private void epicfight_setPosRawInTick(ThrownTrident entity, double x, double y, double z, Operation<Void> original) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(entity, ThrownTridentPatch.class);
		
		if (tridentPatch == null || !tridentPatch.isInnateActivated()) {
			original.call(entity, x, y, z);
		}
	}
	
	@WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ThrownTrident;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"), method = "tick()V")
	private void epicfight_setDeltaMovementInTick(ThrownTrident entity, Vec3 vec3, Operation<Void> original) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(entity, ThrownTridentPatch.class);
		
		if (tridentPatch == null || !tridentPatch.isInnateActivated()) {
			original.call(entity, vec3);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "tick()V", cancellable = false)
	private void epicfight_tickEnd(CallbackInfo info) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch((ThrownTrident)((Object)this), ThrownTridentPatch.class);
		
		if (tridentPatch != null) {
			tridentPatch.tickEnd();
		}
	}
	
	@WrapOperation(     at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;playerTouch(Lnet/minecraft/world/entity/player/Player;)V")
	         , method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V")
	private void epicfight_playerTouch(ThrownTrident entity, Player player, Operation<Void> original) {
		ThrownTridentPatch tridentPatch = EpicFightCapabilities.getEntityPatch(entity, ThrownTridentPatch.class);
		
		if (tridentPatch != null && tridentPatch.isInnateActivated()) {
			PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
			
			if (playerpatch != null && !playerpatch.isLogicalClient()) {
				tridentPatch.catchByPlayer(playerpatch);
			}
		}
		
		original.call(entity, player);
	}
}