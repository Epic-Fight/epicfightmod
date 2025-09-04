package yesman.epicfight.api.forgeevent;

import javax.annotation.Nullable;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

@Cancelable
public class EntityStunEvent extends Event {
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
