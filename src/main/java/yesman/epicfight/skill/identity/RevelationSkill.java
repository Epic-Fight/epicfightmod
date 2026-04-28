package yesman.epicfight.skill.identity;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.AttackResult.ResultType;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.BattleModeGui;
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
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class RevelationSkill extends Skill {
	private static final UUID EVENT_UUID = UUID.fromString("31a396ea-0361-11ee-be56-0242ac120002");
	
	public static RevelationSkill.Builder createRevelationSkillBuilder() {
		return (new Builder())
				.addMotion(WeaponCategories.LONGSWORD, (item, player) -> Animations.REVELATION_TWOHAND)
				.addMotion(WeaponCategories.GREATSWORD, (item, player) -> Animations.REVELATION_TWOHAND)
				.addMotion(WeaponCategories.TACHI, (item, player) -> Animations.REVELATION_TWOHAND)
				.setCategory(SkillCategories.IDENTITY)
				.setActivateType(ActivateType.DURATION)
				.setResource(Resource.NONE)
				;
	}
	
	public static class Builder extends SkillBuilder<RevelationSkill> {
		protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions = Maps.newHashMap();
		
		public Builder addMotion(WeaponCategory weaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>> function) {
			this.motions.put(weaponCategory, function);
			return this;
		}
	}
	
	protected final Map<WeaponCategory, BiFunction<CapabilityItem, PlayerPatch<?>, AnimationAccessor<? extends StaticAnimation>>> motions;
	protected final Map<EntityType<?>, Integer> maxRevelationStacks = Maps.newHashMap();
	protected int blockStack;
	protected int parryStack;
	protected int dodgeStack;
	protected int defaultRevelationStacks;
	
	public RevelationSkill(Builder builder) {
		super(builder);
		
		this.motions = builder.motions;
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		
		this.maxRevelationStacks.clear();
		this.blockStack = parameters.getInt("block_stacks");
		this.parryStack = parameters.getInt("parry_stacks");
		this.dodgeStack = parameters.getInt("dodge_stacks");
		this.defaultRevelationStacks = parameters.getInt("default_revelation_stacks");
		
		CompoundTag maxStacks = parameters.getCompound("max_revelations");
		
		for (String registryName : maxStacks.getAllKeys()) {
			EntityType<?> entityType = EntityType.byString(registryName).orElse(null);
			
			if (entityType != null) {
				this.maxRevelationStacks.put(entityType, maxStacks.getInt(registryName));
			} else {
				EpicFightMod.LOGGER.warn("Revelation registry error: no entity type named : " + registryName);
				
			}
		}
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.SKILL_CAST_EVENT, EVENT_UUID, (event) -> {
			if (container.getExecutor().isLogicalClient()) {
				Skill skill = event.getSkillContainer().getSkill();
				
				if (skill.getCategory() != SkillCategories.WEAPON_INNATE) {
					return;
				}
				
				if (container.getExecutor().getTarget() != null) {
					EpicFightCapabilities.getUnparameterizedEntityPatch(container.getExecutor().getTarget(), LivingEntityPatch.class).ifPresent(entitypatch -> {
						if (this.isActivated(container)) {
							if (container.sendCastRequest((LocalPlayerPatch)container.getExecutor(), ClientEngine.getInstance().controlEngine).isExecutable()) {
								container.setDuration(0);
								event.setCanceled(true);
							}
						}
					});
				}
			}
		});
		
		listener.addEventListener(EventType.SET_TARGET_EVENT, EVENT_UUID, (event) -> {
			container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 0);
		});
		
		listener.addEventListener(EventType.DODGE_SUCCESS_EVENT, EVENT_UUID, (event) -> {
			LivingEntity target = container.getExecutor().getTarget();
			
			if (target != null && target.is(event.getDamageSource().getDirectEntity())) {
				this.checkStackAndActivate(container, event.getPlayerPatch(), target, container.getDataManager().getDataValue(SkillDataKeys.STACKS.get()), this.dodgeStack);
			}
			
		}, -1);
		
		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
			if (event.getResult() == ResultType.BLOCKED) {
				LivingEntity target = container.getExecutor().getTarget();
				
				if (target != null && target.is(event.getDamageSource().getDirectEntity())) {
					int stacks = event.isParried() ? this.parryStack : this.blockStack;
					
					this.checkStackAndActivate(container, event.getPlayerPatch(), target, container.getDataManager().getDataValue(SkillDataKeys.STACKS.get()), stacks);
				}
			}
		}, -1);
		
		listener.addEventListener(EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, EVENT_UUID, (event) -> {
			if (this.isActivated(container)) {
				event.setCanceled(false);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		container.getExecutor().getEventListener().removeListener(EventType.SKILL_CAST_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.SET_TARGET_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.TARGET_INDICATOR_ALERT_CHECK_EVENT, EVENT_UUID);
	}
	
	@Override
	public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
		super.executeOnServer(container, args);
		
		CapabilityItem holdingItem = container.getExecutor().getHoldingItemCapability(InteractionHand.MAIN_HAND);
		AnimationAccessor<? extends StaticAnimation> animation = this.motions.containsKey(holdingItem.getWeaponCategory()) ? this.motions.get(holdingItem.getWeaponCategory()).apply(holdingItem, container.getExecutor()) : Animations.REVELATION_ONEHAND;
		container.getExecutor().playAnimationSynchronized(animation, 0.0F);
	}
	
	public void checkStackAndActivate(SkillContainer container, ServerPlayerPatch playerpatch, LivingEntity target, int stacks, int addStacks) {
		int maxStackSize = this.maxRevelationStacks.getOrDefault(target.getType(), this.defaultRevelationStacks);
		int stacksToAdd = stacks + addStacks;
		
		if (stacksToAdd < maxStackSize) {
			container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), stacksToAdd);
		} else {
			if (!this.isActivated(container)) {
				this.setDurationSynchronize(container, this.maxDuration);
			}
			
			container.getDataManager().setDataSync(SkillDataKeys.STACKS.get(), 0);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldDraw(SkillContainer container) {
		return container.getExecutor().getTarget() != null;
	}


	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphics guiGraphics, float x, float y, float partialTick) {
		PoseStack poseStack = guiGraphics.pose();
		poseStack.pushPose();
		poseStack.translate(0, (float)gui.getSlidingProgression(), 0);
		guiGraphics.blit(this.getSkillTexture(), (int)x, (int)y, 24, 24, 0, 0, 1, 1, 1, 1);
		int stacks = container.getRemainDuration() > 0 ? 0 : this.maxRevelationStacks.getOrDefault(container.getExecutor().getTarget().getType(), this.defaultRevelationStacks)
																- container.getDataManager().getDataValue(SkillDataKeys.STACKS.get());
		guiGraphics.drawString(gui.getFont(), String.format("%d", stacks), x + 18, y + 14, 16777215, true);
		poseStack.popPose();
	}
}