package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = Player.class)
public abstract class MixinPlayer {
	@Inject(at = @At(value = "TAIL"), method = "<clinit>")
	private static void epicfight$staticInitialize(CallbackInfo callbackInfo) {
		PlayerPatch.initPlayerDataAccessor();
	}
	
	@Inject(at = @At(value = "TAIL"), method = "defineSynchedData()V", cancellable = true)
	protected void epicfight$defineSynchedData(CallbackInfo info) {
		PlayerPatch.createSyncedEntityData((Player)(Object)this);
	}
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"
		),
		method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
	)
	private void epicfight$recordDamage(CombatTracker self, DamageSource damagesource, float damage) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.getEntity(), LivingEntityPatch.class);

		if (entitypatch != null) {
			entitypatch.setLastAttackEntity(self.mob);
		}
		
		self.recordDamage(damagesource, damage);
	}
	
	@Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"
        ),
        method = "serverAiStep()V"
    )
    private float epicfight$serverAiStep(Player player) {
		if (player.isLocalPlayer()) {
			return EpicFightCameraAPI.getInstance().getYRotForHead(player);
		}
		
		return player.getYRot();
    }
}