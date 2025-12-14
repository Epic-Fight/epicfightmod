package yesman.epicfight.skill.weaponinnate;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class GraspingSpireSkill extends WeaponInnateSkill {
	private static final UUID EVENT_UUID = UUID.fromString("3fa26bbc-d14e-11ed-afa1-0242ac120002");
	
	private AnimationAccessor<? extends AttackAnimation> first;
	private AnimationAccessor<? extends AttackAnimation> second;
	
	public GraspingSpireSkill(SkillBuilder<? extends WeaponInnateSkill> builder) {
		super(builder);
		this.first = Animations.GRASPING_SPIRAL_FIRST;
		this.second = Animations.GRASPING_SPIRAL_SECOND;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID, (event) -> {
			if (this.first.equals(event.getAnimation())) {
				container.getDataManager().setDataSync(SkillDataKeys.LAST_HIT_COUNT.get(), event.getPlayerPatch().getCurrentlyActuallyHitEntities().size());
			}
		});
		
		container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			if (this.second.equals(event.getDamageSource().getAnimation())) {
				event.getDamageSource().attachImpactModifier(ValueModifier.adder(container.getDataManager().getDataValue(SkillDataKeys.LAST_HIT_COUNT.get()) * 0.4F));
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecutor().getEventListener().removeListener(EventType.ATTACK_ANIMATION_END_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_HURT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		container.getExecutor().playAnimationSynchronized(this.first, 0.0F);
		super.executeOnServer(container, args);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Pierce:");
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