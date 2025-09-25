package yesman.epicfight.world.entity;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class DodgeLocationIndicator extends LivingEntity {
	private static final List<ItemStack> EMPTY_LIST = Collections.emptyList();
	private LivingEntityPatch<?> entitypatch;
	
	public DodgeLocationIndicator(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}
	
	public DodgeLocationIndicator(LivingEntityPatch<?> entitypatch) {
		this(EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get(), entitypatch.getOriginal().level());
		
		this.entitypatch = entitypatch;
		
		this.setPos(entitypatch.getOriginal().position());
		this.setBoundingBox(entitypatch.getOriginal().getBoundingBox().expandTowards(1.0D, 0.0D, 1.0D));
		
		if (this.level().isClientSide()) {
			this.discard();
		}
	}
	
	@Override
	public void tick() {
		if (this.tickCount > 5) {
			this.discard();
		}
	}
	
	@Override
	public boolean hurt(DamageSource damageSource, float amount) {
		if (this.level().isClientSide()) {
			return false;
		}
		
		if (!DodgeAnimation.DODGEABLE_SOURCE_VALIDATOR.apply(damageSource).dealtDamage()) {
			this.entitypatch.onDodgeSuccess(damageSource, this.getBoundingBox().getCenter());
		}
		
		this.discard();
		
		return false;
	}
	
	@Override
	public Iterable<ItemStack> getArmorSlots() {
		return EMPTY_LIST;
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {
		
	}

	@Override
	public HumanoidArm getMainArm() {
		return HumanoidArm.RIGHT;
	}
}