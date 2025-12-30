package yesman.epicfight.api.event.types.entity;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.StunType;

import javax.annotation.Nullable;

public class StunnedEvent extends LivingEntityPatchEvent implements CancelableEvent {
	@Nullable
	private final EpicFightDamageSource source;
	private final StunType stunType;
	
	public StunnedEvent(@Nullable EpicFightDamageSource source, LivingEntityPatch<?> entityPatch, StunType stunType) {
        super(entityPatch);

		this.source = source;
		this.stunType = stunType;
	}

    @Nullable
	public final EpicFightDamageSource getDamageSource() {
		return this.source;
	}
	
	public final StunType getStunType() {
		return this.stunType;
	}
}
