package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class ConditionalWeaponInnateSkill extends WeaponInnateSkill {
	public static final class Builder extends WeaponInnateSkill.Builder<ConditionalWeaponInnateSkill.Builder> {
		protected Function<ServerPlayerPatch, Integer> selector;
		protected AnimationAccessor<? extends AttackAnimation>[] animations;
		
		public Builder(Function<ConditionalWeaponInnateSkill.Builder, ? extends ConditionalWeaponInnateSkill> constructor) {
			super(constructor);
		}
		
		public ConditionalWeaponInnateSkill.Builder setSelector(Function<ServerPlayerPatch, Integer> selector) {
			this.selector = selector;
			return this;
		}
		
		@SafeVarargs
		public final ConditionalWeaponInnateSkill.Builder setAnimations(AnimationAccessor<? extends AttackAnimation>... animations) {
			this.animations = animations;
			return this;
		}
	}
	
	public static ConditionalWeaponInnateSkill.Builder createConditionalWeaponInnateBuilder() {
		return new ConditionalWeaponInnateSkill.Builder(ConditionalWeaponInnateSkill::new).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
	}
	
	public static ConditionalWeaponInnateSkill.Builder createConditionalWeaponInnateBuilder(Function<ConditionalWeaponInnateSkill.Builder, ? extends ConditionalWeaponInnateSkill> constructor) {
		return new ConditionalWeaponInnateSkill.Builder(constructor).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
	}
	
	protected final AnimationAccessor<? extends AttackAnimation>[] attackAnimations;
	protected final Function<ServerPlayerPatch, Integer> selector;
	
	public ConditionalWeaponInnateSkill(ConditionalWeaponInnateSkill.Builder builder) {
		super(builder);
		
		this.attackAnimations = builder.animations;
		this.selector = builder.selector;
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strikes:");
		
		return list;
	}
	
	@Override
	public WeaponInnateSkill registerPropertiesToAnimation() {
		for (AnimationAccessor<? extends AttackAnimation> animationProvider : this.attackAnimations) {
			AttackAnimation anim = animationProvider.get();
			
			for (Phase phase : anim.phases) {
				phase.addProperties(this.properties.get(0).entrySet());
			}
		}
		
		return this;
	}
	
	@Override
	public void executeOnServer(SkillContainer containter, CompoundTag arguments) {
		this.playSkillAnimation(containter.getServerExecutor());
		super.executeOnServer(containter, arguments);
	}
	
	protected int getAnimationInCondition(ServerPlayerPatch executor) {
		return this.selector.apply(executor);
	}
	
	protected void playSkillAnimation(ServerPlayerPatch executor) {
		executor.playAnimationSynchronized(this.attackAnimations[this.getAnimationInCondition(executor)], 0);
	}
}