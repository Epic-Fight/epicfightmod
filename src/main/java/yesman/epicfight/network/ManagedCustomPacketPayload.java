package yesman.epicfight.network;

import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Maps;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.client.CPAnimatorControl;
import yesman.epicfight.network.client.CPChangePlayerMode;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.network.client.CPHandleSkillData;
import yesman.epicfight.network.client.CPModifyEntityModelYRot;
import yesman.epicfight.network.client.CPPairingAnimationRegistry;
import yesman.epicfight.network.client.CPSetPlayerTarget;
import yesman.epicfight.network.client.CPSetStamina;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.network.client.CPUpdatePlayerInput;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.network.server.SPAddLearnedSkill;
import yesman.epicfight.network.server.SPAnimatorControl;
import yesman.epicfight.network.server.SPChangeGamerule;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.network.server.SPChangePlayerMode;
import yesman.epicfight.network.server.SPChangeSkill;
import yesman.epicfight.network.server.SPClearSkills;
import yesman.epicfight.network.server.SPCreateTerrainFracture;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.network.server.SPHandleSkillData;
import yesman.epicfight.network.server.SPInitSkills;
import yesman.epicfight.network.server.SPMobEffectControl;
import yesman.epicfight.network.server.SPModifyExpandedEntityData;
import yesman.epicfight.network.server.SPModifyPlayerData;
import yesman.epicfight.network.server.SPPlayUISound;
import yesman.epicfight.network.server.SPRemoveSkillAndLearn;
import yesman.epicfight.network.server.SPSetAttackTarget;
import yesman.epicfight.network.server.SPSetRemotePlayerSkill;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.network.server.SPUpdatePlayerInput;

public interface ManagedCustomPacketPayload extends CustomPacketPayload {
	public static final Map<Class<? extends CustomPacketPayload>, CustomPacketPayload.Type<?>> PAYLOAD_TYPES = Maps.newHashMap();
	
