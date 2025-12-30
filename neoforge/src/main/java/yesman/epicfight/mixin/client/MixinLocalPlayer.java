package yesman.epicfight.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPUpdatePlayerInput;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(value = LocalPlayer.class)
public abstract class MixinLocalPlayer extends AbstractClientPlayer {
    // Dummy constructor
    public MixinLocalPlayer(ClientLevel arg1, GameProfile arg2) {
        super(arg1, arg2);
    }

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;sendPosition()V", shift = At.Shift.BEFORE), method = "tick()V")
	private void epicfight$tick(CallbackInfo callbackInfo) {
		LocalPlayer epicfight$entity = (LocalPlayer)(Object)this;
		LocalPlayerPatch localPlayerPatch = EpicFightCapabilities.getEntityPatch(epicfight$entity, LocalPlayerPatch.class);
		
		if (localPlayerPatch != null) {
			localPlayerPatch.dx = epicfight$entity.xxa;
			localPlayerPatch.dz = epicfight$entity.zza;
		}
		
		EpicFightNetworkManager.sendToServer(new CPUpdatePlayerInput(epicfight$entity.getId(), epicfight$entity.xxa, epicfight$entity.zza));
	}

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void epicfight$onDrop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        if (ControlEngine.getInstance().isSwitchOrDropBlocked()) {
            // Prevents the player from accidentally dropping the item while attacking in Epic Fight mode.
            cir.cancel();
        }
    }

    @Override
    public void moveRelative(float amount, Vec3 relative) {
        Vec3 vec3 = EpicFightCameraAPI.getInstance().getRelativeMove(relative, amount);
        this.setDeltaMovement(this.getDeltaMovement().add(vec3));
    }
}