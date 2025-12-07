package yesman.epicfight.api.client.neoevent;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public class ProcessEntityPairingPacketEvent extends Event implements ICancellableEvent {
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
