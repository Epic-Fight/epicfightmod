package yesman.epicfight.world.capabilities.projectile;

import java.util.Map;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.RangedWeaponCapability;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public abstract class ProjectilePatch<T extends Projectile> extends EntityPatch<T> {
	protected float impact;
	protected float armorNegation;
	protected Vec3 initialFirePosition;
	protected boolean hasHit;
	
	@Override
	public void onJoinWorld(T projectileEntity, EntityJoinLevelEvent event) {
		Entity shooter = projectileEntity.getOwner();
		boolean flag = true;
		
		if (shooter != null && shooter instanceof LivingEntity livingshooter) {
			this.initialFirePosition = shooter.position();
			ItemStack heldItem = livingshooter.getMainHandItem();
			CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(heldItem);
			
			if (itemCap instanceof RangedWeaponCapability) {
				Map<Attribute, AttributeModifier> modifierMap = itemCap.getDamageAttributesInCondition(Styles.RANGED);
				
				if (modifierMap != null) {
					this.armorNegation = 
						modifierMap.containsKey(EpicFightAttributes.ARMOR_NEGATION.get()) ?
							(float)modifierMap.get(EpicFightAttributes.ARMOR_NEGATION.get()).getAmount()
								: (float)EpicFightAttributes.ARMOR_NEGATION.get().getDefaultValue();
					
					this.impact = 
						modifierMap.containsKey(EpicFightAttributes.IMPACT.get()) ?
							(float)modifierMap.get(EpicFightAttributes.IMPACT.get()).getAmount()
								: (float)EpicFightAttributes.IMPACT.get().getDefaultValue();
					
					if (modifierMap.containsKey(EpicFightAttributes.MAX_STRIKES.get())) {
						this.setMaxStrikes(projectileEntity, (int)modifierMap.get(EpicFightAttributes.MAX_STRIKES.get()).getAmount());
					}
				}
				
				flag = false;
			}
		}
		
		if (flag) {
			this.armorNegation = 0.0F;
			this.impact = 0.0F;
		}
	}
	
	@Override
	public void onAddedToWorld() {
		if (this.getOriginal().level().isClientSide()) {
			double entityId = Double.longBitsToDouble((long)this.getOriginal().getId());
			this.getOriginal().level().addParticle(EpicFightParticles.PROJECTILE_TRAIL.get(), entityId, 0, 0, 0, 0, 0);
		}
	}
	
	/**
	 * @return true if event should be canceled
	 */
	public boolean onProjectileImpact(ProjectileImpactEvent event) {
		return false;
	}
	
	protected abstract void setMaxStrikes(T projectileEntity, int maxStrikes);
	public abstract EpicFightDamageSource createEpicFightDamageSource();
	
	@Override
	public boolean overrideRender() {
		return false;
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		return super.getMatrix(partialTicks);
	}
	
	public void setHit(boolean hit) {
		this.hasHit = hit;
	}
	
	public boolean hit() {
		return this.hasHit;
	}
}
