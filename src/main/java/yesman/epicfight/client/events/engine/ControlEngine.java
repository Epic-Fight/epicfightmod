package yesman.epicfight.client.events.engine;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent.LivingJumpEvent;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.neoevent.playerpatch.SkillCastEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.screen.SkillEditScreen;
import yesman.epicfight.client.gui.screen.config.IngameConfigurationScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@OnlyIn(Dist.CLIENT)
public class ControlEngine implements IEventBasedEngine {
	private static final ControlEngine INSTANCE = new ControlEngine();
	
	public static ControlEngine getInstance() {
		return INSTANCE;
	}
	
	private final Set<CustomPacketPayload> packetsToSend = new HashSet<> ();
	private final Minecraft minecraft;
	private LocalPlayer player;
	private LocalPlayerPatch playerpatch;
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
	private KeyMapping reservedKey;
	private SkillSlot reservedOrHoldingSkillSlot;
	private KeyMapping currentHoldingKey;
	public Options options;
	
	private ControlEngine() {
		this.minecraft = Minecraft.getInstance();
		
		if (this.minecraft != null) {
			this.options = this.minecraft.options;
		}
	}
	
	public void reloadPlayerPatch(LocalPlayerPatch playerpatch) {
		this.weaponInnatePressCounter = 0;
		this.weaponInnatePressToggle = false;
		this.sneakPressCounter = 0;
		this.sneakPressToggle = false;
		this.attackLightPressToggle = false;
		this.player = playerpatch.getOriginal();
		this.playerpatch = playerpatch;
	}
	
	public LocalPlayerPatch getPlayerPatch() {
		return this.playerpatch;
	}
	
	public boolean canPlayerMove(EntityState playerState) {
		return !playerState.movementLocked() || this.player.jumpableVehicle() != null;
	}
	
	public boolean canPlayerRotate(EntityState playerState) {
		return !playerState.turningLocked() || this.player.jumpableVehicle() != null;
	}
	
