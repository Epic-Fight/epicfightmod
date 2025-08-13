package yesman.epicfight.client.events.engine;

import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.types.EntityState;
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
import yesman.epicfight.world.entity.eventlistener.MovementInputEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@OnlyIn(Dist.CLIENT)
public class ControlEngine {
	private final Set<Object> packets = Sets.newHashSet();
	private final Minecraft minecraft;
	private LocalPlayer player;
	private LocalPlayerPatch playerPatch;
	private int weaponInnatePressCounter = 0;
	private int sneakPressCounter = 0;
	private int moverPressCounter = 0;
	private int lastHotbarLockedTime;
	private boolean weaponInnatePressToggle = false;
	private boolean sneakPressToggle = false;
	private boolean moverPressToggle = false;
	private boolean attackLightPressToggle = false;
	private boolean hotbarLocked;
	private boolean holdingFinished;
	private int reserveCounter;
	private KeyMapping reservedKey;
	private SkillSlot reservedOrChargingSkillSlot;
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
		
		if (isKeyPressed(EpicFightKeyMappings.SKILL_EDIT, false)) {
			if (this.playerPatch.getSkillCapability() != null) {
				Minecraft.getInstance().setScreen(new SkillEditScreen(this.player, this.playerPatch.getSkillCapability()));
			}
		}
		
		if (isKeyPressed(EpicFightKeyMappings.OPEN_CONFIG_SCREEN, false)) {
			Minecraft.getInstance().setScreen(new IngameConfigurationScreen(null));
		}
		
		if (isKeyPressed(EpicFightKeyMappings.SWITCH_VANILLA_MODEL_DEBUGGING, false)) {
			boolean flag = ClientEngine.getInstance().switchVanillaModelDebuggingMode();
			this.minecraft.keyboardHandler.debugFeedbackTranslated(flag ? "debug.vanilla_model_debugging.on" : "debug.vanilla_model_debugging.off");
		}
		
