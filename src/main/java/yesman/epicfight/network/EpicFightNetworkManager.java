package yesman.epicfight.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.client.*;
import yesman.epicfight.network.server.*;

public class EpicFightNetworkManager {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE =
		NetworkRegistry.newSimpleChannel(
                EpicFightMod.identifier("network_manager"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

	public static void sendToServer(Object message) {
		INSTANCE.sendToServer(message);
	}
	
	public static void sendToClient(Object message, PacketTarget packetTarget, Object... messages) {
		packetTarget.send(createVanillaPacket(message, packetTarget, messages));
	}
	
	public static void sendToAll(Object message, Object... messages) {
		sendToClient(message, PacketDistributor.ALL.noArg(), messages);
	}

	public static void sendToAllPlayerTrackingThisEntity(Object message, Entity entity, Object... messages) {
		sendToClient(message, PacketDistributor.TRACKING_ENTITY.with(() -> entity), messages);
	}
	
	public static void sendToPlayer(Object message, ServerPlayer player, Object... messages) {
		sendToClient(message, PacketDistributor.PLAYER.with(() -> player), messages);
	}
	
	public static void sendToAllPlayerTrackingThisEntityWithSelf(Object message, ServerPlayer entity, Object... messages) {
		sendToClient(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), messages);
	}
	
	public static void sendToAllPlayerTrackingThisChunkWithSelf(Object message, LevelChunk chunk, Object... messages) {
		sendToClient(message, PacketDistributor.TRACKING_CHUNK.with(() -> chunk), messages);
	}
	
	@SuppressWarnings("unchecked")
	private static Packet<?> createVanillaPacket(Object message, PacketTarget packetTarget, Object... messages) {
		if (messages == null || messages.length == 0) {
			return INSTANCE.toVanillaPacket(message, packetTarget.getDirection());
		} else {
			List<Packet<ClientGamePacketListener>> packets = new ArrayList<> ();
			packets.add((Packet<ClientGamePacketListener>)INSTANCE.toVanillaPacket(message, packetTarget.getDirection()));
			
			for (Object bundleMessage : messages) {
				packets.add((Packet<ClientGamePacketListener>)INSTANCE.toVanillaPacket(bundleMessage, packetTarget.getDirection()));
			}
			
			return new ClientboundBundlePacket(packets);
		}
	}
	
	public static void registerPackets() {
		int id = 0;
		
		INSTANCE.registerMessage(id++, CPSkillRequest.class, CPSkillRequest::toBytes, CPSkillRequest::fromBytes, CPSkillRequest::handle);
		INSTANCE.registerMessage(id++, CPAnimatorControl.class, CPAnimatorControl::toBytes, CPAnimatorControl::fromBytes, CPAnimatorControl::handle);
		INSTANCE.registerMessage(id++, CPModifyEntityModelYRot.class, CPModifyEntityModelYRot::toBytes, CPModifyEntityModelYRot::fromBytes, CPModifyEntityModelYRot::handle);
		INSTANCE.registerMessage(id++, CPChangePlayerMode.class, CPChangePlayerMode::toBytes, CPChangePlayerMode::fromBytes, CPChangePlayerMode::handle);
		INSTANCE.registerMessage(id++, CPUpdatePlayerInput.class, CPUpdatePlayerInput::toBytes, CPUpdatePlayerInput::fromBytes, CPUpdatePlayerInput::handle);
		INSTANCE.registerMessage(id++, CPSetPlayerTarget.class, CPSetPlayerTarget::toBytes, CPSetPlayerTarget::fromBytes, CPSetPlayerTarget::handle);
		INSTANCE.registerMessage(id++, CPChangeSkill.class, CPChangeSkill::toBytes, CPChangeSkill::fromBytes, CPChangeSkill::handle);
		INSTANCE.registerMessage(id++, CPModifySkillData.class, CPModifySkillData::toBytes, CPModifySkillData::fromBytes, CPModifySkillData::handle);
		INSTANCE.registerMessage(id++, CPCheckAnimationRegistryMatches.class, CPCheckAnimationRegistryMatches::toBytes, CPCheckAnimationRegistryMatches::fromBytes, CPCheckAnimationRegistryMatches::handle);
		INSTANCE.registerMessage(id++, CPAnimationVariablePacket.class, CPAnimationVariablePacket::toBytes, CPAnimationVariablePacket::fromBytes, CPAnimationVariablePacket::handle);
		INSTANCE.registerMessage(id++, CPSetStamina.class, CPSetStamina::toBytes, CPSetStamina::fromBytes, CPSetStamina::handle);
		INSTANCE.registerMessage(id++, CPSyncPlayerAnimationPosition.class, CPSyncPlayerAnimationPosition::toBytes, CPSyncPlayerAnimationPosition::fromBytes, CPSyncPlayerAnimationPosition::handle);
		
		INSTANCE.registerMessage(id++, SPChangeSkill.class, SPChangeSkill::toBytes, SPChangeSkill::fromBytes, SPChangeSkill::handle);
		INSTANCE.registerMessage(id++, SPSkillExecutionFeedback.class, SPSkillExecutionFeedback::toBytes, SPSkillExecutionFeedback::fromBytes, SPSkillExecutionFeedback::handle);
		INSTANCE.registerMessage(id++, SPEntityPairingPacket.class, SPEntityPairingPacket::toBytes, SPEntityPairingPacket::fromBytes, SPEntityPairingPacket::handle);
		INSTANCE.registerMessage(id++, SPChangeLivingMotion.class, SPChangeLivingMotion::toBytes, SPChangeLivingMotion::fromBytes, SPChangeLivingMotion::handle);
		INSTANCE.registerMessage(id++, SPSetSkillContainerValue.class, SPSetSkillContainerValue::toBytes, SPSetSkillContainerValue::fromBytes, SPSetSkillContainerValue::handle);
		INSTANCE.registerMessage(id++, SPModifyPlayerData.class, SPModifyPlayerData::toBytes, SPModifyPlayerData::fromBytes, SPModifyPlayerData::handle);
		INSTANCE.registerMessage(id++, SPAnimatorControl.class, SPAnimatorControl::toBytes, SPAnimatorControl::fromBytes, SPAnimatorControl::handle);
		INSTANCE.registerMessage(id++, SPPlayAnimationAndSetTarget.class, SPPlayAnimationAndSetTarget::toBytes, SPPlayAnimationAndSetTarget::fromBytes, SPPlayAnimationAndSetTarget::handle);
		INSTANCE.registerMessage(id++, SPMoveAndPlayAnimation.class, SPMoveAndPlayAnimation::toBytes, SPMoveAndPlayAnimation::fromBytes, SPMoveAndPlayAnimation::handle);
		INSTANCE.registerMessage(id++, SPPotion.class, SPPotion::toBytes, SPPotion::fromBytes, SPPotion::handle);
		INSTANCE.registerMessage(id++, SPModifySkillData.class, SPModifySkillData::toBytes, SPModifySkillData::fromBytes, SPModifySkillData::handle);
		INSTANCE.registerMessage(id++, SPChangeGamerule.class, SPChangeGamerule::toBytes, SPChangeGamerule::fromBytes, SPChangeGamerule::handle);
		INSTANCE.registerMessage(id++, SPChangePlayerMode.class, SPChangePlayerMode::toBytes, SPChangePlayerMode::fromBytes, SPChangePlayerMode::handle);
		INSTANCE.registerMessage(id++, SPAddLearnedSkill.class, SPAddLearnedSkill::toBytes, SPAddLearnedSkill::fromBytes, SPAddLearnedSkill::handle);
		INSTANCE.registerMessage(id++, SPDatapackSync.class, SPDatapackSync::toBytes, SPDatapackSync::fromBytes, SPDatapackSync::handle);
		INSTANCE.registerMessage(id++, SPSetAttackTarget.class, SPSetAttackTarget::toBytes, SPSetAttackTarget::fromBytes, SPSetAttackTarget::handle);
		INSTANCE.registerMessage(id++, SPClearSkills.class, SPClearSkills::toBytes, SPClearSkills::fromBytes, SPClearSkills::handle);
		INSTANCE.registerMessage(id++, SPRemoveSkillAndLearn.class, SPRemoveSkillAndLearn::toBytes, SPRemoveSkillAndLearn::fromBytes, SPRemoveSkillAndLearn::handle);
		INSTANCE.registerMessage(id++, SPFracture.class, SPFracture::toBytes, SPFracture::fromBytes, SPFracture::handle);
		INSTANCE.registerMessage(id++, SPUpdatePlayerInput.class, SPUpdatePlayerInput::toBytes, SPUpdatePlayerInput::fromBytes, SPUpdatePlayerInput::handle);
		INSTANCE.registerMessage(id++, SPAnimationVariablePacket.class, SPAnimationVariablePacket::toBytes, SPAnimationVariablePacket::fromBytes, SPAnimationVariablePacket::handle);
		INSTANCE.registerMessage(id++, SPAbsorption.class, SPAbsorption::toBytes, SPAbsorption::fromBytes, SPAbsorption::handle);
		INSTANCE.registerMessage(id++, SPSyncAnimationPosition.class, SPSyncAnimationPosition::toBytes, SPSyncAnimationPosition::fromBytes, SPSyncAnimationPosition::handle);
		INSTANCE.registerMessage(id++, SPPlayUISound.class, SPPlayUISound::toBytes, SPPlayUISound::fromBytes, SPPlayUISound::handle);
		INSTANCE.registerMessage(id++, SPSetRemotePlayerSkill.class, SPSetRemotePlayerSkill::toBytes, SPSetRemotePlayerSkill::fromBytes, SPSetRemotePlayerSkill::handle);
		INSTANCE.registerMessage(id++, SPInitSkills.class, SPInitSkills::toBytes, SPInitSkills::fromBytes, SPInitSkills::handle);
	}
	
	public static class PayloadBundleBuilder {
		public static PayloadBundleBuilder create() {
			return new PayloadBundleBuilder();
		}
		
		public static PayloadBundleBuilder beginWith(Object payload) {
			return new PayloadBundleBuilder().and(payload);
		}
		
		private final List<Object> payloads = new ArrayList<> ();
		
		public PayloadBundleBuilder and(Object payload) {
			this.payloads.add(payload);
			return this;
		}
		
		public void send(BiConsumer<Object, Object[]> sendTo) {
            if (this.payloads.size() == 1) {
				sendTo.accept(this.payloads.get(0), new Object[0]);
			} else if (!payloads.isEmpty()) {
				sendTo.accept(this.payloads.get(0), this.payloads.subList(1, this.payloads.size()).toArray(new Object[0]));
			}
		}
	}
}