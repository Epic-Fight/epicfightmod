package yesman.epicfight.api.neoevent;

import javax.annotation.Nullable;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

public class EntityStunEvent extends Event implements ICancellableEvent {
	@Nullable
	private final EpicFightDamageSource source;
	private final HurtableEntityPatch<?> stunned;
	private final StunType stunType;
	
	public EntityStunEvent(EpicFightDamageSource source, HurtableEntityPatch<?> stunned, StunType stunType) {
		this.source = source;
		this.stunned = stunned;
		this.stunType = stunType;
	}
	
	public final EpicFightDamageSource getDamageSource() {
		return this.source;
	}
	
	public final HurtableEntityPatch<?> getStunnedEntityPatch() {
		return this.stunned;
	}
	
	public final StunType getStunType() {
		return this.stunType;
	}
}
