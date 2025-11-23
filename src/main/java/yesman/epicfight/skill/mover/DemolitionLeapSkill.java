package yesman.epicfight.skill.mover;

import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.neoevent.MappedMovementInputUpdateEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.*;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class DemolitionLeapSkill extends Skill implements ChargeableSkill {
	private AnimationAccessor<? extends StaticAnimation> chargingAnimation;
	private AnimationAccessor<? extends StaticAnimation> shootAnimation;
	
	public DemolitionLeapSkill(SkillBuilder<?> builder) {
		super(builder);
		
		this.chargingAnimation = Animations.BIPED_DEMOLITION_LEAP_CHARGING;
		this.shootAnimation = Animations.BIPED_DEMOLITION_LEAP;
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.CLIENT)
	public void movementInputEvent(MappedMovementInputUpdateEvent event, SkillContainer skillContainer) {
		if (skillContainer.getExecutor().isHoldingSkill(this)) {
			InputManager.setInputState(event.getInputState().withJumping(false));
		}
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER, priority = 1)
	public void takeDamagePost(TakeDamageEvent.Pre event, SkillContainer container) {
		if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && container.getDataManager().getDataValue(EpicFightSkillDataKeys.PROTECT_NEXT_FALL)) {
			event.attachValueModifier(ValueModifier.multiplier(0.5F));
			container.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
		}
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.SERVER)
	public void livingFall(LivingFallEvent event, SkillContainer skillContainer) {
		if (LevelUtil.calculateLivingEntityFallDamage(event.getEntity(), event.getDamageMultiplier(), event.getDistance()) == 0) {
			skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
		}
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		return super.isExecutableState(executor) && executor.getOriginal().onGround();
	}
	
	@Override
	public void cancelOnClient(SkillContainer container, CompoundTag arguments) {
		super.cancelOnClient(container, arguments);
		container.getExecutor().resetHolding();
		container.getExecutor().playAnimationSynchronized(Animations.BIPED_IDLE, 0.0F);
	}
	
	@Override
	public void executeOnClient(SkillContainer container, CompoundTag arguments) {
		int ticks = arguments.getInt("rawChargingTicks");
		int modifiedTicks = (int)(7.4668F * Math.log10(ticks + 1.0F) / Math.log10(2));
		Vec3f jumpDirection = new Vec3f(0, modifiedTicks * 0.05F, 0);
        EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
        float xRot = Mth.clamp(70.0F + Mth.clamp(cameraApi.getForwardXRot(), -90.0F, 0.0F), 0.0F, 70.0F);
		jumpDirection.add(0.0F, (xRot / 70.0F) * 0.05F, 0.0F);
		jumpDirection.rotate(xRot, Vec3f.X_AXIS);
		jumpDirection.rotate(-cameraApi.getForwardYRot(), Vec3f.Y_AXIS);
		container.getExecutor().getOriginal().setDeltaMovement(jumpDirection.toDoubleVector());
		container.getExecutor().resetHolding();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {
		// Set player charging skill cause it won't be fired on feedback packet cause it jumped
		controlEngine.setHoldingKey(SkillSlots.MOVER, this.getKeyMapping());
		container.getExecutor().startSkillHolding(this);
	}
	
	@Override
	public void startHolding(SkillContainer caster) {
		if (!caster.getExecutor().isLogicalClient()) {
			caster.getExecutor().playAnimationSynchronized(this.chargingAnimation, 0.0F);
		}
	}
	
	@Override
	public void onStopHolding(SkillContainer container, SPSkillFeedback feedback) {
		if (container.getExecutor().getSkillChargingTicks(1.0F) > this.getAllowedMaxChargingTicks()) {
			feedback.setFeedbackType(SPSkillFeedback.FeedbackType.EXPIRED);
		} else {
			container.getExecutor().playSound(EpicFightSounds.ROCKET_JUMP.get(), 1.0F, 0.0F, 0.0F);
			container.getExecutor().playSound(EpicFightSounds.ENTITY_MOVE.get(), 1.0F, 0.0F, 0.0F);

			int accumulatedTicks = container.getExecutor().getChargingTicks();

			LevelUtil.circleSlamFracture(null, container.getExecutor().getOriginal().level(), container.getExecutor().getOriginal().position().subtract(0, 1, 0), accumulatedTicks * 0.05D, true, false, false);
			Vec3 entityEyepos = container.getExecutor().getOriginal().getEyePosition();
			EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(container.getServerExecutor().getOriginal().serverLevel(), entityEyepos.x, entityEyepos.y, entityEyepos.z, 0.0D, 0.0D, 2 + 0.05D * container.getExecutor().getAccumulatedChargeTicks());

			container.getExecutor().playAnimationSynchronized(this.shootAnimation, 0.0F);
			feedback.arguments().putInt("rawChargingTicks", accumulatedTicks);
			container.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, true);
		}
	}

	@Override
	public int getAllowedMaxChargingTicks() {
		return 80;
	}
	
	@Override
	public int getMaxChargingTicks() {
		return 40;
	}
	
	@Override
	public int getMinChargingTicks() {
		return 12;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public KeyMapping getKeyMapping() {
		return EpicFightKeyMappings.MOVER_SKILL;
	}

	@Override
	public void holdTick(SkillContainer container) {
		int chargingTicks = container.getExecutor().getSkillChargingTicks();
		
		if (chargingTicks % 5 == 0 && container.getExecutor().getAccumulatedChargeTicks() < this.getMaxChargingTicks()) {
			if (container.getExecutor().consumeForSkill(this, Skill.Resource.STAMINA, this.consumption)) {
				container.getExecutor().setChargingTicks(container.getExecutor().getChargingTicks() + 5);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("attribute.name.epicfight.stamina_per_second.consume", ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.consumption), "0.25"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}