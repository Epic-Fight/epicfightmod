package yesman.epicfight.skill.weaponinnate;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.neoevent.playerpatch.AttackEndEvent;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

public class EviscerateSkill extends WeaponInnateSkill {
	private AssetAccessor<? extends AttackAnimation> first;
	private AssetAccessor<? extends AttackAnimation> second;
	
	public EviscerateSkill(WeaponInnateSkill.Builder<?> builder) {
		super(builder);
		
		this.first = Animations.EVISCERATE_FIRST;
		this.second = Animations.EVISCERATE_SECOND;
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void attackAnimationEndEvent(AttackEndEvent event, SkillContainer skillContainer) {
		if (Animations.EVISCERATE_FIRST.equals(event.getAnimation())) {
			List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
			
			if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
				event.getPlayerPatch().reserveAnimation(this.second);
				event.getPlayerPatch().getServerAnimator().getPlayerFor(null).reset();
				event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
			}
		}
	}
	
	@Override
	public void executeOnServer(SkillContainer container, CompoundTag arguments) {
		container.getExecutor().playAnimationSynchronized(this.first, 0.0F);
		super.executeOnServer(container, arguments);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "First Strike:");
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Second Strike:");
		return list;
	}
	
	@Override
	public WeaponInnateSkill registerPropertiesToAnimation() {
		this.first.get().phases[0].addProperties(this.properties.get(0).entrySet());
		this.second.get().phases[0].addProperties(this.properties.get(1).entrySet());
		
		return this;
	}
}