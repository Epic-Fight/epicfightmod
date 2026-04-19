package yesman.epicfight.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.config.ClientConfig;

@Mixin(value = GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow @Final
    private Camera mainCamera;
    @Shadow @Final
    Minecraft minecraft;

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"
        ),
        method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V"
    )
    private void epicfight$renderLevel(GameRenderer instance, float partialTicks, Operation<Void> original) {
        if (EpicFightCameraAPI.getInstance().isTPSMode()) {
            this.pickInTPSPerspective();
        } else {
            original.call(instance, partialTicks);
        }
    }

    /**
     * Code copy from {@link GameRenderer#pick(float)}
     */
    @Unique
    private void pickInTPSPerspective() {
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            if (this.minecraft.level != null && this.minecraft.player != null) {
                this.minecraft.hitResult = EpicFightCameraAPI.getInstance().getCrosshairHitResult();
                this.minecraft.crosshairPickEntity = EpicFightCameraAPI.getInstance().getFocusingEntity();

                if (this.minecraft.hitResult != null) {
                    double d0 = this.minecraft.player.blockInteractionRange();
                    double entityReach = this.minecraft.player.entityInteractionRange();
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