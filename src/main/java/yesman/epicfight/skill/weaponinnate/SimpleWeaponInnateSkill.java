package yesman.epicfight.skill.weaponinnate;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class SimpleWeaponInnateSkill extends WeaponInnateSkill {
	public static class Builder extends SkillBuilder<SimpleWeaponInnateSkill> {
		protected AnimationAccessor<? extends AttackAnimation> attackAnimation;
		
		public Builder setAnimations(AnimationAccessor<? extends AttackAnimation> attackAnimation) {
			this.attackAnimation = attackAnimation;
			return this;
		}
	}
	
	public static Builder createSimpleWeaponInnateBuilder() {
		return (new Builder()).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
	}
	
	protected AnimationAccessor<? extends AttackAnimation> attackAnimation;
	
	public SimpleWeaponInnateSkill(Builder builder) {
		super(builder);
		
		this.attackAnimation = builder.attackAnimation;
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.getExecutor().playAnimationSynchronized(this.attackAnimation, 0);
		super.executeOnServer(container, args);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
		
		return list;
	}
	
	@Override
	public WeaponInnateSkill registerPropertiesToAnimation() {
		AttackAnimation anim = this.attackAnimation.get();
		
		for (Phase phase : anim.phases) {
			phase.addProperties(this.properties.get(0).entrySet());
		}
		
		return this;
	}
}