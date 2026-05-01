package yesman.epicfight.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.client.*;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.network.common.BiDirectionalSyncAnimationPositionPacket;
import yesman.epicfight.network.common.BiDirectionalSyncEmoteSlots;
import yesman.epicfight.network.server.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public class EpicFightNetworkManager {
	private static final String PROTOCOL_VERSION = "1";
	
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
	    final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
	    
	    registrar
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_ABSORPTION
	    		, SPAbsorption.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleAbsorption
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_ADD_LEARNED_SKILL
	    		, SPAddLearnedSkill.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleAddLearnedSkill
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_HANDLE_SKILL_DATA
	    		, SPHandleSkillData.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSkillData
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_ANIMATOR_CONTROL
	    		, SPAnimatorControl.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleAnimatorControl
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_GAMERULE
	    		, SPChangeGamerule.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleChangeGameRule
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_LIVING_MOTION
	    		, SPChangeLivingMotion.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleChangeLivingMotion
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_PLAYER_MODE
	    		, SPChangePlayerMode.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleChangePlayerMode
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CHANGE_SKILL
	    		, SPChangeSkill.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleChangeSkill
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CLEAR_SKILLS
	    		, SPClearSkills.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleClearSkills
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_DATAPACK_SYNC
	    		, SPDatapackSync.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleDataPack
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_ENTITY_PAIRING
	    		, SPEntityPairingPacket.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleEntityPairing
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_CREATE_FRACTURE
	    		, SPCreateTerrainFracture.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleFracture
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_Y_ROT
	    		, SPModifyPlayerData.SetPlayerYRot.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleModelYRot
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_DISABLE_Y_ROT
	    		, SPModifyPlayerData.DisablePlayerYRot.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleDisableModelYRot
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_LAST_ATTACK_RESULT
	    		, SPModifyPlayerData.SetLastAttackResult.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetLastAttackResult
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_PLAYER_MODE
	    		, SPModifyPlayerData.SetPlayerMode.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetPlayerMode
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_GRAPPLING_TARGET
	    		, SPModifyPlayerData.SetGrapplingTarget.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetGrapplingTarget
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_MOB_EFFECT
	    		, SPMobEffectControl.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleMobEffect
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_MODIFY_EXPANDED_ENTITY_DATA
	    		, SPModifyExpandedEntityData.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleModifyExpandedEntityData
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_PLAY_UI_SOUND
	    		, SPPlayUISound.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handlePlayUiSound
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_REMOVE_SKILL
	    		, SPRemoveSkillAndLearn.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleRemoveSkill
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_ATTACK_TARGET
	    		, SPSetAttackTarget.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetAttackTarget
	    	)
	    	.playToClient(
	    		  SPSetRemotePlayerSkill.CLIENT_BOUND_SET_REMOTE_PLAYER_SKILL
	    		, SPSetRemotePlayerSkill.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetRemotePlayerSkill
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SET_SKILL_CONTAINER_VALUE
	    		, SPSetSkillContainerValue.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSetSkillContainerValue
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_SKILL_FEEDBACK
	    		, SPSkillFeedback.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleSkillFeedback
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_UPDATE_PLAYER_INPUT
	    		, SPUpdatePlayerInput.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleUpdatePlayerInput
	    	)
	    	.playToClient(
	    		  ManagedCustomPacketPayload.CLIENT_BOUND_INIT_SKILLS
	    		, SPInitSkills.STREAM_CODEC
	    		, EpicFightClientBoundPayloadHandler::handleInitSkills
	    	)
	    ;
	    
	    registrar
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_ANIMATOR_CONTROL
	    		, CPAnimatorControl.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleAnimatorControl
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_CHANGE_PLAYER_MODE
	    		, CPChangePlayerMode.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleChangePlayerMode
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_CHANGE_SKILL
	    		, CPChangeSkill.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleChangeSkill
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_EXECUTE_SKILL
	    		, CPSkillRequest.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleExecuteSkill
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_MODIFY_ENTITY_PATCH_Y_ROT
	    		, CPModifyEntityModelYRot.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleModifyPlayerModelYRot
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_MODIFY_SKILL_DATA
	    		, CPHandleSkillData.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleSkillData
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_PAIRING_ANIMATION_REGISTRY
	    		, CPPairingAnimationRegistry.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handlePairingAnimationRegistry
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_SET_PLAYER_TARGET
	    		, CPSetPlayerTarget.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleSetPlayerTarget
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_SET_STAMINA
	    		, CPSetStamina.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleSetStamina
	    	)
	    	.playToServer(
	    		  ManagedCustomPacketPayload.SERVER_BOUND_UPDATE_PLAYER_INPUT
	    		, CPUpdatePlayerInput.STREAM_CODEC
	    		, EpicFightServerBoundPayloadHandler::handleUpdatePlayerInput
	    	)
	    ;
	    
	    registrar
	    	.playBidirectional(
	    		  ManagedCustomPacketPayload.BI_DIRECTIONAL_MODIFY_ANIMATION_VARIABLE
	    		, BiDirectionalAnimationVariable.STREAM_CODEC
	    		, new DirectionalPayloadHandler<> (
	    			  EpicFightClientBoundPayloadHandler::handleAnimationVariablePacket
	    			, EpicFightServerBoundPayloadHandler::handleAnimationVariablePacket
	    		)
	    	)
	    	.playBidirectional(
    			  ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_ANIMATION_POSITION
  	    		, BiDirectionalSyncAnimationPositionPacket.STREAM_CODEC
  	    		, new DirectionalPayloadHandler<> (
  	    			  EpicFightClientBoundPayloadHandler::handleSyncAnimationPosition
  	    			, EpicFightServerBoundPayloadHandler::handleSyncAnimationPosition
  	    		)
	    	)
            .playBidirectional(
                  ManagedCustomPacketPayload.BI_DIRECTIONAL_SYNC_EMOTE_SLOTS
                , BiDirectionalSyncEmoteSlots.STREAM_CODEC
                , new DirectionalPayloadHandler<> (
                      EpicFightClientBoundPayloadHandler::handleSyncEmoteSlot
                    , EpicFightServerBoundPayloadHandler::handleSyncEmoteSlot
                )
            )
	    ;
	}
	
	@SuppressWarnings("unchecked")
	public static FriendlyByteBuf encodeObjectToBuffer(StreamEncoder<ByteBuf, ?> encoder, Object value) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		((StreamEncoder<ByteBuf, Object>)encoder).encode(buf, value);
		return buf;
	}
	
	public static void sendToServer(CustomPacketPayload message, CustomPacketPayload... others) {
		PacketDistributor.sendToServer(message, others);
	}
	
	public static void sendToAll(CustomPacketPayload message, CustomPacketPayload... others) {
		PacketDistributor.sendToAllPlayers(message, others);
	}

	public static void sendToAllPlayerTrackingThisEntity(CustomPacketPayload message, Entity entity, CustomPacketPayload... others) {
		PacketDistributor.sendToPlayersTrackingEntity(entity, message, others);
	}
	
	public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player, CustomPacketPayload... others) {
		PacketDistributor.sendToPlayer(player, message, others);
	}
	
	public static void sendToAllPlayerTrackingThisEntityWithSelf(CustomPacketPayload message, ServerPlayer entity, CustomPacketPayload... others) {
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message, others);
	}
	
	public static void sendToAllPlayerTrackingThisChunkWithSelf(CustomPacketPayload message, ServerLevel serverLevel, ChunkPos chunkPos, CustomPacketPayload... others) {
		PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, message, others);
	}
	
	public static class PayloadBundleBuilder {
		public static PayloadBundleBuilder create() {
			return new PayloadBundleBuilder();
		}
		
		public static PayloadBundleBuilder beginWith(CustomPacketPayload payload) {
			return new PayloadBundleBuilder().and(payload);
		}
		
		private final List<CustomPacketPayload> payloads = new ArrayList<> ();
		
		public PayloadBundleBuilder and(CustomPacketPayload payload) {
			this.payloads.add(payload);
			return this;
		}
		
		public void send(BiConsumer<CustomPacketPayload, CustomPacketPayload[]> sendTo) {
			if (this.payloads.size() == 1) {
				sendTo.accept(this.payloads.getFirst(), new CustomPacketPayload[0]);
			} else if (!this.payloads.isEmpty()) {
				sendTo.accept(this.payloads.getFirst(), this.payloads.subList(1, this.payloads.size()).toArray(new CustomPacketPayload[0]));
			}
		}
	}
}
