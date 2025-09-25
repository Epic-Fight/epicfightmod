package yesman.epicfight.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.network.client.*;
import yesman.epicfight.network.common.*;
import yesman.epicfight.network.server.*;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public interface EpicFightServerBoundPayloadHandler {
	public static <T> void handleAnimationVariablePacket(final BiDirectionalAnimationVariable data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			data.commonProcess(playerpatch);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(data, playerpatch.getOriginal());
		});
	}
	
	public static void handleAnimatorControl(final CPAnimatorControl data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			if (!data.isClientOnly()) {
				data.animationVariables().forEach(animationVariable -> handleAnimationVariablePacket(animationVariable, context));
				data.commonProcess(playerpatch);
			}
			
			SPAnimatorControl payload = new SPAnimatorControl(data.action(), data.animation(), playerpatch.getOriginal().getId(), data.transitionTimeModifier(), data.pause());
			payload.animationVariables().addAll(data.animationVariables());
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(payload, playerpatch.getOriginal());
			
			if (data.responseToSender()) {
				payload.animationVariables().clear();
				EpicFightNetworkManager.sendToPlayer(payload, playerpatch.getOriginal());
			}
		});
	}
	
	public static void handleChangePlayerMode(final CPChangePlayerMode data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.toMode(data.mode(), false);
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.setPlayerMode(playerpatch.getOriginal().getId(), playerpatch.getPlayerMode()), playerpatch.getOriginal());
		});
	}
	
	public static void handleChangeSkill(final CPChangeSkill data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			Skill skill = Skill.skillOrNull(data.skill());
			playerpatch.getSkill(data.skillSlot()).setSkill(skill);
			
			if (skill != null) {
				if (skill.getCategory().learnable()) {
					playerpatch.getPlayerSkills().addLearnedSkill(skill);
				}
				
				if (data.consumeXp()) {
					playerpatch.getOriginal().giveExperienceLevels(-skill.getRequiredXp());
				} else {
					if (!playerpatch.getOriginal().isCreative()) {
						playerpatch.getOriginal().getInventory().removeItem(playerpatch.getOriginal().getInventory().getItem(data.itemSlotIndex()));
					}
				}
			}
			
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSetRemotePlayerSkill(data.skillSlot(), playerpatch.getOriginal().getId(), data.skill()), playerpatch.getOriginal());
		});
	}
	
	public static void handlePairingAnimationRegistry(final CPPairingAnimationRegistry data, final IPayloadContext context) {
		AnimationManager.getInstance().validateClientAnimationRegistry(data, ((ServerPlayer)context.player()).connection);
	}
	
	public static void handleExecuteSkill(final CPSkillRequest data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			SkillContainer skillContainer = playerpatch.getSkill(data.skillSlot());
			
			switch (data.workType()) {
				case CAST -> skillContainer.requestCasting(playerpatch, data.arguments());
				case CANCEL -> skillContainer.requestCancel(playerpatch, data.arguments());
				case HOLD_START -> skillContainer.requestHold(playerpatch, data.arguments());
			}
		});
	}
	
	public static void handleModifyPlayerModelYRot(final CPModifyEntityModelYRot data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			if (data.disable()) {
				playerpatch.disableModelYRot(false);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.disablePlayerYRot(playerpatch.getOriginal().getId()), playerpatch.getOriginal());
			} else {
				playerpatch.setModelYRot(data.modelYRot(), false);
				EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(SPModifyPlayerData.setPlayerYRot(playerpatch.getOriginal().getId(), data.modelYRot()), playerpatch.getOriginal());
			}
		});
	}
	
	public static void handleSkillData(final CPHandleSkillData data, final IPayloadContext context) {
		Player player = context.player();
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerpatch -> {
			SkillDataManager dataManager = playerpatch.getSkill(data.skillSlot()).getDataManager();
			Object value = data.skillDataKey().value().decode(data.buffer());
			dataManager.setDataRawtype(data.skillDataKey(), value);
		});
	}
	
	public static void handleSetPlayerTarget(final CPSetPlayerTarget data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(entitypatch -> {
			Entity entity = entitypatch.getOriginal().level().getEntity(data.targetEntityId());
			
			if (entity instanceof LivingEntity livingEntity) {
				entitypatch.setAttackTarget(livingEntity);
			} else if (entity == null) {
				entitypatch.setAttackTarget(null);
			}
		});
	}
	
	public static void handleSetStamina(final CPSetStamina data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.setStamina(data.consumption());
			
			if (data.resetActionTick()) {
				playerpatch.resetActionTick();
			}
		});
	}
	
	public static void handleUpdatePlayerInput(final CPUpdatePlayerInput data, final IPayloadContext context) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(context.player(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.dx = data.strafe();
			playerpatch.dz = data.forward();
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPUpdatePlayerInput(data.entityId(), data.forward(), data.strafe()), playerpatch.getOriginal());
		});
	}
	
	public static void handleSyncAnimationPosition(final BiDirectionalSyncAnimationPositionPacket data, final IPayloadContext context) {
		Entity entity = context.player().level().getEntity(data.entityId());
		
		if (entity != null && entity instanceof Player player) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new BiDirectionalSyncAnimationPositionPacket(entity.getId(), data.elapsedTime(), data.position(), data.lerpSteps()), player);
		}
	}
}
