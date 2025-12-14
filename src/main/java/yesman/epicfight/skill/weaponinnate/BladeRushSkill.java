package yesman.epicfight.skill.weaponinnate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.SynchedAnimationVariableKeys;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.effect.EpicFightMobEffects;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class BladeRushSkill extends WeaponInnateSkill {
	public static Builder createBladeRushBuilder() {
		Builder builder = new Builder().setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
		builder.putTryAnimation(EntityType.ZOMBIE, Animations.BLADE_RUSH_TRY)
				.putTryAnimation(EntityType.HUSK, Animations.BLADE_RUSH_TRY)
				.putTryAnimation(EntityType.DROWNED, Animations.BLADE_RUSH_TRY)
				.putTryAnimation(EntityType.SKELETON, Animations.BLADE_RUSH_TRY)
				.putTryAnimation(EntityType.STRAY, Animations.BLADE_RUSH_TRY)
				.putTryAnimation(EntityType.CREEPER, Animations.BLADE_RUSH_TRY);
		
		return builder;
	}
	
	private static final UUID EVENT_UUID = UUID.fromString("444a1a6a-c2f1-11eb-8529-0242ac130003");
	
	public static class Builder extends SkillBuilder<BladeRushSkill> {
		private final Map<EntityType<?>, AnimationAccessor<? extends StaticAnimation>> tryAnimations = Maps.newHashMap();
		
		public Builder putTryAnimation(EntityType<?> entityType, AnimationAccessor<? extends StaticAnimation> tryAnimation) {
			this.tryAnimations.put(entityType, tryAnimation);
			return this;
		}
	}
	
	private final List<AnimationAccessor<? extends StaticAnimation>> comboAnimations = new ArrayList<> (3);
	private final Map<EntityType<?>, AnimationAccessor<? extends StaticAnimation>> tryAnimations;
	
	public BladeRushSkill(Builder builder) {
		super(builder);
		
		this.comboAnimations.add(Animations.BLADE_RUSH_COMBO1);
		this.comboAnimations.add(Animations.BLADE_RUSH_COMBO2);
		this.comboAnimations.add(Animations.BLADE_RUSH_COMBO3);
		this.tryAnimations = builder.tryAnimations;
	}
	
	@Override
	public FriendlyByteBuf gatherArguments(SkillContainer container, ControlEngine controlEngine) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(true);
		
		return buf;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID, (event) -> {
			if (event.getDamageSource().getAnimation().idBetween(Animations.BLADE_RUSH_COMBO1, Animations.BLADE_RUSH_COMBO3) && this.tryAnimations.containsKey(event.getTarget().getType())) {
				MobEffectInstance effectInstance = event.getTarget().getEffect(EpicFightMobEffects.INSTABILITY.get());
				int amp = effectInstance == null ? 0 : effectInstance.getAmplifier() + 1;
				event.getTarget().addEffect(new MobEffectInstance(EpicFightMobEffects.INSTABILITY.get(), 100, amp));
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		container.getExecutor().getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		LivingEntity target = container.getExecutor().getTarget();
		boolean instaKill = false;
		
		if (target != null) {
			if (target.hasEffect(EpicFightMobEffects.INSTABILITY.get()) && target.getEffect(EpicFightMobEffects.INSTABILITY.get()).getAmplifier() >= 2) {
				instaKill = true;
			} else {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(target, LivingEntityPatch.class);
				
				if (entitypatch != null && entitypatch.getEntityState().hurtLevel() > 1 && this.tryAnimations.containsKey(target.getType())) {
					instaKill = true;
				}
			}
		} else {
			return;
		}
		
		if (instaKill) {
			container.getDataManager().setData(SkillDataKeys.COMBO_COUNTER.get(), 0);
			container.getExecutor().getAnimator().getVariables().put(SynchedAnimationVariableKeys.TARGET_ENTITY.get(), Animations.BLADE_RUSH_TRY, target.getId());
			container.getExecutor().playAnimationSynchronized(Animations.BLADE_RUSH_TRY, 0);
		} else {
			int counter = container.getDataManager().getDataValue(SkillDataKeys.COMBO_COUNTER.get());
			AnimationAccessor<? extends StaticAnimation> animation = this.comboAnimations.get(counter);
			container.getDataManager().setDataF(SkillDataKeys.COMBO_COUNTER.get(), (v) -> (v + 1) % this.comboAnimations.size());
			container.getExecutor().getAnimator().getVariables().put(SynchedAnimationVariableKeys.TARGET_ENTITY.get(), animation, target.getId());
			container.getExecutor().playAnimationSynchronized(animation, 0);
		}
		
		super.executeOnServer(container, args);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemStack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = super.getTooltipOnItem(itemStack, cap, playerCap);
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(0), "Each Strike:");
		this.generateTooltipforPhase(list, itemStack, cap, playerCap, this.properties.get(1), "Execution:");
		return list;
	}
	
	@Override
	public WeaponInnateSkill registerPropertiesToAnimation() {
		Animations.BLADE_RUSH_COMBO1.get().phases[0].addProperties(this.properties.get(0).entrySet());
		Animations.BLADE_RUSH_COMBO2.get().phases[0].addProperties(this.properties.get(0).entrySet());
		Animations.BLADE_RUSH_COMBO3.get().phases[0].addProperties(this.properties.get(0).entrySet());
		Animations.BLADE_RUSH_EXECUTE_BIPED.get().phases[0].addProperties(this.properties.get(1).entrySet());
		
		return this;
	}
	
	@Override
	public boolean checkExecuteCondition(SkillContainer container) {
		return container.getExecutor().getTarget() != null && container.getExecutor().getTarget().isAlive() && container.getExecutor().getOriginal().distanceToSqr(container.getExecutor().getTarget()) < 100.0D;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onScreen(LocalPlayerPatch playerpatch, float resolutionX, float resolutionY) {
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void validationFeedback(SkillContainer container) {
		Skill skill = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND).getInnateSkill(container.getExecutor(), container.getExecutor().getOriginal().getItemInHand(InteractionHand.MAIN_HAND));
		
		if (this.equals(skill) && !this.checkExecuteCondition(container)) {
			if (container.getExecutor().getTarget() == null || !container.getExecutor().getTarget().isAlive()) {
				Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(EpicFightMod.format("gui.%s.warn.no_target")), false);
			} else {
				Minecraft.getInstance().gui.setOverlayMessage(Component.translatable(EpicFightMod.format("gui.%s.warn.target_too_far")), false);
			}
		}
	}
}