		while (isKeyPressed(EpicFightKeyMappings.ATTACK, true)) {
			if (this.playerPatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.ATTACK) {
				boolean shouldPlayAttackAnimation = true;
				
				if (this.options.keyAttack.getKey() == EpicFightKeyMappings.ATTACK.getKey() && minecraft.hitResult != null) {
					if (this.minecraft.hitResult.getType() == HitResult.Type.ENTITY) {
						Entity hitEntity = ((EntityHitResult)this.minecraft.hitResult).getEntity();
						
						if (!(hitEntity instanceof LivingEntity) && !(hitEntity instanceof PartEntity)) {
							shouldPlayAttackAnimation = false;
						}
					}
					
					if (shouldPlayAttackAnimation) {
						if (this.playerPatch.isTargetLockedOn()) {
							shouldPlayAttackAnimation = true;
						} else if (ClientConfig.combatPreferredItems.contains(this.player.getMainHandItem().getItem())) {
							if (this.minecraft.hitResult.getType() == HitResult.Type.BLOCK && minecraft.level != null) {
								BlockPos bp = ((BlockHitResult) this.minecraft.hitResult).getBlockPos();
								BlockState bs = this.minecraft.level.getBlockState(bp);
								shouldPlayAttackAnimation = !this.player.getMainHandItem().getItem().canAttackBlock(bs, this.player.level(), bp, this.player) || !this.player.getMainHandItem().isCorrectToolForDrops(bs);
							}
						} else {
							shouldPlayAttackAnimation = this.minecraft.hitResult.getType() != HitResult.Type.BLOCK;
						}
					}
					
					// Disable vanilla attack key
					if (shouldPlayAttackAnimation) {
						makeUnpressed(this.options.keyAttack);
					}
				}
				
				if (shouldPlayAttackAnimation) {
					if (!EpicFightKeyMappings.ATTACK.getKey().equals(EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey())) {
						SkillSlot slot = (!this.player.onGround() && !this.player.isInWater() && this.player.getDeltaMovement().y > 0.03D) ? SkillSlots.AIR_ATTACK : SkillSlots.BASIC_ATTACK;
						
						if (this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this).isExecutable()) {
							this.player.resetAttackStrengthTicker();
							this.attackLightPressToggle = false;
							this.releaseAllServedKeys();
						} else {
							if (!this.player.isSpectator() && slot == SkillSlots.BASIC_ATTACK) {
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
			if (this.playerPatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.DODGE) {
				if (EpicFightKeyMappings.DODGE.getKey().getValue() == this.options.keyShift.getKey().getValue()) {
					if (this.player.getVehicle() == null) {
						if (!this.sneakPressToggle) {
							this.sneakPressToggle = true;
						}
					}
				} else {
					SkillSlot skillCategory = (this.playerPatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
					SkillContainer skill = this.playerPatch.getSkill(skillCategory);
					
					if (skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
						this.reserveKey(SkillSlots.DODGE, EpicFightKeyMappings.DODGE);
					}
				}
			}
		}
		
		if (isKeyDown(EpicFightKeyMappings.GUARD)) {
			if (this.playerPatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.GUARD) {
				if (!this.playerPatch.isHoldingAny()) {
					boolean hasUseAction = false;
					
					// Support for traditional guard keybind
					if (EpicFightKeyMappings.GUARD.getKey().equals(this.options.keyUse.getKey())) {
						// Check if the item has any use effect and if true, player won't guard
						if (this.player.getMainHandItem().getUseAnimation() != UseAnim.NONE) {
							hasUseAction = true;
						}
					}
					
					if (!hasUseAction) {
						SkillCastEvent skillCastEvent = this.playerPatch.getSkill(SkillSlots.GUARD).sendCastRequest(this.playerPatch, this);
						
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
		}
		
		while (isKeyPressed(EpicFightKeyMappings.WEAPON_INNATE_SKILL, true)) {
			if (this.playerPatch.isEpicFightMode() && this.currentHoldingKey != EpicFightKeyMappings.WEAPON_INNATE_SKILL) {
				if (!EpicFightKeyMappings.ATTACK.getKey().equals(EpicFightKeyMappings.WEAPON_INNATE_SKILL.getKey())) {
					if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
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
			if (this.playerPatch.isEpicFightMode() && !this.playerPatch.isChargingAny()) {
				if (EpicFightKeyMappings.MOVER_SKILL.getKey().getValue() == this.options.keyJump.getKey().getValue()) {
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
					SkillContainer skill = this.playerPatch.getSkill(SkillSlots.MOVER);
					skill.sendCastRequest(this.playerPatch, this);
				}
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.SWITCH_MODE, false)) {
			if (EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(this.playerPatch.getOriginal().level())) {
				this.playerPatch.toggleMode();
			} else {
				this.minecraft.gui.getChat().addMessage(Component.translatable("epicfight.messages.mode_switching_disabled").withStyle(ChatFormatting.RED));
			}
		}
		
		while (isKeyPressed(EpicFightKeyMappings.LOCK_ON, false)) {
			this.playerPatch.toggleLockOn();
		}
		
		// Disable swap hand items
		if (this.playerPatch.getEntityState().inaction() || (!this.playerPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).canBePlacedOffhand())) {
			makeUnpressed(this.minecraft.options.keySwapOffhand);
		}
		
		// Pause here if player is not in battle mode
		if (!this.playerPatch.isEpicFightMode() || Minecraft.getInstance().isPaused()) {
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
						if (this.playerPatch.getSkill(SkillSlots.WEAPON_INNATE).sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
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
			SkillSlot slot = (!this.player.onGround() && !this.player.isInWater() && this.player.getDeltaMovement().y > 0.03D) ? SkillSlots.AIR_ATTACK : SkillSlots.BASIC_ATTACK;
			
			if (this.playerPatch.getSkill(slot).sendCastRequest(this.playerPatch, this).isExecutable()) {
				this.player.resetAttackStrengthTicker();
				this.releaseAllServedKeys();
			} else {
				if (!this.player.isSpectator() && slot == SkillSlots.BASIC_ATTACK) {
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
				SkillSlot skillSlot = (this.playerPatch.getEntityState().knockDown()) ? SkillSlots.KNOCKDOWN_WAKEUP : SkillSlots.DODGE;
				SkillContainer skill = this.playerPatch.getSkill(skillSlot);
				
				if (skill.sendCastRequest(this.playerPatch, this).shouldReserveKey()) {
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
			SkillContainer container = this.playerPatch.getSkill(this.reservedOrChargingSkillSlot);
			
			if (!container.isEmpty()) {
				if (container.getSkill() instanceof ChargeableSkill chargingSkill) {
					if (!isKeyDown(this.currentHoldingKey)) {
						this.holdingFinished = true;
					}
					
					if (this.holdingFinished) {
						if (this.playerPatch.getSkillChargingTicks() > chargingSkill.getMinChargingTicks()) {
							container.sendCastRequest(this.playerPatch, this);
							this.releaseAllServedKeys();
						}
					} else if (this.playerPatch.getSkillChargingTicks() >= chargingSkill.getAllowedMaxChargingTicks()) {
						this.releaseAllServedKeys();
					}
				} else if (container.getSkill() instanceof HoldableSkill) {
					if (!isKeyDown(this.currentHoldingKey)) {
						this.holdingFinished = true;
					}
					
					if (this.holdingFinished) {
						// Note: Holdable skills are canceled in client first
						this.playerPatch.resetHolding();
						container.getSkill().cancelOnClient(container, container.getSkill().gatherArguments(container, this));
						container.sendCancelRequest(this.playerPatch, this);
						this.releaseAllServedKeys();
					}
				} else {
					this.releaseAllServedKeys();
				}
			}
		}
		
		if (this.reservedKey != null) {
			if (this.reserveCounter > 0) {
				SkillContainer skill = this.playerPatch.getSkill(this.reservedOrChargingSkillSlot);
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

		if (!this.playerPatch.getEntityState().canSwitchHoldingItem() || this.hotbarLocked) {
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
		
		if (this.player.isAlive()) {
			this.playerPatch.getEventListener().triggerEvents(EventType.MOVEMENT_INPUT_EVENT, new MovementInputEvent(this.playerPatch, input));
		}
	}
	
	private void reserveKey(SkillSlot slot, KeyMapping keyMapping) {
		this.reservedKey = keyMapping;
		this.reservedOrChargingSkillSlot = slot;
		this.reserveCounter = 8;
	}
	
	public void releaseAllServedKeys() {
		this.holdingFinished = true;
		this.currentHoldingKey = null;
		this.reservedOrChargingSkillSlot = null;
		this.reserveCounter = -1;
		this.reservedKey = null;
	}
	
	public void setHoldingKey(SkillSlot chargingSkillSlot, KeyMapping keyMapping) {
		this.holdingFinished = false;
		this.currentHoldingKey = keyMapping;
		this.reservedOrChargingSkillSlot = chargingSkillSlot;
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
	
	public void addPacketToSend(Object packet) {
		this.packets.add(packet);
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
			InputEvent.InteractionKeyMappingTriggered inputEvent = net.minecraftforge.client.ForgeHooksClient.onClickInput(mouseButton, key, InteractionHand.MAIN_HAND);
			
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
	
	@OnlyIn(Dist.CLIENT)
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
	public static class Events {
		static ControlEngine controlEngine;
		
		@SubscribeEvent
		public static void mouseScrollEvent(InputEvent.MouseScrollingEvent event) {
			// Disable item switching
			if (controlEngine.minecraft.player != null && controlEngine.playerPatch != null && !controlEngine.playerPatch.getEntityState().canSwitchHoldingItem() && controlEngine.minecraft.screen == null) {
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
			if (controlEngine.minecraft.player == null) {
				return;
			}
			
			if (
				event.getKeyMapping() == controlEngine.minecraft.options.keyAttack &&
				EpicFightKeyMappings.ATTACK.getKey() == controlEngine.minecraft.options.keyAttack.getKey() &&
				controlEngine.minecraft.hitResult.getType() == HitResult.Type.BLOCK &&
				ClientConfig.combatPreferredItems.contains(controlEngine.player.getMainHandItem().getItem())
			) {
				BlockPos bp = ((BlockHitResult) controlEngine.minecraft.hitResult).getBlockPos();
				BlockState bs = controlEngine.minecraft.level.getBlockState(bp);
				
				if (!controlEngine.player.getMainHandItem().getItem().canAttackBlock(bs, controlEngine.player.level(), bp, controlEngine.player) || controlEngine.player.getMainHandItem().getDestroySpeed(bs) <= 1.0F) {
					event.setSwingHand(false);
					event.setCanceled(true);
				}
			}
		}
	}
}