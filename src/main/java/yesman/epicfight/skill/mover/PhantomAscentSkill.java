package yesman.epicfight.skill.mover;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.event.EntityEventListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.SkillCastEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;

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

    @Override
    public void onInitiate(SkillContainer skillContainer, EntityEventListener eventListener) {
        super.onInitiate(skillContainer, eventListener);

        eventListener.registerEvent(
            EpicFightClientEventHooks.Control.MAPPED_MOVEMENT_INPUT_UPDATE,
            event -> {
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
                        if (jumpCounter < this.extraJumps + 1) {
                            SkillCastEvent skillCastEvent = new SkillCastEvent(skillContainer.getExecutor(), skillContainer, null);
                            EpicFightEventHooks.Player.CAST_SKILL.postWithListener(skillCastEvent, eventListener);

                            if (skillCastEvent.isCanceled()) {
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

                            float launchingYRot = EpicFightCameraAPI.getInstance().getForwardYRot() + degree;
                            Vec3 horizontalLaunchingDirection = MathUtils.getVectorForRotation(0.0F, launchingYRot).scale(0.15D * scale);
                            Vec3 currentDelta = skillContainer.getExecutor().getOriginal().getDeltaMovement();
                            Vec3 newDelta = currentDelta.add(horizontalLaunchingDirection);
                            skillContainer.getExecutor().getOriginal().setDeltaMovement(newDelta.x, this.jumpPower + skillContainer.getExecutor().getOriginal().getJumpBoostPower(), currentDelta.z);
                            skillContainer.getExecutor().setModelYRot(EpicFightCameraAPI.getInstance().getForwardYRot() + degree, true);
                            skillContainer.getExecutor().playAnimationInClientSide(this.animations.get(vertic < 0 ? 1 : 0), 0.0F);
                            ControlEngine.getInstance().releaseAllServedKeys();
                        };
                    } else {
                        skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 1);
                    }
                }

                skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, jumpPressed);
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.TAKE_DAMAGE_INCOME,
            event -> {
                if (event.getDamageSource().is(DamageTypeTags.IS_FALL) && skillContainer.getDataManager().getDataValue(EpicFightSkillDataKeys.PROTECT_NEXT_FALL)) {
                    float damage = event.getDamage();

                    if (damage < 2.5F) {
                        event.cancel();
                    }

                    skillContainer.getDataManager().setData(EpicFightSkillDataKeys.PROTECT_NEXT_FALL, false);
                }
            },
            this
        );

        eventListener.registerEvent(
            EpicFightEventHooks.Entity.ON_FALL,
            event -> {
                skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_COUNT, 0);
                skillContainer.getDataManager().setData(EpicFightSkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK, false);
            },
            this
        );
    }

    @Override
    public boolean canExecute(SkillContainer container) {
        return false;
    }

    @Override @ClientOnly
    public List<Object> getTooltipArgsOfScreen(List<Object> list) {
        list.add(this.extraJumps);
        return list;
    }

    private static boolean isJumpActionPressed() {
        return InputManager.isActionActive(MinecraftInputAction.JUMP);
    }
}