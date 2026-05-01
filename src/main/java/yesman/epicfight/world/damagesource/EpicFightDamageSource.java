package yesman.epicfight.world.damagesource;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;

public class EpicFightDamageSource extends DamageSource {
	private final ValueModifier.ResultCalculator modifiedDamageCalculator = ValueModifier.calculator();
	private final ValueModifier.ResultCalculator modifiedArmorNegationCalculator = ValueModifier.calculator();
	private final ValueModifier.ResultCalculator modifiedImpactCalculator = ValueModifier.calculator();
	private final Set<ExtraDamageInstance> extraDamages = new HashSet<> ();
	private final Set<TagKey<DamageType>> runtimeTags = new HashSet<> ();
	
	private StunType stunType = StunType.SHORT;
	private ItemStack usedItem = ItemStack.EMPTY;
	private AnimationAccessor<? extends StaticAnimation> animation;
	private Vec3 initialPosition;
	
	private float baseArmorNegation;
	private float baseImpact;
	private boolean chargeWeapon;
	
	public EpicFightDamageSource(DamageSource damageSource) {
		this(damageSource.typeHolder(), damageSource.getDirectEntity(), damageSource.getEntity(), damageSource.getSourcePosition());
	}
	
	public EpicFightDamageSource(Holder<DamageType> damageType, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 initialPosition) {
		super(damageType, directEntity, causingEntity, initialPosition);
		this.initialPosition = initialPosition;
	}
	
	public EpicFightDamageSource attachDamageModifier(ValueModifier damageModifier) {
		this.modifiedDamageCalculator.attach(damageModifier);
		return this;
	}
	
	public EpicFightDamageSource attachArmorNegationModifier(ValueModifier damageModifier) {
		this.modifiedArmorNegationCalculator.attach(damageModifier);
		return this;
	}
	
	public EpicFightDamageSource attachImpactModifier(ValueModifier damageModifier) {
		this.modifiedImpactCalculator.attach(damageModifier);
		return this;
	}
	
	public EpicFightDamageSource addExtraDamage(ExtraDamageInstance extraDamageInstance) {
		this.extraDamages.add(extraDamageInstance);
		return this;
	}
	
	public EpicFightDamageSource setUsedItem(ItemStack itemstack) {
		this.usedItem = itemstack;
		return this;
	}
	
	public ItemStack getUsedItem() {
		return this.usedItem;
	}
	
	public EpicFightDamageSource setStunType(StunType stunType) {
		this.stunType = stunType;
		return this;
	}
	
	public StunType getStunType() {
		return this.stunType;
	}
	
	public EpicFightDamageSource setBaseArmorNegation(float f) {
		this.baseArmorNegation = f;
		return this;
	}
	
	public float getBaseArmorNegation() {
		return this.baseArmorNegation;
	}
	
	public EpicFightDamageSource setBaseImpact(float f) {
		this.baseImpact = f;
		return this;
	}
	
	public float getBaseImpact() {
		return this.baseImpact;
	}
	
	public EpicFightDamageSource setInitialPosition(Vec3 initialPosition) {
		this.initialPosition = initialPosition;
		return this;
	}
	
	public Vec3 getInitialPosition() {
		return initialPosition;
	}
	
	public void setChargeWeapon(boolean chargeWeapon) {
		this.chargeWeapon = chargeWeapon;
	}
	
	public boolean shouldChargeWeapon() {
		return chargeWeapon;
	}
	
	public EpicFightDamageSource setAnimation(AnimationAccessor<? extends StaticAnimation> animation) {
		this.animation = animation;
		return this;
	}
	
	public AnimationAccessor<? extends StaticAnimation> getAnimation() {
		return this.animation == null ? Animations.EMPTY_ANIMATION : this.animation;
	}
	
	public float calculateDamageAgainst(@Nullable Entity owner, @Nullable LivingEntity target, float baseDamage) {
		MutableFloat totalDamage = new MutableFloat(this.modifiedDamageCalculator.getResult(baseDamage));
		
		if (owner instanceof LivingEntity livingentity && target != null) {
			this.extraDamages.forEach(extraDamageInstance -> {
				totalDamage.add(extraDamageInstance.get(livingentity, this.getUsedItem(), target, baseDamage));
			});
		}
		
		return totalDamage.getValue();
	}
	
	public float calculateArmorNegation() {
		return this.modifiedArmorNegationCalculator.getResult(this.baseArmorNegation);
	}
	
	public float calculateImpact() {
		return this.modifiedImpactCalculator.getResult(this.baseImpact);
	}
	
	@Override
	public boolean is(TagKey<DamageType> type) {
		return this.runtimeTags.contains(type) || super.is(type);
	}

	public EpicFightDamageSource addRuntimeTag(TagKey<DamageType> type) {
		this.runtimeTags.add(type);
		return this;
	}
	
	public EpicFightDamageSource setExecute() {
		this.runtimeTags.add(EpicFightDamageTypeTags.EXECUTION);
		this.runtimeTags.add(DamageTypeTags.BYPASSES_ARMOR);
		
		return this;
	}
}