	// Client bound payloads
	public static final CustomPacketPayload.Type<SPAbsorption> CLIENT_BOUND_ABSORPTION = registerPayloadType(SPAbsorption.class, EpicFightMod.MODID, "client_bound_absorption");
	public static final CustomPacketPayload.Type<SPAddLearnedSkill> CLIENT_BOUND_ADD_LEARNED_SKILL = registerPayloadType(SPAddLearnedSkill.class, EpicFightMod.MODID, "client_bound_add_learned_skill");
	public static final CustomPacketPayload.Type<SPHandleSkillData> CLIENT_BOUND_HANDLE_SKILL_DATA = registerPayloadType(SPHandleSkillData.class, EpicFightMod.MODID, "client_bound_register_or_remove_skill_data");
	public static final CustomPacketPayload.Type<SPAnimatorControl> CLIENT_BOUND_ANIMATOR_CONTROL = registerPayloadType(SPAnimatorControl.class, EpicFightMod.MODID, "client_bound_animator_control");
	public static final CustomPacketPayload.Type<SPChangeGamerule> CLIENT_BOUND_CHANGE_GAMERULE = registerPayloadType(SPChangeGamerule.class, EpicFightMod.MODID, "client_bound_change_gamerule");
	public static final CustomPacketPayload.Type<SPChangeLivingMotion> CLIENT_BOUND_CHANGE_LIVING_MOTION = registerPayloadType(SPChangeLivingMotion.class, EpicFightMod.MODID, "client_bound_change_living_motion");
	public static final CustomPacketPayload.Type<SPChangePlayerMode> CLIENT_BOUND_CHANGE_PLAYER_MODE = registerPayloadType(SPChangePlayerMode.class, EpicFightMod.MODID, "client_bound_change_player_mode");
	public static final CustomPacketPayload.Type<SPChangeSkill> CLIENT_BOUND_CHANGE_SKILL = registerPayloadType(SPChangeSkill.class, EpicFightMod.MODID, "client_bound_change_skill");
	public static final CustomPacketPayload.Type<SPClearSkills> CLIENT_BOUND_CLEAR_SKILLS = registerPayloadType(SPClearSkills.class, EpicFightMod.MODID, "client_bound_clear_skills");
	public static final CustomPacketPayload.Type<SPDatapackSync> CLIENT_BOUND_DATAPACK_SYNC = registerPayloadType(SPDatapackSync.class, EpicFightMod.MODID, "client_bound_sycn_datapack");
	public static final CustomPacketPayload.Type<SPEntityPairingPacket> CLIENT_BOUND_ENTITY_PAIRING = registerPayloadType(SPEntityPairingPacket.class, EpicFightMod.MODID, "client_bound_entity_pairing");
	public static final CustomPacketPayload.Type<SPCreateTerrainFracture> CLIENT_BOUND_CREATE_FRACTURE = registerPayloadType(SPCreateTerrainFracture.class, EpicFightMod.MODID, "client_bound_create_terrain_fracture");
	public static final CustomPacketPayload.Type<SPMobEffectControl> CLIENT_BOUND_MOB_EFFECT = registerPayloadType(SPMobEffectControl.class, EpicFightMod.MODID, "client_bound_share_mob_effect_to_tracking_players");
	public static final CustomPacketPayload.Type<SPModifyExpandedEntityData> CLIENT_BOUND_MODIFY_EXPANDED_ENTITY_DATA = registerPayloadType(SPModifyExpandedEntityData.class, EpicFightMod.MODID, "client_bound_modify_expanded_entity_data");
	public static final CustomPacketPayload.Type<SPModifyPlayerData.DisablePlayerYRot> CLIENT_BOUND_DISABLE_Y_ROT = registerPayloadType(SPModifyPlayerData.DisablePlayerYRot.class, EpicFightMod.MODID, "client_bound_disable_player_y_rot");
	public static final CustomPacketPayload.Type<SPModifyPlayerData.SetGrapplingTarget> CLIENT_BOUND_SET_GRAPPLING_TARGET = registerPayloadType(SPModifyPlayerData.SetGrapplingTarget.class, EpicFightMod.MODID, "client_bound_set_grappling_target");
	public static final CustomPacketPayload.Type<SPModifyPlayerData.SetLastAttackResult> CLIENT_BOUND_SET_LAST_ATTACK_RESULT = registerPayloadType(SPModifyPlayerData.SetLastAttackResult.class, EpicFightMod.MODID, "client_bound_set_lasst_attack_result");
	public static final CustomPacketPayload.Type<SPModifyPlayerData.SetPlayerMode> CLIENT_BOUND_SET_PLAYER_MODE = registerPayloadType(SPModifyPlayerData.SetPlayerMode.class, EpicFightMod.MODID, "client_bound_set_player_mode");
	public static final CustomPacketPayload.Type<SPModifyPlayerData.SetPlayerYRot> CLIENT_BOUND_SET_PLAYER_Y_ROT = registerPayloadType(SPModifyPlayerData.SetPlayerYRot.class, EpicFightMod.MODID, "client_bound_set_player_y_rot");
	public static final CustomPacketPayload.Type<SPPlayUISound> CLIENT_BOUND_PLAY_UI_SOUND = registerPayloadType(SPPlayUISound.class, EpicFightMod.MODID, "client_bound_play_ui_sound");
	public static final CustomPacketPayload.Type<SPRemoveSkillAndLearn> CLIENT_BOUND_REMOVE_SKILL = registerPayloadType(SPRemoveSkillAndLearn.class, EpicFightMod.MODID, "client_bound_remove_skill");
	public static final CustomPacketPayload.Type<SPSetAttackTarget> CLIENT_BOUND_SET_ATTACK_TARGET = registerPayloadType(SPSetAttackTarget.class, EpicFightMod.MODID, "client_bound_set_target");
	public static final CustomPacketPayload.Type<SPSetRemotePlayerSkill> CLIENT_BOUND_SET_REMOTE_PLAYER_SKILL = registerPayloadType(SPSetRemotePlayerSkill.class, EpicFightMod.MODID, "client_bound_set_remote_player_skill");
	public static final CustomPacketPayload.Type<SPSetSkillContainerValue> CLIENT_BOUND_SET_SKILL_CONTAINER_VALUE = registerPayloadType(SPSetSkillContainerValue.class, EpicFightMod.MODID, "client_bound_set_skill_container_values");
	public static final CustomPacketPayload.Type<SPSkillFeedback> CLIENT_BOUND_SKILL_FEEDBACK = registerPayloadType(SPSkillFeedback.class, EpicFightMod.MODID, "client_bound_skill_feedback");
	public static final CustomPacketPayload.Type<SPUpdatePlayerInput> CLIENT_BOUND_UPDATE_PLAYER_INPUT = registerPayloadType(SPUpdatePlayerInput.class, EpicFightMod.MODID, "client_bound_update_player_input");
	public static final CustomPacketPayload.Type<SPInitSkills> CLIENT_BOUND_INIT_SKILLS = registerPayloadType(SPInitSkills.class, EpicFightMod.MODID, "client_bound_init_skills");
	
