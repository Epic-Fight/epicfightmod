package yesman.epicfight.skill.mover;

import java.util.UUID;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.utils.LevelUtil;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class DemolitionLeapSkill extends Skill implements ChargeableSkill {
	private static final UUID EVENT_UUID = UUID.fromString("3d142bf4-0dcd-11ee-be56-0242ac120002");
	private AnimationAccessor<? extends StaticAnimation> chargingAnimation;
	private AnimationAccessor<? extends StaticAnimation> shootAnimation;
	
	public DemolitionLeapSkill(SkillBuilder<? extends Skill> builder) {
		super(builder);
		
		this.chargingAnimation = Animations.BIPED_DEMOLITION_LEAP_CHARGING;
		this.shootAnimation = Animations.BIPED_DEMOLITION_LEAP;
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		PlayerEventListener listener = container.getExecutor().getEventListener();
		
		listener.addEventListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID, (event) -> {
			if (event.getPlayerPatch().isHoldingSkill(this)) {
                InputManager.setInputState(event.getInputState().withJumping(false));
			}
		});

		listener.addEventListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, (event) -> {
			if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && container.getDataManager().getDataValue(SkillDataKeys.PROTECT_NEXT_FALL.get())) {
				event.attachValueModifier(ValueModifier.multiplier(0.5F));
				container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
			}
		}, 1);
		
		listener.addEventListener(EventType.FALL_EVENT, EVENT_UUID, (event) -> {
			if (LevelUtil.calculateLivingEntityFallDamage(event.getForgeEvent().getEntity(), event.getForgeEvent().getDamageMultiplier(), event.getForgeEvent().getDistance()) == 0) {
				container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), false);
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.MOVEMENT_INPUT_EVENT, EVENT_UUID);
		container.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_HURT, EVENT_UUID, 1);
		container.getExecutor().getEventListener().removeListener(EventType.FALL_EVENT, EVENT_UUID);
	}
	
	@Override
	public boolean isExecutableState(PlayerPatch<?> executor) {
		return super.isExecutableState(executor) && executor.getOriginal().onGround();
	}
	
	@Override
	public void cancelOnClient(SkillContainer container, FriendlyByteBuf args) {
		super.cancelOnClient(container, args);
		container.getExecutor().resetHolding();
		container.getExecutor().playAnimationSynchronized(Animations.BIPED_IDLE, 0.0F);
	}
	
	@Override
	public void executeOnClient(SkillContainer container, FriendlyByteBuf args) {
		args.readInt(); // discard raw charging ticks
		int ticks = args.readInt();
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
	
	@Override
	public void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer) {
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
	public void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedback) {
		if (container.getExecutor().getSkillChargingTicks(1.0F) > this.getAllowedMaxChargingTicks()) {
			feedback.setFeedbackType(SPSkillExecutionFeedback.FeedbackType.EXPIRED);
		} else {
			container.getServerExecutor().playSound(EpicFightSounds.ROCKET_JUMP.get(), 1.0F, 0.0F, 0.0F);
			container.getServerExecutor().playSound(EpicFightSounds.ENTITY_MOVE.get(), 1.0F, 0.0F, 0.0F);

			int accumulatedTicks = container.getExecutor().getChargingAmount();

			LevelUtil.circleSlamFracture(null, container.getServerExecutor().getOriginal().level(), container.getServerExecutor().getOriginal().position().subtract(0, 1, 0), accumulatedTicks * 0.05D, true, false, false);
			Vec3 entityEyepos = container.getServerExecutor().getOriginal().getEyePosition();
			EpicFightParticles.AIR_BURST.get().spawnParticleWithArgument(container.getServerExecutor().getOriginal().serverLevel(), entityEyepos.x, entityEyepos.y, entityEyepos.z, 0.0D, 0.0D, 2 + 0.05D * container.getServerExecutor().getAccumulatedChargeAmount());

			container.getServerExecutor().playAnimationSynchronized(this.shootAnimation, 0.0F);
			feedback.getBuffer().writeInt(accumulatedTicks);
			container.getDataManager().setData(SkillDataKeys.PROTECT_NEXT_FALL.get(), true);
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

	@Override
	public KeyMapping getKeyMapping() {
		return EpicFightKeyMappings.MOVER_SKILL;
	}

	@Override
	public void holdTick(SkillContainer container) {
		int chargingTicks = container.getExecutor().getSkillChargingTicks();
		
		if (chargingTicks % 5 == 0 && container.getExecutor().getAccumulatedChargeAmount() < this.getMaxChargingTicks()) {
			if (container.getExecutor().consumeForSkill(this, Skill.Resource.STAMINA, this.consumption)) {
				container.getExecutor().setChargingAmount(container.getExecutor().getChargingAmount() + 5);
			}
		}
	}
	
	@Override
	public boolean getCustomConsumptionTooltips(SkillBookScreen.AttributeIconList consumptionList) {
		consumptionList.add(Component.translatable("attribute.name.epicfight.stamina.consume.tooltip"), Component.translatable("attribute.name.epicfight.stamina_per_second.consume", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.consumption), "0.25"), SkillBookScreen.STAMINA_TEXTURE_INFO);
		return true;
	}
}