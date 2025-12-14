package yesman.epicfight.api.client.forgeevent;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.network.server.SPEntityPairingPacket;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

@Cancelable
public class ProcessEntityPairingPacketEvent extends Event {
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