	// Server bound payloads
	public static final CustomPacketPayload.Type<CPAnimatorControl> SERVER_BOUND_ANIMATOR_CONTROL = registerPayloadType(CPAnimatorControl.class, EpicFightMod.MODID, "server_bound_animator_control");
	public static final CustomPacketPayload.Type<CPChangePlayerMode> SERVER_BOUND_CHANGE_PLAYER_MODE = registerPayloadType(CPChangePlayerMode.class, EpicFightMod.MODID, "server_bound_change_player_mode");
	public static final CustomPacketPayload.Type<CPChangeSkill> SERVER_BOUND_CHANGE_SKILL = registerPayloadType(CPChangeSkill.class, EpicFightMod.MODID, "server_bound_change_skill");
	public static final CustomPacketPayload.Type<CPSkillRequest> SERVER_BOUND_EXECUTE_SKILL = registerPayloadType(CPSkillRequest.class, EpicFightMod.MODID, "server_bound_skill_request");
	public static final CustomPacketPayload.Type<CPModifyEntityModelYRot> SERVER_BOUND_MODIFY_ENTITY_PATCH_Y_ROT = registerPayloadType(CPModifyEntityModelYRot.class, EpicFightMod.MODID, "server_bound_modify_entitypatch_y_rot");
	public static final CustomPacketPayload.Type<CPHandleSkillData> SERVER_BOUND_MODIFY_SKILL_DATA = registerPayloadType(CPHandleSkillData.class, EpicFightMod.MODID, "server_bound_modify_skill_data");
	public static final CustomPacketPayload.Type<CPPairingAnimationRegistry> SERVER_BOUND_PAIRING_ANIMATION_REGISTRY = registerPayloadType(CPPairingAnimationRegistry.class, EpicFightMod.MODID, "server_bound_pairing_animation_registry");
	public static final CustomPacketPayload.Type<CPSetPlayerTarget> SERVER_BOUND_SET_PLAYER_TARGET = registerPayloadType(CPSetPlayerTarget.class, EpicFightMod.MODID, "server_bound_set_player_target");
	public static final CustomPacketPayload.Type<CPSetStamina> SERVER_BOUND_SET_STAMINA = registerPayloadType(CPSetStamina.class, EpicFightMod.MODID, "server_bound_set_stamina");
	public static final CustomPacketPayload.Type<CPUpdatePlayerInput> SERVER_BOUND_UPDATE_PLAYER_INPUT = registerPayloadType(CPUpdatePlayerInput.class, EpicFightMod.MODID, "server_bound_update_player_input");
	
	// Bi-directional payloads
	public static final CustomPacketPayload.Type<BiDirectionalAnimationVariable> BI_DIRECTIONAL_MODIFY_ANIMATION_VARIABLE = registerPayloadType(BiDirectionalAnimationVariable.class, EpicFightMod.MODID, "bi_directional_modify_animation_variable");
	public static final CustomPacketPayload.Type<BiDirectionalSyncAnimationPositionPacket> BI_DIRECTIONAL_SYNC_ANIMATION_POSITION = registerPayloadType(BiDirectionalSyncAnimationPositionPacket.class, EpicFightMod.MODID, "bi_directional_update_player_input");
	
	public static <T extends ManagedCustomPacketPayload> CustomPacketPayload.Type<T> registerPayloadType(Class<T> type, String modid, String payloadId) {
		CustomPacketPayload.Type<T> packet = new CustomPacketPayload.Type<T> (ResourceLocation.fromNamespaceAndPath(modid, payloadId));
		PAYLOAD_TYPES.put(type, packet);
		
		return packet;
	}
	
	@Override
	default CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		if (!PAYLOAD_TYPES.containsKey(this.getClass())) {
			throw new NoSuchElementException("Unregistered packet: " + this.getClass());
		}
		
		return PAYLOAD_TYPES.get(this.getClass());
	}
}
