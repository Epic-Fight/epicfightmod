package yesman.epicfight.api.event.impl;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.storage.PlayerDataStorage;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.StartUsingItemEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import java.io.File;

public final class VanillaPlayerEventHooks {
    /// Called when a player loose an arrow
    ///
    /// @see BowItem#releaseUsing
    /// @see CrossbowItem#performShooting
    public static void onLooseArrow(Player player) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerpatch -> {
            if (playerpatch.isLogicalClient()) {
                playerpatch.getAnimator().playShootingAnimation();
            } else {
                EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAnimatorControl(AbstractAnimatorControl.Action.SHOT, Animations.EMPTY_ANIMATION, player.getId(), 0.0F, false), player);
            }
        });
    }

    /// Called when a player starts tracking an entity in the world
    ///
    /// @see ServerEntity#addPairing
    public static void onStartTracking(Entity target, ServerPlayer player) {
        // Sync absorption attribute
        if (target instanceof LivingEntity livingEntity) {
            if (livingEntity.getAbsorptionAmount() > 0.0F) {
                EpicFightNetworkManager.sendToPlayer(new SPAbsorption(target.getId(), livingEntity.getAbsorptionAmount()), player);
            }
        }

        EpicFightCapabilities.getUnparameterizedEntityPatch(target, EntityPatch.class).ifPresent(entitypatch -> {
            entitypatch.onStartTracking(player);
        });
    }

    /// Called when a player starts tracking an entity in the world
    ///
    /// @see ServerEntity#removePairing
    public static void onStopTracking(Entity target, ServerPlayer player) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(target, EntityPatch.class).ifPresent(entitypatch -> {
            entitypatch.onStopTracking(player);
        });
    }

    public static final PlayerPatch.PlayerMode[] PLAYER_MODES = PlayerPatch.PlayerMode.values();

    /// Called when a player is loaded from the world file
    ///
    /// @see PlayerDataStorage#load(Player)
    /// @see PlayerList#load
    public static void onLoad(Player player, File playerDirectory, String playerUUID) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(playerpatch -> {
            File file = new File(playerDirectory, playerUUID + ".dat");

            if (!file.exists()) {
                int initialMode = Math.min(EpicFightGameRules.INITIAL_PLAYER_MODE.getRuleValue(player.level()), PLAYER_MODES.length - 1);
                playerpatch.toMode(PLAYER_MODES[initialMode], true);
            }
        });
    }

    /// Called when a plyer cloned from an old one, by respawning
    ///
    /// @param originalPlayer   an old player to copy
    /// @param newPlayer        a new player being copied
    /// @param byDeath          whether this copy is caused by the player respawning from death
    ///
    /// @see ServerPlayer#restoreFrom(ServerPlayer, boolean)
    public static void onCloned(Player originalPlayer, Player newPlayer, boolean byDeath) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(originalPlayer, ServerPlayerPatch.class).ifPresent(oldCap -> {
            EpicFightCapabilities.getPlayerPatchAsOptional(newPlayer).ifPresent(newCap -> {
                if ((!byDeath || EpicFightGameRules.KEEP_SKILLS.getRuleValue(originalPlayer.level()))) {
                    newCap.copyOldData(oldCap, byDeath);
                }

                newCap.toMode(oldCap.getPlayerMode(), false);
            });
        });
    }

    /// Called when a player changes dimension he belongs to
    ///
    /// @see ServerPlayer#changeDimension
    public static void onChagneDimension(Player player) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(playerpatch -> {
            playerpatch.getAnimator().resetLivingAnimations();
            playerpatch.modifyLivingMotionByCurrentItem(true);

            EpicFightNetworkManager.PayloadBundleBuilder packetBundleBuilder = EpicFightNetworkManager.PayloadBundleBuilder.create();

            playerpatch.getPlayerSkills().listSkillContainers().filter(SkillContainer::hasSkill).forEach(skillContainer -> {
                skillContainer.getSkill().onTracked(skillContainer, packetBundleBuilder);
            });

            packetBundleBuilder.send((start, others) -> {
                EpicFightNetworkManager.sendToPlayer(start, playerpatch.getOriginal(), others);
            });
        });
    }

    /// Called when a player starts using an item in hand
    ///
    /// @see ServerPlayerGameMode#useItem
    ///
    /// @return whether cancel the event
    public static boolean onUseItemInServerSide(Player player) {
        // Client item use event is fired in {@link ClientEvents#rightClickItemClient}
        if (player.level().isClientSide()) {
            return false;
        }

        ServerPlayerPatch playerpatch = EpicFightCapabilities.getServerPlayerPatch((ServerPlayer)player);

        if (playerpatch != null) {
            ItemStack itemstack = playerpatch.getOriginal().getOffhandItem();

            if (!playerpatch.getEntityState().canUseItem()) {
                return true;
            } else if (itemstack.getUseAnimation() == UseAnim.NONE || !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand()) {
                StartUsingItemEvent startUsingItemEvent = new StartUsingItemEvent(playerpatch);
                EpicFightEventHooks.Player.USE_ITEM.postWithListener(startUsingItemEvent, playerpatch.getEventListener());

                if (playerpatch.getEntityState().movementLocked()) {
                    startUsingItemEvent.cancel();
                }

                return startUsingItemEvent.isCanceled();
            }
        }

        return false;
    }

    private VanillaPlayerEventHooks() {}
}
