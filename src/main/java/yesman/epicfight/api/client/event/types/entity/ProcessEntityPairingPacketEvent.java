package yesman.epicfight.api.client.event.types.entity;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class ProcessEntityPairingPacketEvent extends Event implements CancelableEvent {
	private final EntityPatch<?> entitypatch;
	private final SPEntityPairingPacket packet;
	
	public ProcessEntityPairingPacketEvent(EntityPatch<?> entitypatch, SPEntityPairingPacket packet) {
		this.entitypatch = entitypatch;
		this.packet = packet;
	}
	
	public EntityPatch<?> getEntityPatch() {
		return this.entitypatch;
	}
	
	public SPEntityPairingPacket getPacket() {
		return this.packet;
	}
}
