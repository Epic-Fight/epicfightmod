package yesman.epicfight.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.config.ClientConfig;

@Mixin(value = GameRenderer.class)
public abstract class MixinGameRenderer {
	@Shadow
	private Camera mainCamera;
	@Shadow
	private Minecraft minecraft;
	@Shadow
	protected abstract void pick(float sartialTick);
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"
		),
		method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"
	)
	private void epicfight$renderLevel(GameRenderer gameRenderer, float partialTick) {
		if (EpicFightCameraAPI.getInstance().isTPSMode()) {
			this.pickInTPSPerspective(partialTick);
		} else {
			this.pick(partialTick);
		}
	}
	
	/**
	 * Code copy from {@link GameRenderer#pick(float)}
	 */
	@Unique
	private void pickInTPSPerspective(float partialTick) {
		Entity entity = this.minecraft.getCameraEntity();
		if (entity != null) {
			if (this.minecraft.level != null) {
				this.minecraft.hitResult = EpicFightCameraAPI.getInstance().getCrosshairHitResult();
				this.minecraft.crosshairPickEntity = EpicFightCameraAPI.getInstance().getFocusingEntity();
				
				if (this.minecraft.hitResult != null) {
					double d0 = (double) this.minecraft.gameMode.getPickRange();
					double entityReach = this.minecraft.player.getEntityReach();
					double distanceLimit = Math.max(d0, entityReach) + ClientConfig.cameraZoom * 0.5D;
					Vec3 hitPos = this.minecraft.hitResult.getLocation();
					
					if (hitPos.distanceToSqr(this.mainCamera.getPosition()) > distanceLimit * distanceLimit) {
						Vec3 cameraPos = this.mainCamera.getPosition();
						this.minecraft.hitResult = BlockHitResult.miss(hitPos, Direction.getNearest(cameraPos.x, cameraPos.y, cameraPos.z), BlockPos.containing(hitPos));
					}
				}
			}
		}
	}
}
