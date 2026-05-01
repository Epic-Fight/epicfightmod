package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;

import java.util.List;

public abstract class HurtableEntityPatch<T extends LivingEntity> extends EntityPatch<T> {
	private boolean stunReductionDecreases;
	protected float stunTimeReductionDefault;
	protected float stunTimeReduction;
	protected boolean cancelKnockback;
	
	public HurtableEntityPatch(T original) {
		super(original);
	}
	
	protected void updateStunTime() {
		this.cancelKnockback = false;
		
		if (this.stunReductionDecreases) {
			float stunArmor = this.getStunArmor();
			
			this.stunTimeReduction -= 0.1F * (1.1F - this.stunTimeReduction * this.stunTimeReduction) * (1.0F - stunArmor / (7.5F + stunArmor));
			
			if (this.stunTimeReduction < 0.0F) {
				this.stunReductionDecreases = false;
				this.stunTimeReduction = 0.0F;
			}
		} else {
			if (this.stunTimeReduction < this.stunTimeReductionDefault) {
				this.stunTimeReduction += 0.02F * (1.1F - this.stunTimeReduction * this.stunTimeReduction);
				
				if (this.stunTimeReduction > this.stunTimeReductionDefault) {
					this.stunTimeReduction = this.stunTimeReductionDefault;
				}
			}
		}
	}
	
	@Override
	public void preTick() {
		if (!this.original.level().isClientSide()) {
			this.updateStunTime();
		}
	}
	
	public abstract boolean applyStun(StunType stunType, float stunTime);
	
	public float getWeight() {
		return (float)this.original.getAttributeValue(Attributes.MAX_HEALTH) * 2.0F;
	}
	
	public float getStunShield() {
		return 0.0F;
	}
	
	public void setStunShield(float value) {
	}
	
	/**
	 * Define custom actions when stun shield gets damage
	 */
	public void damageStunShield(float damage, float impact) {
		this.setStunShield(this.getStunShield() - impact);
	}
	
	public void setStunReductionOnHit(StunType stunType) {
		this.stunReductionDecreases = true;
		
		if (stunType != StunType.NONE) {
			this.stunTimeReduction += Math.max((1.0F - this.stunTimeReduction) * 0.8F, 0.5F);
			this.stunTimeReduction = Math.min(1.0F, this.stunTimeReduction);
			this.stunReductionDecreases = true;
		}
	}
	
	public float getStunReduction() {
		return this.stunTimeReduction;
	}
	
	public void setDefaultStunReduction(EquipmentSlot equipmentslot, ItemStack from, ItemStack to) {
		List<AttributeModifier> modifiersToAdd = CapabilityItem.getAttributeModifiersAsWeapon(EpicFightAttributes.STUN_ARMOR, equipmentslot, to, null);
		List<AttributeModifier> modifiersToRemove = CapabilityItem.getAttributeModifiersAsWeapon(EpicFightAttributes.STUN_ARMOR, equipmentslot, from, null);
		
		AttributeInstance tempAttr = new AttributeInstance(EpicFightAttributes.STUN_ARMOR, (i)->{});
		tempAttr.replaceFrom(this.original.getAttribute(EpicFightAttributes.STUN_ARMOR));
		
		for (AttributeModifier modifier : modifiersToAdd) {
			if (!tempAttr.hasModifier(modifier.id())) {
				tempAttr.addTransientModifier(modifier);
			}
		}
		
		for (AttributeModifier modifier : modifiersToRemove) {
			if (tempAttr.hasModifier(modifier.id())) {
				tempAttr.removeModifier(modifier);
			}
		}
		
		float stunArmor = (float)tempAttr.getValue();
		this.stunReductionDecreases = stunArmor < this.getStunArmor();
		this.stunTimeReductionDefault = stunArmor / (stunArmor + 7.5F);
	}
	
	public float getStunArmor() {
		AttributeInstance stunArmor = this.original.getAttribute(EpicFightAttributes.STUN_ARMOR);
		return (float)(stunArmor == null ? 0.0F : stunArmor.getValue());
	}
	
	public EntityState getEntityState() {
		return EntityState.DEFAULT_STATE;
	}

	public boolean shouldCancelKnockback() {
		return this.cancelKnockback;
	}
	
	public abstract boolean isStunned();
	
	public void knockBackEntity(Vec3 sourceLocation, float power) {
		double d1 = sourceLocation.x() - this.original.getX();
        double d0;
        
		for (d0 = sourceLocation.z() - this.original.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
            d1 = (Math.random() - Math.random()) * 0.01D;
        }
		
		power *= 1.0D - this.original.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
		
		if (power > 0.0D) {
			this.original.hurtMarked = true;
			this.original.hasImpulse = true;
			Vec3 vec3 = this.original.getDeltaMovement();
			Vec3 vec31 = (new Vec3(d1, 0.0D, d0)).normalize().scale(power);
			this.original.setDeltaMovement(vec3.x / 2.0D - vec31.x, this.original.onGround() ? Math.min(0.4D, vec3.y / 2.0D) : vec3.y, vec3.z / 2.0D - vec31.z);
		}
	}
	
	public void playSound(Holder<SoundEvent> sound, float pitchModifierMin, float pitchModifierMax) {
		this.playSound(sound.value(), 1.0F, pitchModifierMin, pitchModifierMax);
	}
	
	public void playSound(SoundEvent sound, float pitchModifierMin, float pitchModifierMax) {
		this.playSound(sound, 1.0F, pitchModifierMin, pitchModifierMax);
	}
	
	public void playSound(Holder<SoundEvent> sound, float volume, float pitchModifierMin, float pitchModifierMax) {
		this.playSound(sound.value(), volume, pitchModifierMin, pitchModifierMax);
	}
	
	public void playSound(SoundEvent sound, float volume, float pitchModifierMin, float pitchModifierMax) {
		if (sound == null) {
			return;
		}
		
		float pitch = (this.original.getRandom().nextFloat() * 2.0F - 1.0F) * (pitchModifierMax - pitchModifierMin);

		if (!this.isLogicalClient()) {
			this.original.level().playSound(null, this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch);
		} else {
			this.original.level().playLocalSound(this.original.getX(), this.original.getY(), this.original.getZ(), sound, this.original.getSoundSource(), volume, 1.0F + pitch, false);
		}
	}

	@Override
	public boolean overrideRender() {
		return false;
	}
}