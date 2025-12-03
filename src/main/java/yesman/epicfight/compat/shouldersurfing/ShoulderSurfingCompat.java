package yesman.epicfight.compat.shouldersurfing;

import com.github.exopandora.shouldersurfing.api.callback.ICameraCouplingCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Background:
 * The Shoulder Surfing Reloaded mod includes a camera decoupling feature, allowing the player to rotate the camera 360°
 * without rotating their controlled character. When the vanilla Attack key is pressed,
 * Shoulder Surfing automatically detects it and rotates the player to face the crosshair target as needed to attack
 * enemies.
 * <p>
 * <p>
 * The Epic Fight mod added a custom Attack keybind in this commit: <a href="https://github.com/Epic-Fight/epicfight/compare/e963d283e2ec...35e8aa9ea4ba#diff-9dc988ba2888a8eece19c042d1412fc79025c1c46370f5c0b5974a597c56ba5bL76">...</a>
 * to support controller mods (more details: <a href="https://github.com/Epic-Fight/epicfight/issues/1297">...</a>).
 * However, Shoulder Surfing Reloaded cannot detect this custom keybind if the vanilla Attack key
 * differs from the Epic Fight Attack key, which is usually the case when using a controller.
 * This means players must always disable the camera decoupling feature to make the mod playable with a controller
 * and are forced to use the same Attack key as in vanilla and Epic Fight.
 * <p>
 * <p>
 * This Shoulder Surfing plugin works around the issue by bypassing the
 * <a href="https://github.com/Exopandora/ShoulderSurfing/blob/7f0df83beb4f7158810e188150eb7e9812981529/common/src/main/java/com/github/exopandora/shouldersurfing/client/ShoulderSurfingImpl.java#L188-L191">
 * ShoulderSurfingImpl.isAttacking
 * </a> method,
 * allowing the Shoulder Surfing mod to support the Epic Fight Attack keybind as well.
 * <p>
 * <p>
 * This plugin always forces the player to turn when attacking, regardless of the Shoulder Surfing "player.turning" config.
 * By default, Shoulder Surfing only turns the player when a target is detected ("REQUIRES_TARGET"),
 * which works for vanilla mechanics but is incompatible with Epic Fight.
 * Overriding it to always turn ensures proper behavior with the Epic Fight Attack keybind and controller input.
 * <p>
 */
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused") // Referenced in src/main/resources/shouldersurfing_plugin.json
public class ShoulderSurfingCompat implements IShoulderSurfingPlugin {
    @Override
    public void register(IShoulderSurfingRegistrar registrar) {
        registrar.registerCameraCouplingCallback(new ForceCameraCouplingWhenAttackingCallback());
        registrar.registerCameraCouplingCallback(new ForceCameraCouplingWhenHoldingSkillCallback());
        EpicFightTpsCameraDisableState.disable(EpicFightTpsCameraDisabledReason.ShoulderSurfing);

        EpicFightClientHooks.Camera.BUILD_TRANSFORM_PRE.registerEvent(ShoulderSurfingCompat::buildCameraTransform);

        EpicFightClientHooks.Camera.LOCK_ON_TICK.registerEvent(ShoulderSurfingCompat::lockOnTick);
    }

    private static class ForceCameraCouplingWhenAttackingCallback implements ICameraCouplingCallback {
        @Override
        public boolean isForcingCameraCoupling(Minecraft minecraft) {
            return InputManager.isActionActive(EpicFightInputAction.ATTACK) || InputManager.isActionActive(MinecraftInputAction.ATTACK_DESTROY);
        }
    }

    private static class ForceCameraCouplingWhenHoldingSkillCallback implements ICameraCouplingCallback {
        @Override
        public boolean isForcingCameraCoupling(Minecraft minecraft) {
            LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
            if (localPlayerPatch == null) {
                return false;
            }

            // Forces camera coupling when the player is holding any holdable skill,
            // including Demolition Leap, without directly referencing specific skills.
            return localPlayerPatch.isHoldingAny();
        }
    }

    private static void buildCameraTransform(BuildCameraTransform.Pre event) {
        IShoulderSurfing instance = ShoulderSurfing.getInstance();

        // Turn completely off epic fight camera transform modification when Shoulder Surfing camera activated
        if (instance.isShoulderSurfing()) {
            if (event.getCameraApi().isLockingOnTarget()) {
                // Copy the Lock-on rotations to the SSR camera
                float camXRot = Mth.rotLerp(event.getPartialTick(), event.getCameraApi().getCameraXRotO(), event.getCameraApi().getCameraXRot());
                float camYRot = Mth.rotLerp(event.getPartialTick(), event.getCameraApi().getCameraYRotO(), event.getCameraApi().getCameraYRot());

                instance.getCamera().setXRot(camXRot);
                instance.getCamera().setYRot(camYRot);
            }

            event.cancel();
        }
    }

    private static void lockOnTick(LockOnEvent.Tick event) {
        IShoulderSurfing instance = ShoulderSurfing.getInstance();

        // Calculates lock-on rotations based on the SSR's camera position, store those rotations to Epic Fight camera API's rotations
        // since they will eventually be written to SSR's camera rotation in BUILD_TRANSFORM_PRE.
        if (!instance.isShoulderSurfing()) {
        	return;
        }
        LocalPlayer localPlayer = event.getCameraApi().getMinecraft().player;
        double toTargetDistanceSqr = localPlayer.position().distanceToSqr(event.getLockOnTarget().position());

        // Lerp the start and end location of the camera arm for lock-on based on the distance between the player and the focusing entity
        Vec3 lockStart = MathUtils.lerpVector(localPlayer.getEyePosition(), event.getCameraApi().getMinecraft().gameRenderer.getMainCamera().getPosition(), (float)Mth.clampedMap(toTargetDistanceSqr, 1.0F, 18.0F, 0.2F, 1.0F));
        Vec3 lockEnd = MathUtils.lerpVector(event.getLockOnTarget().getEyePosition(), event.getLockOnTarget().getBoundingBox().getCenter(), (float)Mth.clampedMap(toTargetDistanceSqr, 0.0F, 18.0F, 0.5F, 1.0F));

        float clamp = 30.0F;
        Vec3 toTarget = lockEnd.subtract(lockStart);
        float xRot = (float)MathUtils.getXRotOfVector(toTarget);
        float yRot = (float)MathUtils.getYRotOfVector(toTarget);

        CameraType cameraType = event.getCameraApi().getMinecraft().options.getCameraType();
        if (!cameraType.isFirstPerson()) xRot = Mth.clamp(xRot, -clamp, clamp);

        float xLerp = Mth.clamp(Mth.wrapDegrees(xRot - instance.getCamera().getXRot()) * 0.4F, -clamp, clamp);
        float yLerp = Mth.clamp(Mth.wrapDegrees(yRot - instance.getCamera().getYRot()) * 0.4F, -clamp, clamp);
        Vec3 playerToTarget = lockEnd.subtract(localPlayer.getEyePosition());
        event.getCameraApi().setCameraRotations(instance.getCamera().getXRot() + xLerp, instance.getCamera().getYRot() + yLerp, false);
        event.setXRot((float)MathUtils.getXRotOfVector(playerToTarget));
        event.setYRot((float)MathUtils.getYRotOfVector(playerToTarget));
    }
}