	public void handleEpicFightKeyMappings() {
		// Pause here if playerpatch is null
		if (this.playerpatch == null) {
			return;
		}
		
		if (isKeyPressed(EpicFightKeyMappings.SKILL_EDIT, false)) {
			if (this.playerpatch.getPlayerSkills() != null) {
				Minecraft.getInstance().setScreen(new SkillEditScreen(this.player, this.playerpatch.getPlayerSkills()));
			}
		}
		
		if (isKeyPressed(EpicFightKeyMappings.OPEN_CONFIG_SCREEN, false)) {
			Minecraft.getInstance().setScreen(new IngameConfigurationScreen(null, null));
		}
		
		if (isKeyPressed(EpicFightKeyMappings.SWITCH_VANILLA_MODEL_DEBUGGING, false)) {
			boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
			this.minecraft.keyboardHandler.debugFeedbackTranslated(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off");
		}
		
		while (isKeyPressed(EpicFightKeyMappings.ATTACK, true)) {
			if (this.playerpatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.ATTACK) {
				boolean shouldPlayAttackAnimation = this.playerpatch.canPlayAttackAnimation();
				
				if (this.options.keyAttack.getKey() == EpicFightKeyMappings.ATTACK.getKey() && this.minecraft.hitResult != null) {
					// Disable vanilla attack key
					if (shouldPlayAttackAnimation) {
						makeUnpressed(this.options.keyAttack);
					}
				}
				
				if (shouldPlayAttackAnimation) {
					if (!EpicFightKeyMappings.ATTACK.getKey().equals(EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey())) {
						SkillContainer airSlash = this.playerpatch.getSkill(SkillSlots.AIR_SLASH);
						SkillSlot slot = (this.tickSinceLastJump > 0 && airSlash.getSkill() != null && airSlash.getSkill().canExecute(airSlash)) ? SkillSlots.AIR_SLASH : SkillSlots.COMBO_ATTACKS;
						SkillCastEvent skillCastEvent = this.playerpatch.getSkill(slot).sendCastRequest(this.playerpatch, this);
						
						if (skillCastEvent.isExecutable()) {
							this.player.resetAttackStrengthTicker();
							this.attackLightPressToggle = false;
							this.releaseAllServedKeys();
						} else {
							if (!this.player.isSpectator() && slot == SkillSlots.COMBO_ATTACKS) {
								this.reserveKey(slot, EpicFightKeyMappings.ATTACK);
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
		}
		
		while (isKeyPressed(EpicFightKeyMappings.DODGE, true)) {
			if (this.playerpatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.DODGE) {
				if (EpicFightKeyMappings.DODGE.getKey().getValue() == this.options.keyShift.getKey().getValue()) {
					if (this.player.getVehicle() == null) {
						if (!this.sneakPressToggle) {
							this.sneakPressToggle = true;
						}
					}
				} else {
					SkillSlot skillCategory = (this.playerpatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
					SkillContainer skill = this.playerpatch.getSkill(skillCategory);
					
					if (!skill.isEmpty() && skill.sendCastRequest(this.playerpatch, this).shouldReserveKey()) {
						this.reserveKey(SkillSlots.DODGE, EpicFightKeyMappings.DODGE);
					}
				}
			}
		}
		
		if (isKeyDown(EpicFightKeyMappings.GUARD)) {
			if (this.playerpatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.GUARD) {
				boolean shouldCancelGuard = false;
				
				if (this.playerpatch.isHoldingAny()) {
					shouldCancelGuard = true;
				} else if (this.player.getMainHandItem().getUseAnimation() == UseAnim.BLOCK || this.player.getOffhandItem().getUseAnimation() == UseAnim.BLOCK) {
					shouldCancelGuard = true;
				}
				
				if (!shouldCancelGuard) {
					SkillCastEvent skillCastEvent = this.playerpatch.getSkill(SkillSlots.GUARD).sendCastRequest(this.playerpatch, this);
					
					if (skillCastEvent.shouldReserveKey()) {
						if (!this.player.isSpectator()) {
							this.reserveKey(SkillSlots.GUARD, EpicFightKeyMappings.GUARD);
						}
					} else {
						this.lockHotkeys();
					}
				}
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.WEAPON_INNATE_SKILL, true)) {
			if (this.playerpatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.WEAPON_INNATE_SKILL) {
				if (!EpicFightKeyMappings.ATTACK.getKey().equals(EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey())) {
					if (this.playerpatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerpatch, this).shouldReserveKey()) {
						if (!this.player.isSpectator()) {
							this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightKeyMappings.WEAPON_INNATE_SKILL);
						}
					} else {
						this.lockHotkeys();
					}
				}
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.MOVER_SKILL, true)) {
			if (this.playerpatch.isEpicFightMode() && !this.playerpatch.isHoldingAny()) {
				if (EpicFightKeyMappings.MOVER_SKILL.getKey().getValue() == this.options.keyJump.getKey().getValue()) {
					SkillContainer skillContainer = this.playerpatch.getSkill(SkillSlots.MOVER);
					SkillCastEvent event = new SkillCastEvent(this.playerpatch, skillContainer, null);
					
					if (skillContainer.canUse(this.playerpatch, event) && this.player.getVehicle() == null) {
						if (!this.moverPressToggle) {
							this.moverPressToggle = true;
						}
					}
				} else {
					SkillContainer skill = this.playerpatch.getSkill(SkillSlots.MOVER);
					skill.sendCastRequest(this.playerpatch, this);
				}
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.SWITCH_MODE, false)) {
			if (EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(this.playerpatch.getOriginal().level())) {
				this.playerpatch.toggleMode();
			} else {
				this.minecraft.gui.getChat().addMessage(Component.translatable("epicfight.messages.mode_switching_disabled").withStyle(ChatFormatting.RED));
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.LOCK_ON, false)) {
			this.playerpatch.toggleLockOn();
		}
		
		// Disable swap hand items
		if (this.playerpatch.getEntityState().inaction() || (!this.playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).canBePlacedOffhand())) {
			makeUnpressed(this.minecraft.options.keySwapOffhand);
		}
		
		// Pause here if player is not in battle mode
		if (!this.playerpatch.isEpicFightMode() || Minecraft.getInstance().isPaused()) {
			return;
		}
		
		if (this.player.tickCount - this.lastHotbarLockedTime > 20 && this.hotbarLocked) {
			this.unlockHotkeys();
		}
		
		if (this.weaponInnatePressToggle) {
			if (!isKeyDown(EpicFightKeyMappings.WEAPON_INNATE_SKILL)) {
				this.attackLightPressToggle = true;
				this.weaponInnatePressToggle = false;
				this.weaponInnatePressCounter = 0;
			} else {
				if (EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey().equals(EpicFightKeyMappings.ATTACK.getKey())) {
					if (this.weaponInnatePressCounter > ClientConfig.longPressCounter) {
						if (this.playerpatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerpatch, this).shouldReserveKey()) {
							if (!this.player.isSpectator()) {
								this.reserveKey(SkillSlots.WEAPON_INNATE, EpicFightKeyMappings.WEAPON_INNATE_SKILL);
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
			SkillContainer airSlash = this.playerpatch.getSkill(SkillSlots.AIR_SLASH);
			SkillSlot slot = (this.tickSinceLastJump > 0 && airSlash.getSkill() != null && airSlash.getSkill().canExecute(airSlash)) ? SkillSlots.AIR_SLASH : SkillSlots.COMBO_ATTACKS;
			SkillCastEvent skillCastEvent = this.playerpatch.getSkill(slot).sendCastRequest(this.playerpatch, this);
			
			if (skillCastEvent.isExecutable()) {
				this.player.resetAttackStrengthTicker();
				this.releaseAllServedKeys();
			} else {
				if (!this.player.isSpectator() && slot == SkillSlots.COMBO_ATTACKS) {
					this.reserveKey(slot, EpicFightKeyMappings.ATTACK);
				}
			}
			
			this.lockHotkeys();
			
			this.attackLightPressToggle = false;
			this.weaponInnatePressToggle = false;
			this.weaponInnatePressCounter = 0;
		}
		
		if (this.sneakPressToggle) {
			if (!isKeyDown(this.options.keyShift)) {
				SkillSlot skillSlot = (this.playerpatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
				SkillContainer skill = this.playerpatch.getSkill(skillSlot);
				
				if (skill.sendCastRequest(this.playerpatch, this).shouldReserveKey()) {
					this.reserveKey(skillSlot, this.options.keyShift);
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
			SkillContainer container = this.playerpatch.getSkill(this.reservedOrHoldingSkillSlot);
			
			if (!container.isEmpty()) {
				if (container.getSkill() instanceof HoldableSkill) {
					if (!isKeyDown(this.currentHoldingKey)) {
						this.holdingFinished = true;
					}
					
					if (container.getSkill() instanceof ChargeableSkill chargingSkill) {
						if (this.holdingFinished) {
							if (this.playerpatch.getSkillChargingTicks() > chargingSkill.getMinChargingTicks()) {
								container.sendCastRequest(this.playerpatch, this);
								this.releaseAllServedKeys();
							}
						} else if (this.playerpatch.getSkillChargingTicks() >= chargingSkill.getAllowedMaxChargingTicks()) {
							this.releaseAllServedKeys();
						}
					} else {
						if (this.holdingFinished) {
							// Note: Holdable skills are canceled in client first
							this.playerpatch.resetHolding();
							CompoundTag arguments = new CompoundTag();
							container.getSkill().gatherArguments(container, this, arguments);
							container.getSkill().cancelOnClient(container, arguments);
							container.sendCancelRequest(this.playerpatch, this);
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
				SkillContainer skill = this.playerpatch.getSkill(this.reservedOrHoldingSkillSlot);
				this.reserveCounter--;
				
				if (skill.getSkill() != null) {
					if (skill.sendCastRequest(this.playerpatch, this).isExecutable()) {
						this.releaseAllServedKeys();
						this.lockHotkeys();
					}
				}
			} else {
				this.releaseAllServedKeys();
			}
		}
		
		if (!this.playerpatch.getEntityState().canSwitchHoldingItem() || this.hotbarLocked) {
			for (int i = 0; i < 9; ++i) {
				while (this.options.keyHotbarSlots[i].consumeClick());
			}
			
			while (this.options.keyDrop.consumeClick());
		}
	}
	
	private void inputTick(Input input) {
		if (this.moverPressToggle) {
			if (!isKeyDown(this.options.keyJump)) {
				this.moverPressToggle = false;
				this.moverPressCounter = 0;
				
				if (this.player.onGround()) {
					this.player.noJumpDelay = 0;
					input.jumping = true;
				}
			} else {
				if (this.moverPressCounter > ClientConfig.longPressCounter) {
					SkillContainer skill = this.playerpatch.getSkill(SkillSlots.MOVER);
					skill.sendCastRequest(this.playerpatch, this);
					
					this.moverPressToggle = false;
					this.moverPressCounter = 0;
				} else {
					this.player.noJumpDelay = 2;
					this.moverPressCounter++;
				}
			}
		}
		
		if (!this.canPlayerMove(this.playerpatch.getEntityState())) {
			input.forwardImpulse = 0F;
			input.leftImpulse = 0F;
			input.up = false;
			input.down = false;
			input.left = false;
			input.right = false;
			input.jumping = false;
			input.shiftKeyDown = false;
			this.player.sprintTriggerTime = -1;
			this.player.setSprinting(false);
		}
		
		if (this.tickSinceLastJump > 0) this.tickSinceLastJump--;
	}
	
	private void reserveKey(SkillSlot slot, KeyMapping keyMapping) {
		this.reservedKey = keyMapping;
		this.reservedOrHoldingSkillSlot = slot;
		this.reserveCounter = 8;
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
		
		for (int i = 0; i < 9; ++i) {
			while (this.options.keyHotbarSlots[i].consumeClick());
		}
	}
	
	public void unlockHotkeys() {
		this.hotbarLocked = false;
	}
	
	public void addPacketToSend(CustomPacketPayload packet) {
		this.packetsToSend.add(packet);
	}
	
	public static boolean isKeyDown(KeyMapping key) {
		if (key.getKey().getType() == InputConstants.Type.KEYSYM) {
			return key.isDown() || GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else if(key.getKey().getType() == InputConstants.Type.MOUSE) {
			return key.isDown() || GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), key.getKey().getValue()) > 0;
		} else {
			return false;
		}
	}
	
	private static boolean isKeyPressed(KeyMapping key, boolean eventCheck) {
		boolean consumes = key.consumeClick();
		
		if (consumes && eventCheck) {
			int mouseButton = InputConstants.Type.MOUSE == key.getKey().getType() ? key.getKey().getValue() : -1;
			InputEvent.InteractionKeyMappingTriggered inputEvent = ClientHooks.onClickInput(mouseButton, key, InteractionHand.MAIN_HAND);
			
	        if (inputEvent.isCanceled()) {
	        	return false;
	        }
		}
        
    	return consumes;
	}
	
	public static void makeUnpressed(KeyMapping keyMapping) {
		while (keyMapping.consumeClick()) {}
		setKeyBind(keyMapping, false);
	}
	
	public static void setKeyBind(KeyMapping key, boolean setter) {
		KeyMapping.set(key.getKey(), setter);
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
	
	/******************
	 * Event listeners
	 ******************/
	private void epicfight$mouseScrollEvent(InputEvent.MouseScrollingEvent event) {
		// Disable item switching
		if (this.player != null && this.playerpatch != null && !this.playerpatch.getEntityState().canSwitchHoldingItem() && this.minecraft.screen == null) {
			event.setCanceled(true);
		}
	}
	
	private void epicfight$moveInputEvent(MovementInputUpdateEvent event) {
		if (this.playerpatch == null) {
			return;
		}
		
		this.inputTick(event.getInput());
		this.playerpatch.getPlayerSkills().fireSkillEvents(EpicFightMod.MODID, event);
	}
	
	private void epicfight$clientTickEndEvent(ClientTickEvent.Post event) {
		if (this.player == null) {
			return;
		}
		
		this.packetsToSend.forEach(EpicFightNetworkManager::sendToServer);
		this.packetsToSend.clear();
	}

	private void epicfight$interactionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
		if (this.player == null) {
			return;
		}
		
		if (
			event.getKeyMapping() == this.minecraft.options.keyAttack &&
			EpicFightKeyMappings.ATTACK.getKey() == this.minecraft.options.keyAttack.getKey() &&
			this.minecraft.hitResult.getType() == HitResult.Type.BLOCK &&
			ClientConfig.combatPreferredItems.contains(this.player.getMainHandItem().getItem())
		) {
			BlockPos bp = ((BlockHitResult)this.minecraft.hitResult).getBlockPos();
			BlockState bs = this.minecraft.level.getBlockState(bp);
			
			if (!this.player.getMainHandItem().getItem().canAttackBlock(bs, this.player.level(), bp, this.player) || this.player.getMainHandItem().getDestroySpeed(bs) <= 1.0F) {
				event.setSwingHand(false);
				event.setCanceled(true);
			}
		}
		
		if (
			event.getKeyMapping() == this.minecraft.options.keyUse &&
			event.getKeyMapping().getKey().equals(EpicFightKeyMappings.GUARD.getKey())
		) {
			MutableBoolean canGuard = new MutableBoolean();
			
			EpicFightCapabilities.getUnparameterizedEntityPatch(this.minecraft.player, LocalPlayerPatch.class).ifPresent(playerpatch -> {
				SkillContainer skillcontainer = playerpatch.getSkill(SkillSlots.GUARD);
				
				if (skillcontainer.getSkill() != null && skillcontainer.getSkill().canExecute(skillcontainer)) {
					canGuard.setValue(true);
				}
			});
			
			if (this.minecraft.hitResult.getType() == HitResult.Type.MISS) {
				if (canGuard.booleanValue() && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
					event.setSwingHand(false);
					event.setCanceled(true);
				}
			} else {
				if (canGuard.booleanValue()) {
					InteractionResult interactionResult = switch (this.minecraft.hitResult.getType()) {
						case ENTITY -> {
							yield ((EntityHitResult)this.minecraft.hitResult).getEntity().interact(this.minecraft.player, event.getHand());
						}
						case BLOCK -> {
							BlockHitResult blockHitResult = ((BlockHitResult)this.minecraft.hitResult);
							yield this.minecraft.gameMode.performUseItemOn(this.player, event.getHand(), blockHitResult);
						}
						default -> throw new IllegalArgumentException();
					};
					
					if (interactionResult.consumesAction() && ClientConfig.keyConflictResolveScope.cancelInteraction()) {
						event.setSwingHand(false);
						event.setCanceled(true);
					} else if (!interactionResult.consumesAction() && ClientConfig.keyConflictResolveScope.cancelItemUse()) {
						event.setSwingHand(false);
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	private void epicfight$livingJumpEvent(LivingJumpEvent event) {
		if (event.getEntity() == this.player) {
			this.tickSinceLastJump = 5;
		}
	}
	
	/**********************
	 * Event listeners end
	 **********************/
	@Override
	public void gameEventBus(IEventBus gameEventBus) {
		gameEventBus.addListener(this::epicfight$mouseScrollEvent);
		gameEventBus.addListener(this::epicfight$moveInputEvent);
		gameEventBus.addListener(this::epicfight$clientTickEndEvent);
		gameEventBus.addListener(this::epicfight$interactionKeyMappingTriggered);
		gameEventBus.addListener(this::epicfight$livingJumpEvent);
	}
	
	@Override
	public void modEventBus(IEventBus modEventBus) {
	}
}