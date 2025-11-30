package yesman.epicfight.mixin.common;

import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = Player.class)
public abstract class MixinPlayer {
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"
		),
		method = "actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"
	)
	private void epicfight$recordDamage(CombatTracker self, DamageSource damagesource, float damage) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(damagesource.getEntity(), LivingEntityPatch.class).ifPresent(entitypatch -> {
			entitypatch.setLastAttackEntity(self.mob);
		});
		
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