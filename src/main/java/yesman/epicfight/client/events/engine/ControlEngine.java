package yesman.epicfight.client.events.engine;

import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.PlayerInputState;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class ControlEngine {
	private final Set<Object> packets = Sets.newHashSet();
	private final Minecraft minecraft;
	private LocalPlayer player;
	private LocalPlayerPatch playerPatch;
	private int weaponInnatePressCounter = 0;
	private int sneakPressCounter = 0;
	private int moverPressCounter = 0;
	private int tickSinceLastJump = 0;
	private int lastHotbarLockedTime;
	private boolean weaponInnatePressToggle = false;
	private boolean sneakPressToggle = false;
	private boolean moverPressToggle = false;
	private boolean attackLightPressToggle = false;
	private boolean hotbarLocked;
	private boolean holdingFinished;
	private int reserveCounter;
    /**
     * <b>DEPRECATED:</b> This field is retained for backward compatibility and should not be used
     * for comparisons or method calls on this instance. In future updates, {@link EpicFightInputAction}
     * will be stored directly instead of a vanilla {@link KeyMapping}.
     *
     * <p>Do not rely on this field for new functionality. For mapping a {@link KeyMapping} to an
     * action, use {@link ControlEngine#mapKeyMappingToAction} instead (temporary solution).</p>
     *
     * @see ControlEngine#mapKeyMappingToAction
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
	private KeyMapping reservedKey;
	private SkillSlot reservedOrHoldingSkillSlot;
    /**
     * <b>DEPRECATED:</b> Consider using {@link ControlEngine#isCurrentHoldingAction} or 
     * {@link ControlEngine#isCurrentHoldingActionActive} instead of directly 
     * accessing or comparing this field. This field is retained for backward 
     * compatibility; in future updates, {@link EpicFightInputAction} will be
     * stored directly instead of a vanilla {@link KeyMapping}.
     *
     * @see ControlEngine#mapKeyMappingToAction
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    private KeyMapping currentHoldingKey;
	public Options options;
	
	public ControlEngine() {
		Events.controlEngine = this;
		this.minecraft = Minecraft.getInstance();
		this.options = this.minecraft.options;
	}
	
	public void setPlayerPatch(LocalPlayerPatch playerPatch) {
		this.weaponInnatePressCounter = 0;
		this.weaponInnatePressToggle = false;
		this.sneakPressCounter = 0;
		this.sneakPressToggle = false;
		this.attackLightPressToggle = false;
		this.player = playerPatch.getOriginal();
		this.playerPatch = playerPatch;
	}
	
	public LocalPlayerPatch getPlayerPatch() {
		return this.playerPatch;
	}
	
	public boolean canPlayerMove(EntityState playerState) {
		return !playerState.movementLocked() || this.player.jumpableVehicle() != null;
	}
	
	public boolean canPlayerRotate(EntityState playerState) {
		return !playerState.turningLocked() || this.player.jumpableVehicle() != null;
	}
	
	public void handleEpicFightKeyMappings() {
		// Pause here if playerpatch is null
		if (this.playerPatch == null) {
			return;
		}

        InputManager.triggerOnPress(EpicFightInputAction.OPEN_SKILL_SCREEN, this::openSkillEditor);

        InputManager.triggerOnPress(EpicFightInputAction.OPEN_CONFIG_SCREEN, this::openConfig);

        InputManager.triggerOnPress(EpicFightInputAction.SWITCH_VANILLA_MODEL_DEBUGGING, this::switchVanillaModelDebugging);

        // The "runKeyboardMouseEvent" is used as a workaround to this issue:
        // * https://github.com/Epic-Fight/epicfight/issues/1771
        // * https://github.com/Creators-of-Create/Create/issues/6901

        InputManager.triggerOnPress(
                EpicFightInputAction.ATTACK,
                () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.ATTACK, this::maybeAttack)
        );

        InputManager.triggerOnPress(
                EpicFightInputAction.DODGE,
                () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.DODGE, this::maybeDodge)
        );

        if (InputManager.isActionActive(EpicFightInputAction.GUARD)) this.maybeGuard();

        InputManager.triggerOnPress(
                EpicFightInputAction.WEAPON_INNATE_SKILL,
                () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.WEAPON_INNATE_SKILL, this::handleSeparateWeaponInnateSkill)
        );

        InputManager.triggerOnPress(
                EpicFightInputAction.MOBILITY,
                () -> InputUtils.runKeyboardMouseEvent(EpicFightInputAction.MOBILITY, this::maybePerformMoverSkill)
        );

        InputManager.triggerOnPress(EpicFightInputAction.SWITCH_MODE, this::switchMode);

        InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON, this::toggleLockOnState);

        InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON_SHIFT_LEFT, this::searchNewTargetFromLeft);

        InputManager.triggerOnPress(EpicFightInputAction.LOCK_ON_SHIFT_RIGHT, this::searchNewTargetFromRight);

        if (shouldDisableSwapHandItems()) consumeSwapOffhandKeyClicks();
		
		// Pause here if player is not in battle mode
		if (!this.playerPatch.isEpicFightMode() || Minecraft.getInstance().isPaused()) {
			return;
		}
		
		if (this.player.tickCount - this.lastHotbarLockedTime > 20 && this.hotbarLocked) {
			this.unlockHotkeys();
		}
		
		if (this.weaponInnatePressToggle) {
			if (!InputManager.isActionActive(EpicFightInputAction.WEAPON_INNATE_SKILL)) {
				this.attackLightPressToggle = true;
				this.weaponInnatePressToggle = false;
				this.weaponInnatePressCounter = 0;
			} else {
				if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.WEAPON_INNATE_SKILL, EpicFightInputAction.ATTACK)) {
					if (this.weaponInnatePressCounter > ClientConfig.longPressCounter) {
						if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
							if (!this.player.isSpectator()) {
								this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightInputAction.WEAPON_INNATE_SKILL);
							}
						} else {
							this.lockHotkeys();
						}
						
						this.weaponInnatePressToggle = false;
						this.weaponInnatePressCounter = 0;
					} else {
						this.weaponInnatePressCounter++;
					}
				}
			}
		}
		
		if (this.attackLightPressToggle) {
            SkillSlot slot = SkillSlots.BASIC_ATTACK;
			SkillCastEvent skillCastEvent = this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this);
			
			if (skillCastEvent.isExecutable()) {
				this.player.resetAttackStrengthTicker();
				this.releaseAllServedKeys();
			} else {
				if (!this.player.isSpectator()) {
					this.reserveKey(slot, EpicFightInputAction.ATTACK);
				}
			}
			
			this.lockHotkeys();
			
			this.attackLightPressToggle = false;
			this.weaponInnatePressToggle = false;
			this.weaponInnatePressCounter = 0;
		}
		
		if (this.sneakPressToggle) {
			if (!InputManager.isActionActive(MinecraftInputAction.SNEAK)) {
				SkillSlot skillSlot = (this.playerPatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
				SkillContainer skill = this.playerPatch.getSkill(skillSlot);
				
				if (skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
					this.reserveKey(skillSlot, MinecraftInputAction.SNEAK);
				}
				
				this.sneakPressToggle = false;
				this.sneakPressCounter = 0;
			} else {
				if (this.sneakPressCounter > ClientConfig.longPressCounter) {
					this.sneakPressToggle = false;
					this.sneakPressCounter = 0;
				} else {
					this.sneakPressCounter++;
				}
			}
		}
		
		if (this.currentHoldingKey != null) {
			SkillContainer container = this.playerPatch.getSkill(this.reservedOrHoldingSkillSlot);
			
			if (!container.isEmpty()) {
				if (container.getSkill() instanceof HoldableSkill) {
					if (!this.isCurrentHoldingActionActive()) {
						this.holdingFinished = true;
					}
					
					if (container.getSkill() instanceof ChargeableSkill chargingSkill) {
						if (this.holdingFinished) {
							if (this.playerPatch.getSkillChargingTicks() > chargingSkill.getMinChargingTicks()) {
								container.sendCastRequest(this.playerPatch, this);
								this.releaseAllServedKeys();
							}
						} else if (this.playerPatch.getSkillChargingTicks() >= chargingSkill.getAllowedMaxChargingTicks()) {
							this.releaseAllServedKeys();
						}
					} else {
						if (this.holdingFinished) {
							// Note: Holdable skills are canceled in client first
							this.playerPatch.resetHolding();
							container.getSkill().cancelOnClient(container, container.getSkill().gatherArguments(container, this));
							container.sendCancelRequest(this.playerPatch, this);
							this.releaseAllServedKeys();
						}
					}
				} else {
					this.releaseAllServedKeys();
				}
			}
		}
		
		if (this.reservedKey != null) {
			if (this.reserveCounter > 0) {
				SkillContainer skill = this.playerPatch.getSkill(this.reservedOrHoldingSkillSlot);
				this.reserveCounter--;
				
				if (skill.getSkill() != null) {
					if (skill.sendCastRequest(this.playerPatch, this).isExecutable()) {
						this.releaseAllServedKeys();
						this.lockHotkeys();
					}
				}
			} else {
				this.releaseAllServedKeys();
			}
		}

		if (isSwitchOrDropBlocked()) {
			disableHotbarSlotPresses();
			consumeDropKeyClicks();
		}

        if (this.minecraft.level != null && EpicFightCameraAPI.getInstance().isTPSMode() && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LCONTROL)) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LEFT)) {
                ClientConfig.cameraHorizontalLocation = Math.min(10, ClientConfig.cameraHorizontalLocation + 1);
            }

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_RIGHT)) {
                ClientConfig.cameraHorizontalLocation = Math.max(-10, ClientConfig.cameraHorizontalLocation - 1);
            }

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_UP)) {
                ClientConfig.cameraVerticalLocation = Math.min(5, ClientConfig.cameraVerticalLocation + 1);
            }

            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_DOWN)) {
                ClientConfig.cameraVerticalLocation = Math.max(-2, ClientConfig.cameraVerticalLocation - 1);
            }
        }
	}

    private void openSkillEditor() {
        final CapabilitySkill capabilitySkill = this.playerPatch.getSkillCapability();
        if (capabilitySkill == null) {
            return;
        }
        minecraft.setScreen(new SkillEditScreen(this.player, capabilitySkill));
    }

    private void openConfig() {
        minecraft.setScreen(new IngameConfigurationScreen(null));
    }

    private void switchVanillaModelDebugging() {
        boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
        this.minecraft.keyboardHandler.debugFeedbackTranslated(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off");
    }

    private void maybeAttack() {
        if (!this.playerPatch.isEpicFightMode() || isCurrentHoldingAction(EpicFightInputAction.ATTACK)) {
            return;
        }
        final MinecraftInputAction vanillaAttack = MinecraftInputAction.ATTACK_DESTROY;
        final EpicFightInputAction epicFightAttack = EpicFightInputAction.ATTACK;

        boolean shouldPlayAttackAnimation = this.playerPatch.canPlayAttackAnimation();
        if (vanillaAttack.keyMapping().getKey() == epicFightAttack.keyMapping().getKey() &&
                Minecraft.getInstance().hitResult != null && shouldPlayAttackAnimation) {
            // Not needed for controller inputs.
            // This is called for keyboard/mouse inputs to just reset the internal keymapping counter.
            // It does not cancel the attack input, as that is handled in a mixin on the Minecraft class.
            consumeVanillaAttackKeyClicks();
        }

        if (shouldPlayAttackAnimation) {
            if (!InputManager.isBoundToSamePhysicalInput(epicFightAttack, EpicFightInputAction.WEAPON_INNATE_SKILL)) {
                SkillSlot slot = SkillSlots.BASIC_ATTACK;
                SkillCastEvent skillCastEvent = this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this);

                if (skillCastEvent.isExecutable()) {
                    this.player.resetAttackStrengthTicker();
                    this.attackLightPressToggle = false;
                    this.releaseAllServedKeys();
                } else {
                    if (!this.player.isSpectator()) {
                        this.reserveKey(slot, epicFightAttack);
                    }
                }

                this.lockHotkeys();
                this.attackLightPressToggle = false;
                this.weaponInnatePressToggle = false;
                this.weaponInnatePressCounter = 0;
            } else {
                if (!this.weaponInnatePressToggle) {
                    this.weaponInnatePressToggle = true;
                }
            }
        }
    }

    private void maybeDodge() {
        if (!this.playerPatch.isEpicFightMode() || isCurrentHoldingAction(EpicFightInputAction.DODGE)) {
            return;
        }
        if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.DODGE, MinecraftInputAction.SNEAK)) {
            if (this.player.getVehicle() == null) {
                if (!this.sneakPressToggle) {
                    this.sneakPressToggle = true;
                }
            }
        } else {
            SkillSlot skillCategory = (this.playerPatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
            SkillContainer skill = this.playerPatch.getSkill(skillCategory);

            if (!skill.isEmpty() && skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
                this.reserveKey(SkillSlots.DODGE, EpicFightInputAction.DODGE);
            }
        }
    }

    private void maybeGuard() {
        if (!this.playerPatch.isEpicFightMode() || isCurrentHoldingAction(EpicFightInputAction.GUARD)) {
            return;
        }
        boolean shouldCancelGuard = false;
        
        if (this.playerPatch.isHoldingAny()) {
            shouldCancelGuard = true;
        } else if (ShieldItem.class.isAssignableFrom(this.player.getMainHandItem().getItem().getClass()) || ShieldItem.class.isAssignableFrom(this.player.getOffhandItem().getItem().getClass())) {
            shouldCancelGuard = true;
        }
        
        if (!shouldCancelGuard) {
            SkillCastEvent skillCastEvent = this.playerPatch.getSkill(SkillSlots.GUARD).sendCastRequest(this.playerPatch, this);

            if (skillCastEvent.shouldReserveKey()) {
                if (!this.player.isSpectator()) {
                    this.reserveKey(SkillSlots.GUARD, EpicFightInputAction.GUARD);
                }
            } else {
                this.lockHotkeys();
            }
        }
    }

    private void handleSeparateWeaponInnateSkill() {
        if (!this.playerPatch.isEpicFightMode() || isCurrentHoldingAction(EpicFightInputAction.WEAPON_INNATE_SKILL)) {
            return;
        }
        if (!InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.ATTACK, EpicFightInputAction.WEAPON_INNATE_SKILL)) {
            if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
                if (!this.player.isSpectator()) {
                    this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightInputAction.WEAPON_INNATE_SKILL);
                }
            } else {
                this.lockHotkeys();
            }
        }
    }

    private void maybePerformMoverSkill() {
        if (!this.playerPatch.isEpicFightMode() || this.playerPatch.isHoldingAny()) {
            return;
        }
        if (InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.MOBILITY, MinecraftInputAction.JUMP)) {
            SkillContainer skillContainer = this.playerPatch.getSkill(SkillSlots.MOVER);

            if (!skillContainer.isEmpty()) {
                SkillCastEvent event = new SkillCastEvent(this.playerPatch, skillContainer, skillContainer.getSkill().gatherArguments(skillContainer, this));

                if (skillContainer.canUse(this.playerPatch, event) && this.player.getVehicle() == null) {
                    if (!this.moverPressToggle) {
                        this.moverPressToggle = true;
                    }
                }
            }
        } else {
            // Immediately trigger the skill cast if Mover and Jump are bound to different keys/buttons.
            SkillContainer skill = this.playerPatch.getSkill(SkillSlots.MOVER);
            skill.sendCastRequest(this.playerPatch, this);
        }
    }

    private void switchMode() {
        final boolean canSwitch = EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(this.playerPatch.getOriginal().level());
        if (!canSwitch) {
            this.minecraft.gui.getChat().addMessage(Component.translatable("epicfight.messages.mode_switching_disabled").withStyle(ChatFormatting.RED));
            return;
        }
        this.playerPatch.toggleMode();
    }
    
    private void toggleLockOnState() {
    	EpicFightCameraAPI.getInstance().toggleLockOn();
    }
    
    private void searchNewTargetFromLeft() {
    	EpicFightCameraAPI.getInstance().setNextLockOnTarget(1, true, true);
    }
    
    private void searchNewTargetFromRight() {
    	EpicFightCameraAPI.getInstance().setNextLockOnTarget(-1, true, true);
    }
    
	private void inputTick(Input input) {
        PlayerInputState inputState = InputManager.getInputState(input);
		if (this.moverPressToggle) {
			if (!InputManager.isActionActive(MinecraftInputAction.JUMP)) {
				this.moverPressToggle = false;
				this.moverPressCounter = 0;
				
				if (this.player.onGround()) {
					this.player.noJumpDelay = 0;
                    inputState = inputState.withJumping(true);
                    InputManager.setInputState(inputState);
				}
			} else {
				if (this.moverPressCounter > ClientConfig.longPressCounter) {
					SkillContainer skill = this.playerPatch.getSkill(SkillSlots.MOVER);
					skill.sendCastRequest(this.playerPatch, this);
					
					this.moverPressToggle = false;
					this.moverPressCounter = 0;
				} else {
					this.player.noJumpDelay = 2;
					this.moverPressCounter++;
				}
			}
		}
		
		if (!this.canPlayerMove(this.playerPatch.getEntityState())) {
            inputState = inputState.copyWith(0F, 0F, false, false, false, false, false, false);
            InputManager.setInputState(inputState);
			this.player.sprintTriggerTime = -1;
			this.player.setSprinting(false);
		}
		
		if (this.player.isAlive()) {
			this.playerPatch.getEventListener().triggerEvents(EventType.MOVEMENT_INPUT_EVENT, new MovementInputEvent(this.playerPatch, inputState));
		}
		
		if (this.tickSinceLastJump > 0) this.tickSinceLastJump--;
	}

    /**
     * <b>DEPRECATED:</b> This method is retained for backward compatibility and will 
     * be removed in a future release. Do not use it for new code.
     * <p>Instead of using this method, use
     * {@link #reserveKey(SkillSlot, InputAction)}, which works directly
     * with {@link EpicFightInputAction}.</p>
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated(forRemoval = true)
	private void reserveKey(SkillSlot slot, KeyMapping keyMapping) {
		this.reservedKey = keyMapping;
		this.reservedOrHoldingSkillSlot = slot;
		this.reserveCounter = 8;
	}

    private void reserveKey(SkillSlot slot, InputAction action) {
        reserveKey(slot, action.keyMapping());
    }
	
	public void releaseAllServedKeys() {
		this.holdingFinished = true;
		this.currentHoldingKey = null;
		this.reservedOrHoldingSkillSlot = null;
		this.reserveCounter = -1;
		this.reservedKey = null;
	}
	
	public void setHoldingKey(SkillSlot chargingSkillSlot, KeyMapping keyMapping) {
		this.holdingFinished = false;
		this.currentHoldingKey = keyMapping;
		this.reservedOrHoldingSkillSlot = chargingSkillSlot;
		this.reserveCounter = -1;
		this.reservedKey = null;
	}

	public void lockHotkeys() {
		this.hotbarLocked = true;
		this.lastHotbarLockedTime = this.player.tickCount;
        disableHotbarSlotPresses();
	}
	
	public void unlockHotkeys() {
		this.hotbarLocked = false;
	}
	
	public void addPacketToSend(Object packet) {
		this.packets.add(packet);
	}

    /**
     * <b>DEPRECATED:</b> Use {@link InputManager#isActionActive} instead for controller support,
     * though it only handles Epic Fight supported actions. For checking a custom mod keybind,
     * use the vanilla {@link KeyMapping#isDown}.
     * <p>
     * Please note that there is a difference between vanilla {@link KeyMapping#isDown} and this method,
     * {@link KeyMapping#isDown} may be <code>false</code> in some cases even if the physical key
     * is actually down, for example, if a screen is open.
     * <p>
     * Even though this is a private method, it is retained in case an Epic Fight addon
     * accesses it by bypassing Java private access modifier restriction.
     */
	@SuppressWarnings({"JavadocReference", "DeprecatedIsStillUsed"})
    @Deprecated(forRemoval = true)
    public static boolean isKeyDown(KeyMapping key) {
		if (key.getKey().getType() == InputConstants.Type.KEYSYM) {
			return key.isDown() || GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else if(key.getKey().getType() == InputConstants.Type.MOUSE) {
			return key.isDown() || GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else {
			return false;
		}
	}

    /**
     * <b>DEPRECATED:</b> Use {@link InputManager#triggerOnPress} instead for controller support,
     * though it only handles Epic Fight supported actions. For checking a custom mod keybind,
     * use the vanilla {@link KeyMapping#consumeClick} with a {@code while} statement.
     * <p>
     * Even though this is a private method, it is retained in case an Epic Fight addon
     * accesses it by bypassing Java private access modifier restriction.
     * @see InputManager#isPhysicalKeyDownInternalWorkaround(KeyMapping)
     * @see InputManager#isKeyDown(KeyMapping)
     * @deprecated Consider adapting the Minecraft's {@link KeyMapping#isDown()}, keep in mind that
     *  this may return false in some cases, such as when a screen or chat is open.
     */
    @SuppressWarnings({"JavadocReference", "removal"})
    @Deprecated(forRemoval = true)
	private static boolean isKeyPressed(KeyMapping key, boolean eventCheck) {
		boolean consumes = key.consumeClick();
		
		if (consumes && eventCheck) {
			int mouseButton = InputConstants.Type.MOUSE == key.getKey().getType() ? key.getKey().getValue() : -1;
			InputEvent.InteractionKeyMappingTriggered inputEvent = net.minecraftforge.client.ForgeHooksClient.onClickInput(mouseButton, key, InteractionHand.MAIN_HAND);
			
	        if (inputEvent.isCanceled()) {
	        	return false;
	        }
		}
        
    	return consumes;
	}

    /**
     * <b>DISCOURAGED:</b> Does not support controller mods or other input systems.
     * <p>
     * This method was previously called before {@link Minecraft#handleKeybinds} to disable some vanilla
     * input actions.
     * <p>
     * Rather than relying on this method, consider fixing the problem at the core.
     * For example, to disable vanilla attack:
     * <pre>
     * {@code
     * @Mixin(value = Minecraft.class)
     * public class MixinMinecraft {
     *     @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
     *     private void onVanillaAttack(CallbackInfoReturnable<Boolean> cir) {
     *         cir.cancel();
     *     }
     * }
     * }
     * </pre>
     * <b>NOTE:</b> You may still want to call this method to reset the internal {@link KeyMapping#clickCount} state,
     * as a safety guard to prevent potential issues with other mods.
     * Refer to {@link ControlEngine#consumeVanillaAttackKeyClicks} and {@link ControlEngine#shouldDisableVanillaAttack} as an example.
     *
     * @see ControlEngine#shouldDisableVanillaAttack
     * @see ControlEngine#shouldDisableSwapHandItems
     */
    @SuppressWarnings({"JavadocReference", "DeprecatedIsStillUsed", "StatementWithEmptyBody", "DefaultAnnotationParam"})
    @Deprecated(forRemoval = false)
    public static void makeUnpressed(KeyMapping keyMapping) {
        while (keyMapping.consumeClick()) {
        }
        
        keyMapping.setDown(false);
    }

    /**
     * <b>DISCOURAGED:</b> Does not allow Epic Fight to perform additional
     * logic that is independent of the Minecraft vanilla {@link KeyMapping}.
     * Previously used to set the {@link KeyMapping#isDown} state for sprint keybind,
     * but that is now handled in {@link ControlEngine#setSprintingKeyStateNotDown}.
     * Alternatively, you could use the vanilla @{@link KeyMapping#set} directly.
     *
     * @see ControlEngine#makeUnpressed
     * @see ControlEngine#setSprintingKeyStateNotDown
     */
    @SuppressWarnings("JavadocReference")
    @Deprecated(forRemoval = true)
    public static void setKeyBind(KeyMapping key, boolean setter) {
    	key.setDown(setter);
    }

    /**
     * Sets the state of the sprint keybind ({@link KeyMapping#isDown}) to `false`.
     * <p>
     * This only changes the internal {@link KeyMapping} state and does not actually disable sprinting.
     * To actually stop sprinting, this should usually be called alongside
     * {@link LocalPlayer#setSprinting}.
     * <p>
     * This is intended to support mods and other systems that rely on {@link KeyMapping#isDown()}
     * to check whether the player is sprinting, rather than {@link LocalPlayer#isSprinting()}.
     * This is not needed for controller mods.
     */
    @SuppressWarnings("JavadocReference")
    public static void setSprintingKeyStateNotDown() {
    	MinecraftInputAction.SPRINT.keyMapping().setDown(false);
    }

    /**
     * Disables the player's vanilla attacks while in Epic Fight mode.
     * <p>
     * Previously, we injected into the vanilla {@link Minecraft#handleKeybinds} method
     * and used this workaround to reset the internal counter for the vanilla attack keybind:
     * <pre>
     * {@code
     * // Called before Minecraft#handleKeybinds is called.
     * KeyMapping attack = Minecraft.getInstance().options.keyAttack;
     * while (attack.consumeClick()) {}
     * KeyMapping.set(attack.getKey(), false);
     * }
     * </pre>
     * <p>
     * However, that approach relied on assumptions and did not support other mods, inputs, or systems.
     * The problem is now solved by injecting into {@link Minecraft#startAttack} and
     * canceling the call when the player is in Epic Fight mode.
     * This also means, the player must be always in vanilla mode to perform vanilla attacks, which is as intended.
     *
     * @see ControlEngine#consumeVanillaAttackKeyClicks
     */
    @SuppressWarnings("JavadocReference")
    @ApiStatus.Internal
    public static boolean shouldDisableVanillaAttack() {
        final LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null) {
            return false;
        }
        return playerPatch.isEpicFightMode() && playerPatch.canPlayAttackAnimation();
    }

    /**
     * Disables the swap offhand items while the player is in action.
     * <p>
     * Previously, we injected into the vanilla {@link Minecraft#handleKeybinds} method
     * and used this workaround to reset the internal counter for the vanilla swap offhand keybind:
     * <pre>
     * {@code
     * // Called before Minecraft#handleKeybinds is called.
     * KeyMapping swapOffhand = Minecraft.getInstance().options.keySwapOffhand;
     * while (swapOffhand.consumeClick()) {}
     * KeyMapping.set(swapOffhand.getKey(), false);
     * }
     * </pre>
     * <p>
     * However, that approach relied on assumptions and did not support other mods, inputs, or systems.
     * The problem is now solved by injecting into {@link ClientPacketListener#send} and
     * canceling the call when the player is in action and trying to swap offhand items.
     *
     * @see ControlEngine#consumeSwapOffhandKeyClicks
     */
    @SuppressWarnings("JavadocReference")
    @ApiStatus.Internal
    public static boolean shouldDisableSwapHandItems() {
        final LocalPlayerPatch playerPatch = ClientEngine.getInstance().getPlayerPatch();
        if (playerPatch == null) {
            return false;
        }
        return playerPatch.getEntityState().inaction() || (!playerPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).canBePlacedOffhand());
    }


    /**
     * Previously used to disable the vanilla attack key so the player
     * can't attack entities or break grass when in Epic Fight mode, but that is now handled by
     * {@link yesman.epicfight.mixin.client.MixinMinecraft#onStartVanillaAttack} and
     * {@link yesman.epicfight.mixin.client.MixinMinecraft#onContinueVanillaAttack}.
     * <p>
     * This method now only decrements the internal counter of the vanilla {@link KeyMapping#clickCount}
     * to prevent potential conflicts with other mods. It acts as a safety measure;
     * removing it should no longer cause issues.
     * <p>
     * This method does not rely on {@link InputManager} because it operates solely on
     * the vanilla {@link KeyMapping} behavior.
     * @see ControlEngine#shouldDisableVanillaAttack
     */
    @SuppressWarnings("JavadocReference")
    private static void consumeVanillaAttackKeyClicks() {
        makeUnpressed(MinecraftInputAction.ATTACK_DESTROY.keyMapping());
    }

    /**
     * Previously used to temporarily disable the vanilla swap-offhand key while the player
     * was performing an action or attacking, but this is now handled by
     * {@link yesman.epicfight.mixin.client.MixinClientPacketListener#onBeforeSendPacket}.
     * <p>
     * This method now only decrements the internal counter of the vanilla {@link KeyMapping#clickCount}
     * to prevent potential conflicts with other mods. It serves as a safety measure;
     * removing it should no longer cause any issues.
     * <p>
     * This method does not rely on {@link InputManager} because it operates solely on
     * the vanilla {@link KeyMapping} behavior.
     * @see ControlEngine#shouldDisableSwapHandItems
     */
    @SuppressWarnings("JavadocReference")
    private static void consumeSwapOffhandKeyClicks() {
        makeUnpressed(MinecraftInputAction.SWAP_OFF_HAND.keyMapping());
    }

    /**
     * Disables hotbar slot key presses (keyboard only).
     * <p>
     * This feature is strictly for keyboards and will not support controllers,
     * as controllers have limited buttons. Keyboard users can switch slots via
     * number keys or the mouse wheel. Controller users can only switch using
     * forward/backward buttons.
     *
     * @see ControlEngine#isHotbarCyclingDisabled
     */
    private static void disableHotbarSlotPresses() {
        final KeyMapping[] hotbarSlots = Minecraft.getInstance().options.keyHotbarSlots;
        for (int i = 0; i < 9; ++i) {
            final KeyMapping hotbarSlot = hotbarSlots[i];
            makeUnpressed(hotbarSlot);
        }
    }

    /**
     * Previously used to temporarily disable the vanilla item drop key while the player
     * was performing an action or attacking, but this is now handled by
     * {@link yesman.epicfight.mixin.client.MixinLocalPlayer#onDrop}.
     * <p>
     * This method now only decrements the internal counter of the vanilla {@link KeyMapping#clickCount}
     * to prevent potential conflicts with other mods. It serves as a safety measure;
     * removing it should no longer cause any issues.
     * <p>
     */
    @SuppressWarnings("JavadocReference")
    private static void consumeDropKeyClicks() {
        makeUnpressed(MinecraftInputAction.DROP.keyMapping());
    }

    /**
     * Maps a {@link KeyMapping} to its corresponding input action, if defined.
     * <p>
     * Each {@link InputAction} enum constant has an associated {@link KeyMapping},
     * but not every {@link KeyMapping} corresponds to an {@link EpicFightInputAction}, so this may return {@code null}.
     * Using {@link KeyMapping} directly does not support controllers.
     * <p>
     * Ideally, this workaround should not exist. The code should depend on {@link EpicFightInputAction} directly
     * instead of storing {@link KeyMapping} instances. However, since some classes and Epic Fight addons still rely on:
     * <ul>
     *   <li>{@link HoldableSkill#getKeyMapping}</li>
     *   <li>{@link ControlEngine#currentHoldingKey}</li>
     *   <li>{@link ControlEngine#reservedKey}</li>
     * </ul>
     * this method remains temporarily for backward compatibility. Future updates should refactor these dependencies
     * to remove reliance on {@link KeyMapping}, allowing this method to be fully removed.
     * <p>
     * Sometimes, it makes sense to use this method, for example, if you're using an event such as {@link InputEvent.InteractionKeyMappingTriggered},
     * which provides only a {@link KeyMapping}.
     */
    private static @Nullable InputAction mapKeyMappingToAction(@NotNull KeyMapping keyMapping) {
        return InputAction.fromKeyMapping(keyMapping);
    }

    /**
     * Checks if the specified input action is currently being held.
     *
     * @param other the input action to check
     * @return {@code true} if the given action is currently held; otherwise {@code false}
     * @see ControlEngine#mapKeyMappingToAction
     */
    private boolean isCurrentHoldingAction(@NotNull InputAction other) {
        if (currentHoldingKey == null) {
            return false;
        }
        final InputAction currentHoldingAction = mapKeyMappingToAction(currentHoldingKey);
        if (currentHoldingAction == null) {
            // Fallback for legacy or custom key mappings.
            // This is IMPORTANT to prevent addon breakage; this allows custom keybinds from other mods,
            // but controller inputs will not support those custom keybinds.
            return currentHoldingKey == other.keyMapping();
        }
        return other == currentHoldingAction;
    }
    
    private boolean isCurrentHoldingActionActive() {
        if (currentHoldingKey == null) {
            return false;
        }
        final InputAction currentHoldingAction = mapKeyMappingToAction(currentHoldingKey);
        if (currentHoldingAction == null) {
            // Fallback for legacy or custom key mappings.
            // This is IMPORTANT to prevent addon breakage; this allows custom keybinds from other mods,
            // but controller inputs will not support those custom keybinds.
            return isKeyDown(currentHoldingKey);
        }
        return InputManager.isActionActive(currentHoldingAction);
    }

    /**
     * Determines whether hotbar cycling should be disabled.
     * <p>
     * Used internally in {@link InputEvent.MouseScrollingEvent} and
     * {@link yesman.epicfight.mixin.client.MixinInventory}. Cancelling the mouse
     * scroll event disables cycling for vanilla mouse input, but other input
     * systems (e.g., controllers) still call {@link Inventory#swapPaint}, so we
     * cancel those calls as well. This ensures universal behavior while
     * maximizing compatibility.
     *
     * @return {@code true} if hotbar item cycling should be disabled; {@code false} otherwise.
     * */
    @ApiStatus.Internal
    public static boolean isHotbarCyclingDisabled() {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayerPatch localPlayerPatch = ClientEngine.getInstance().getPlayerPatch();
        return minecraft.player != null && localPlayerPatch != null && !localPlayerPatch.getEntityState().canSwitchHoldingItem() && minecraft.screen == null;
    }

    /**
     * Checks whether the player is blocked from switching or dropping the held item.
     *
     * @return true if switching or dropping is blocked, false otherwise
     */
    public boolean isSwitchOrDropBlocked() {
        return !this.playerPatch.getEntityState().canSwitchHoldingItem() || this.hotbarLocked;
    }

    public boolean moverToggling() {
		return this.moverPressToggle;
	}
	
	public boolean sneakToggling() {
		return this.sneakPressToggle;
	}
	
	public boolean attackToggling() {
		return this.attackLightPressToggle;
	}
	
	public boolean weaponInnateToggling() {
		return this.weaponInnatePressToggle;
	}
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
	public static class Events {
		static ControlEngine controlEngine;
		
		@SubscribeEvent
		public static void livingJumpEvent(LivingJumpEvent event) {
			if (event.getEntity() == controlEngine.player) {
				controlEngine.tickSinceLastJump = 5;
			}
		}
		
		@SubscribeEvent
		public static void mouseScrollEvent(InputEvent.MouseScrollingEvent event) {
			// Disables item switching for the vanilla mouse input
			if (isHotbarCyclingDisabled()) {
				event.setCanceled(true);
			}
		}
		
		@SubscribeEvent
		public static void moveInputEvent(MovementInputUpdateEvent event) {
			if (controlEngine.playerPatch == null) {
				return;
			}
			
			controlEngine.inputTick(event.getInput());
		}
		
		@SubscribeEvent
		public static void clientTickEndEvent(TickEvent.ClientTickEvent event) {
			if (controlEngine.minecraft.player == null) {
				return;
			}
			
			if (event.phase == TickEvent.Phase.END) {
				for (Object packet : controlEngine.packets) {
					EpicFightNetworkManager.sendToServer(packet);
				}
				
				controlEngine.packets.clear();
			}
		}
		
		@SubscribeEvent
		public static void interactionEvent(InteractionKeyMappingTriggered event) {
			if (controlEngine.minecraft.player == null || controlEngine.minecraft.hitResult == null) return;
            final InputAction triggeredAction = mapKeyMappingToAction(event.getKeyMapping());
            
            if (triggeredAction == null) {
                // The key mapping corresponds to a fixed vanilla action (attack, pick block, or use item).
                // These are predictable, so it's safe to map the key mapping to an input action.
                return;
            }
            
			if (
                triggeredAction == MinecraftInputAction.ATTACK_DESTROY &&
                InputManager.isBoundToSamePhysicalInput(EpicFightInputAction.ATTACK, MinecraftInputAction.ATTACK_DESTROY) &&
				controlEngine.minecraft.hitResult.getType() == HitResult.Type.BLOCK &&
				ClientConfig.combatPreferredItems.contains(controlEngine.player.getMainHandItem().getItem())
			) {
				BlockPos bp = ((BlockHitResult) controlEngine.minecraft.hitResult).getBlockPos();
				BlockState bs = controlEngine.minecraft.level.getBlockState(bp);
				
				// Cancel digging when the player swings combat preferred items
				if (!controlEngine.player.getMainHandItem().getItem().canAttackBlock(bs, controlEngine.player.level(), bp, controlEngine.player) || controlEngine.player.getMainHandItem().getDestroySpeed(bs) <= 1.0F) {
					event.setSwingHand(false);
					event.setCanceled(true);
				}
			}
			
			LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(controlEngine.minecraft.player, LocalPlayerPatch.class);
			
			if (playerpatch == null) {
				return;
			}
			
			if (playerpatch.isVanillaMode() && triggeredAction == MinecraftInputAction.ATTACK_DESTROY) {
				// Blocks vanilla attacks against living entities
				if (
					!EpicFightGameRules.ALLOW_VANILLA_MELEE.getRuleValue(playerpatch.getOriginal().level()) &&
					controlEngine.minecraft.hitResult instanceof EntityHitResult entityHitResult &&
					(entityHitResult.getEntity() instanceof LivingEntity || entityHitResult.getEntity() instanceof PartEntity)
				) {
					event.setSwingHand(false);
					event.setCanceled(true);
				}
			}
			
			if (
                triggeredAction == MinecraftInputAction.USE &&
                InputManager.isBoundToSamePhysicalInput(MinecraftInputAction.USE, EpicFightInputAction.GUARD)
			) {
				MutableBoolean canGuard = new MutableBoolean(false);
				MutableBoolean vanillaMode = new MutableBoolean(false);
				SkillContainer skillcontainer = playerpatch.getSkill(SkillSlots.GUARD);
				
				if (playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.VANILLA) {
					vanillaMode.setTrue();
				}
				
				if (skillcontainer.getSkill() != null && skillcontainer.getSkill().canExecute(skillcontainer)) {
					canGuard.setValue(true);
				}
				
				if (!vanillaMode.getValue()) {
					if (controlEngine.minecraft.hitResult.getType() == HitResult.Type.MISS) {
						if (canGuard.booleanValue() && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
							event.setSwingHand(false);
							event.setCanceled(true);
						}
					} else {
						if (canGuard.booleanValue()) {
							InteractionResult interactionResult = switch (controlEngine.minecraft.hitResult.getType()) {
								case ENTITY -> {
									yield ((EntityHitResult)controlEngine.minecraft.hitResult).getEntity().interact(controlEngine.minecraft.player, event.getHand());
								}
								case BLOCK -> {
									BlockHitResult blockHitResult = ((BlockHitResult)controlEngine.minecraft.hitResult);
									BlockPos blockpos = blockHitResult.getBlockPos();
									BlockState blockstate = controlEngine.minecraft.level.getBlockState(blockpos);
									FakeLevel fakeLevelForSimulation = FakeLevel.getFakeLevel(controlEngine.minecraft.level);
									FakeLevel.FakeClientPlayer fakePlayerForSimulation = FakeLevel.getFakePlayer(controlEngine.minecraft.player.getGameProfile());
									
									yield blockstate.use(fakeLevelForSimulation, fakePlayerForSimulation, event.getHand(), blockHitResult);
								}
								default -> throw new IllegalArgumentException();
							};
							
							if (interactionResult != InteractionResult.PASS && ClientConfig.keyConflictResolveScope.cancelInteraction()) {
								event.setSwingHand(false);
								event.setCanceled(true);
							} else if (interactionResult == InteractionResult.PASS && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
								event.setSwingHand(false);
								event.setCanceled(true);
							}
						}
					}
				}
			}
		}
	}
}