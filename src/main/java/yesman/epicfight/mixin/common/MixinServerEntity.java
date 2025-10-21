package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = ServerEntity.class)
public abstract class MixinServerEntity {
	
	@Shadow
	@Final
	private Entity entity;
	
	@Inject(at = @At(value = "TAIL"), method = "sendDirtyEntityData()V", cancellable = false)
	public void epicfight$sendDirtyEntityData(CallbackInfo callback) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(this.entity, LivingEntityPatch.class).ifPresent(entitypatch -> {
			EpicFightNetworkManager.PayloadBundleBuilder payloadsBuilder = entitypatch.getExpandedSynchedData().prepareDataToSend();
			
			if (payloadsBuilder != null) {
				payloadsBuilder.send((payload, payloads) -> entitypatch.sendToAllPlayersTrackingMe(payload, payloads));
			}
		});
	}
}
