package yesman.epicfight.api.event.types.entity;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

/// Called when an entity delivers damage to others
public abstract class DealDamageEvent extends LivingEntityPatchEvent {
	protected final LivingEntity target;
    protected final float originalDamage;
	private final EpicFightDamageSource damageSource;
	
	public DealDamageEvent(LivingEntityPatch<?> entityPatch, LivingEntity target, EpicFightDamageSource source, float damage) {
		super(entityPatch);

		this.target = target;
		this.damageSource = source;
		this.originalDamage = damage;
	}

	public LivingEntity getTarget() {
		return this.target;
	}
	
	/// Modifying the original event's damage amount will have no effect on final damage calculation. Instead, use ValueModifier in EpicFightDamageSource
	public EpicFightDamageSource getDamageSource() {
		return this.damageSource;
	}

    public float getOriginalDamage() {
        return this.originalDamage;
    }

	public static final class Income extends DealDamageEvent implements CancelableEvent {
		public Income(LivingEntityPatch<?> entityPatch, LivingEntity target, EpicFightDamageSource source, float damageAmount) {
			super(entityPatch, target, source, damageAmount);
		}
	}

    private static abstract class Modifiable extends DealDamageEvent {
        protected float modifiedDamage;

        public Modifiable(LivingEntityPatch<?> entityPatch, LivingEntity target, EpicFightDamageSource source, float damage) {
            super(entityPatch, target, source, damage);
            this.modifiedDamage = damage;
        }

        public void setModifiedDamage(float amount) {
            this.modifiedDamage = amount;
        }

        public float getModifiedDamage() {
            return this.modifiedDamage;
        }
    }

	public static final class Pre extends DealDamageEvent.Modifiable {
		public Pre(LivingEntityPatch<?> entityPatch, LivingEntity target, EpicFightDamageSource source, float damageAmount) {
			super(entityPatch, target, source, damageAmount);
		}
	}
	
	public static final class Post extends DealDamageEvent.Modifiable {
		public Post(LivingEntityPatch<?> entityPatch, LivingEntity target, EpicFightDamageSource source, float damageAmount) {
			super(entityPatch, target, source, damageAmount);
		}
	}
}
