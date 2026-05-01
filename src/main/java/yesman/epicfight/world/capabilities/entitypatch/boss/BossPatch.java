package yesman.epicfight.world.capabilities.entitypatch.boss;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.UUID;

public interface BossPatch<T extends Entity> {
	BossEvent getBossEvent();
	
	public T getOriginal();
	
	default void recordBossEventOwner(ServerPlayer trackingPlayer) {
		SPEntityPairingPacket packet = new SPEntityPairingPacket(this.getOriginal().getId(), EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER);
		packet.buffer().writeBoolean(true);
		packet.buffer().writeUUID(this.getBossEvent().getId());
		EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
	}
	
	default void removeBossEventOwner(ServerPlayer trackingPlayer) {
		SPEntityPairingPacket packet = new SPEntityPairingPacket(this.getOriginal().getId(), EntityPairingPacketTypes.SET_BOSS_EVENT_OWNER);
		packet.buffer().writeBoolean(false);
		packet.buffer().writeUUID(this.getBossEvent().getId());
		EpicFightNetworkManager.sendToPlayer(packet, trackingPlayer);
	}
	
	@SuppressWarnings("unchecked")
	default <P extends LivingEntityPatch<?>> P cast() {
		return (P)this;
	}

    @ClientOnly
	default void processOwnerRecordPacket(FriendlyByteBuf buffer) {
		boolean addOperation = buffer.readBoolean();
		UUID eventUUID = buffer.readUUID();
		
		if (addOperation) {
			RenderEngine.getInstance().addBossEventOwner(eventUUID, this);
		} else {
			RenderEngine.getInstance().removeBossEventOwner(eventUUID, this);
		}
	}
}
