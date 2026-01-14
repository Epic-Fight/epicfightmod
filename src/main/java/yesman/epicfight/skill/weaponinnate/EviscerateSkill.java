package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class EviscerateSkill extends WeaponInnateSkill {
	private static final UUID EVENT_UUID = UUID.fromString("f082557a-b2f9-11eb-8529-0242ac130003");
	private AssetAccessor<? extends AttackAnimation> first;
	private AssetAccessor<? extends AttackAnimation> second;
	
	private float damageCap;
	
	public EviscerateSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
		super(builder);
		
		this.first = Animations.EVISCERATE_FIRST;
		this.second = Animations.EVISCERATE_SECOND;
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		
		this.damageCap = parameters.getFloat("damage_cap");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		container.getExecutor().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
			if (Animations.EVISCERATE_FIRST.equals(event.getAnimation())) {
				List<LivingEntity> hurtEntities = event.getPlayerPatch().getCurrentlyActuallyHitEntities();
				
				if (!hurtEntities.isEmpty() && hurtEntities.get(0).isAlive()) {
					event.getPlayerPatch().reserveAnimation(this.second);
					event.getPlayerPatch().getServerAnimator().getPlayerFor(null).reset();
					event.getPlayerPatch().getCurrentlyActuallyHitEntities().clear();
				}
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecutor().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.getExecutor().playAnimationSynchronized(this.first, 0.0F);
		
		super.executeOnServer(container, args);
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
	
	/// TODO: bad implementation but to avoid breaking changes
	/// make [ExtraDamageInstance] configurable via skill parameters
	public float getDamageCap() {
		return this.damageCap;
	}
}