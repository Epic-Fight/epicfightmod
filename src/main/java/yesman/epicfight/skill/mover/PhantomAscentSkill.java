package yesman.epicfight.skill.mover;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.client.neoevent.MappedMovementInputUpdateEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.api.neoevent.playerpatch.TakeDamageEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;

import java.util.ArrayList;
import java.util.List;

public class PhantomAscentSkill extends Skill {
	private final List<AnimationAccessor<? extends StaticAnimation>> animations = new ArrayList<> (2);
	private int extraJumps;
	private double jumpPower;
	
	public PhantomAscentSkill(SkillBuilder<?> builder) {
		super(builder);
		
		this.animations.add(Animations.BIPED_PHANTOM_ASCENT_FORWARD);
		this.animations.add(Animations.BIPED_PHANTOM_ASCENT_BACKWARD);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.extraJumps = parameters.getInt("extra_jumps");
		this.consumption = 0.2F;
		this.jumpPower = parameters.getDouble("jump_power");
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.CLIENT)
	public void mappedMovementInputEvent(MappedMovementInputUpdateEvent event, SkillContainer skillContainer) {
		if (
			skillContainer.getExecutor().getOriginal().getVehicle() != null ||
			!skillContainer.getExecutor().isEpicFightMode() ||
			skillContainer.getExecutor().getOriginal().getAbilities().flying ||
			skillContainer.getExecutor().isHoldingAny() ||
			skillContainer.getExecutor().getEntityState().inaction()
		) {
			return;
		}
		
		// Check directly from the keybind because event.getMovementInput().isJumping doesn't allow to be set as true while player's jumping
		boolean jumpPressed = isJumpActionPressed();
		boolean jumpPressedPrev = skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK);
		
		if (jumpPressed && !jumpPressedPrev) {
			if (skillContainer.getStack() < 1) {
				return;
			}
			
			int jumpCounter = skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.JUMP_COUNT);
			
			if (jumpCounter > 0 || skillContainer.getExecutor().currentLivingMotion == LivingMotions.FALL) {
				if (jumpCounter < (this.extraJumps + 1)) {
					SkillCastEvent skillexecuteevent = new SkillCastEvent(skillContainer.getExecutor(), skillContainer, null);
					
					if (PlayerPatchEvent.postAndFireSkillListeners(skillexecuteevent).isCanceled()) {
						return;
					}
					
					skillContainer.setResource(0.0F);
					
					if (jumpCounter == 0 && skillContainer.getExecutor().currentLivingMotion == LivingMotions.FALL) {
						skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 2);
					} else {
						skillContainer.getDataManager().setDataF(EpicFightSkillDataKeys.JUMP_COUNT, (v) -> v + 1);
					}
					
					skillContainer.getDataManager().setDataSync(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, true);
					
					float sneakingSpeed = (float)skillContainer.getExecutor().getOriginal().getAttributeValue(Attributes.SNEAKING_SPEED);
                    event.sneakingTick(false, sneakingSpeed);
					
                    final MovementDirection movementDirection = MovementDirection.fromInputState(event.getInputState());
                    final int forward = movementDirection.forward();
                    final int backward = movementDirection.backward();
                    final int left = movementDirection.left();
                    final int right = movementDirection.right();
                    final int vertic = movementDirection.vertical();
                    final int horizon = movementDirection.horizontal();
					int degree = -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon);
					int scale = forward == 0 && backward == 0 && left == 0 && right == 0 ? 0 : (vertic < 0 ? -1 : 1);
					Vec3 forwardHorizontal = Vec3.directionFromRotation(new Vec2(0, skillContainer.getExecutor().getOriginal().getViewYRot(1.0F)));
					Vec3 jumpDir = OpenMatrix4f.transform(OpenMatrix4f.createRotatorDeg(-degree, Vec3f.Y_AXIS), forwardHorizontal.scale(0.15D * scale));
					Vec3 deltaMove = skillContainer.getExecutor().getOriginal().getDeltaMovement();
					skillContainer.getExecutor().getOriginal().setDeltaMovement(deltaMove.x + jumpDir.x, this.jumpPower + skillContainer.getExecutor().getOriginal().getJumpBoostPower(), deltaMove.z + jumpDir.z);
                    event.getPlayerPatch().setModelYRot(EpicFightCameraAPI.getInstance().getForwardYRot() + degree, true);
					skillContainer.getExecutor().playAnimationInClientSide(this.animations.get(vertic < 0 ? 1 : 0), 0.0F);
					ControlEngine.getInstance().releaseAllServedKeys();
				};
			} else {
				skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 1);
			}
		}
		
		skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, jumpPressed);
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.CLIENT)
	public void hurtEventPost(TakeDamageEvent.Pre event, SkillContainer skillContainer) {
		if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.PROTECT_NEXT_FALL)) { // This is not synced
			float damage = event.getDamage();
			
			if (damage < 2.5F) {
				event.attachValueModifier(ValueModifier.setter(0.0F));
			}
			
			skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
		}
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.CLIENT)
	public void livingFallEvent(LivingFallEvent event, SkillContainer skillContainer) {
		skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 0);
		skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, false);
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(this.extraJumps);
		
		return list;
	}

    private static boolean isJumpActionPressed() {
        return InputManager.isActionActive(MinecraftInputAction.JUMP);
    }
}