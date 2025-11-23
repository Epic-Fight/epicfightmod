package yesman.epicfight.compat;

import com.github.exopandora.shouldersurfing.api.callback.ICameraCouplingCallback;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.InputManager;
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
    }

    private static class ForceCameraCouplingWhenAttackingCallback implements ICameraCouplingCallback {
        @Override
        public boolean isForcingCameraCoupling(Minecraft minecraft) {
            return InputManager.isActionActive(EpicFightInputAction.ATTACK) || InputManager.isActionActive(EpicFightInputAction.VANILLA_ATTACK_DESTROY);
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
}
