package yesman.epicfight.world.capabilities.entitypatch.player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.forgeevent.BattleModeSustainableEvent;
import yesman.epicfight.api.forgeevent.ChangePlayerModeEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.ChargeableSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.AttackSpeedModifyEvent;
import yesman.epicfight.world.entity.eventlistener.FallEvent;
import yesman.epicfight.world.entity.eventlistener.ModifyBaseDamageEvent;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;
import yesman.epicfight.world.entity.eventlistener.SkillConsumeEvent;
import yesman.epicfight.world.gamerule.EpicFightGamerules;

public abstract class PlayerPatch<T extends Player> extends LivingEntityPatch<T> {
	public static EntityDataAccessor<Float> STAMINA;
	
	public static void initPlayerDataAccessor() {
		STAMINA = SynchedEntityData.defineId(Player.class, EntityDataSerializers.FLOAT);
	}
	
	public static void createSyncedEntityData(LivingEntity livingentity) {
		livingentity.getEntityData().define(STAMINA, Float.valueOf(0.0F));
	}
	
	protected static final UUID PLAYER_EVENT_UUID = UUID.fromString("e6beeac4-77d2-11eb-9439-0242ac130002");
	protected static final float PLAYER_SCALE = 0.9375F;
	protected PlayerEventListener eventListeners;
	protected PlayerMode playerMode = PlayerMode.MINING;
	protected boolean battleModeRestricted;
	
	protected float modelYRotO;
	protected float modelYRot;
	protected boolean useModelYRot;
	protected int tickSinceLastAction;
	protected int lastChargingTick;
	protected int chargingAmount;
	protected ChargeableSkill chargingSkill;
	
	// Manage the previous position here because playerpatch#tick called before entity#travel method.
	protected double xo;
	protected double yo;
	protected double zo;
	
	// Manage the player's horizontal delta movement here instead of directly modifying entity#xxa, entity#zza (it causes potential issues in terms of mod compatibility)
	public double dx;
	public double dz;
	
	public PlayerPatch() {
		this.eventListeners = new PlayerEventListener(this);
	}
	
	@Override
	public void onJoinWorld(T entity, EntityJoinLevelEvent event) {
		super.onJoinWorld(entity, event);
		
		CapabilitySkill skillCapability = this.getSkillCapability();
		skillCapability.skillContainers[SkillSlots.BASIC_ATTACK.universalOrdinal()].setSkill(EpicFightSkills.BASIC_ATTACK);
		skillCapability.skillContainers[SkillSlots.AIR_ATTACK.universalOrdinal()].setSkill(EpicFightSkills.AIR_ATTACK);
		skillCapability.skillContainers[SkillSlots.KNOCKDOWN_WAKEUP.universalOrdinal()].setSkill(EpicFightSkills.KNOCKDOWN_WAKEUP);
		
		this.tickSinceLastAction = 0;
		
		this.eventListeners.addEventListener(EventType.ACTION_EVENT_SERVER, PLAYER_EVENT_UUID, (playerEvent) -> {
			this.resetActionTick();
		});
	}
	
	@Override
	public void initAnimator(Animator animator) {
		/* Living Animations */
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
		animator.addLivingAnimation(LivingMotions.RUN, Animations.BIPED_RUN);
		animator.addLivingAnimation(LivingMotions.SNEAK, Animations.BIPED_SNEAK);
		animator.addLivingAnimation(LivingMotions.SWIM, Animations.BIPED_SWIM);
		animator.addLivingAnimation(LivingMotions.FLOAT, Animations.BIPED_FLOAT);
		animator.addLivingAnimation(LivingMotions.KNEEL, Animations.BIPED_KNEEL);
		animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
		animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
		animator.addLivingAnimation(LivingMotions.SIT, Animations.BIPED_SIT);
		animator.addLivingAnimation(LivingMotions.FLY, Animations.BIPED_FLYING);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
		animator.addLivingAnimation(LivingMotions.JUMP, Animations.BIPED_JUMP);
		animator.addLivingAnimation(LivingMotions.CLIMB, Animations.BIPED_CLIMBING);
		animator.addLivingAnimation(LivingMotions.SLEEP, Animations.BIPED_SLEEPING);
		animator.addLivingAnimation(LivingMotions.CREATIVE_FLY, Animations.BIPED_CREATIVE_FLYING);
		animator.addLivingAnimation(LivingMotions.CREATIVE_IDLE, Animations.BIPED_CREATIVE_IDLE);

		/* Mix Animations */
		animator.addLivingAnimation(LivingMotions.DIGGING, Animations.BIPED_DIG);
		animator.addLivingAnimation(LivingMotions.AIM, Animations.BIPED_BOW_AIM);
		animator.addLivingAnimation(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT);
		animator.addLivingAnimation(LivingMotions.DRINK, Animations.BIPED_DRINK);
		animator.addLivingAnimation(LivingMotions.EAT, Animations.BIPED_EAT);
		animator.addLivingAnimation(LivingMotions.SPECTATE, Animations.BIPED_SPYGLASS_USE);
	}
	
	public void copySkillsFrom(PlayerPatch<?> old) {
		CapabilitySkill oldSkill = old.getSkillCapability();
		CapabilitySkill newSkill = this.getSkillCapability();
		int i = 0;
		
		for (SkillContainer container : newSkill.skillContainers) {
			container.setExecuter(this);
			Skill oldone = oldSkill.skillContainers[i].getSkill();
			
			if (oldone != null && oldone.getCategory().learnable()) {
				container.setSkill(oldSkill.skillContainers[i].getSkill());
			}
			
			i++;
		}
		
		for (SkillCategory skillCategory : SkillCategory.ENUM_MANAGER.universalValues()) {
			if (oldSkill.hasCategory(skillCategory)) {
				for (Skill learnedSkill : oldSkill.getLearnedSkills(skillCategory)) {
					newSkill.addLearnedSkill(learnedSkill);
				}
			}
		}
	}
	
	public void setModelYRot(float rotDeg, boolean sendPacket) {
		this.useModelYRot = true;
		this.modelYRot = rotDeg;
	}
	
	public void disableModelYRot(boolean sendPacket) {
		this.useModelYRot = false;
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		float oYRot;
		float yRot;
		float scale = (this.original.isBaby() ? 0.5F : 1.0F) * PLAYER_SCALE;
		
		if (this.original.getVehicle() instanceof LivingEntity ridingEntity) {
			oYRot = ridingEntity.yBodyRotO;
			yRot = ridingEntity.yBodyRot;
		} else {
			oYRot = this.modelYRotO;
			yRot = this.modelYRot;
		}
		
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, oYRot, yRot, partialTicks, scale, scale, scale);
	}
	
	@Override
	public void serverTick(LivingEvent.LivingTickEvent event) {
		super.serverTick(event);
		
		if (this.state.canBasicAttack()) {
			this.tickSinceLastAction++;
		}
		
		float stamina = this.getStamina();
		float maxStamina = this.getMaxStamina();
		float staminaRegen = (float)this.original.getAttributeValue(EpicFightAttributes.STAMINA_REGEN.get());
		int regenStandbyTime = 900 / (int)(30 * staminaRegen);
		
		if (stamina < maxStamina && this.tickSinceLastAction > regenStandbyTime) {
			float staminaFactor = 1.0F + (float)Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
			this.setStamina(stamina + maxStamina * 0.01F * staminaFactor * staminaRegen);
		}
		
		if (maxStamina < stamina) {
			this.setStamina(maxStamina);
		}
		
		this.xo = this.original.getX();
		this.yo = this.original.getY();
		this.zo = this.original.getZ();
	}
	
	@Override
	public void tick(LivingEvent.LivingTickEvent event) {
		if (this.playerMode == PlayerMode.BATTLE || this.battleModeRestricted) {
			BattleModeSustainableEvent battleModeSustainableEvent = new BattleModeSustainableEvent(this);
			MinecraftForge.EVENT_BUS.post(battleModeSustainableEvent);
			
			if (battleModeSustainableEvent.isCanceled()) {
				if (this.playerMode == PlayerMode.BATTLE) {
					this.toMiningMode(false);
					this.battleModeRestricted = true;
				}
			} else {
				if (this.battleModeRestricted) {
					this.battleModeRestricted = false;
					this.toBattleMode(false);
				}
			}
		}
		
		if (this.original.getVehicle() == null) {
			for (SkillContainer container : this.getSkillCapability().skillContainers) {
				if (container != null) {
					container.update();
				}
			}
		}
		
		super.tick(event);
		
		this.modelYRotO = this.modelYRot;
		
		if (this.getEntityState().turningLocked()) {
			if (!this.useModelYRot) {
				this.setModelYRot(this.original.getYRot(), false);
			}
		} else {
			if (this.useModelYRot) {
				this.disableModelYRot(false);
			}
		}
		
		if (this.getEntityState().inaction() && this.original.getControlledVehicle() == null) {
			this.original.yBodyRot = this.original.getYRot();
			this.original.yHeadRot = this.original.getYRot();
		}
		
		if (!this.useModelYRot) {
			float originalYRot = this.isLogicalClient() ? this.original.yBodyRot : this.original.getYRot();
			this.modelYRot += Mth.clamp(Mth.wrapDegrees(originalYRot - this.modelYRot), -45.0F, 45.0F);
		}
	}
	
	public SkillContainer getSkill(Skill skill) {
		if (skill == null) {
			return null;
		}
		
		return this.getSkillCapability().getSkillContainer(skill);
	}
	
	public SkillContainer getSkill(SkillSlot slot) {
		return this.getSkill(slot.universalOrdinal());
	}
	
	public SkillContainer getSkill(int slotIndex) {
		return this.getSkillCapability().skillContainers[slotIndex];
	}
	
	public CapabilitySkill getSkillCapability() {
		return this.original.getCapability(EpicFightCapabilities.CAPABILITY_SKILL).orElse(CapabilitySkill.EMPTY);
	}
	
	public PlayerEventListener getEventListener() {
		return this.eventListeners;
	}
	
	@Override
	public float getModifiedBaseDamage(float baseDamage) {
		ModifyBaseDamageEvent<PlayerPatch<?>> event = new ModifyBaseDamageEvent<>(this, baseDamage);
		this.getEventListener().triggerEvents(EventType.MODIFY_DAMAGE_EVENT, event);
		
		return event.getDamage();
	}
	
	public float getAttackSpeed(InteractionHand hand) {
		float baseSpeed;
		
		if (hand == InteractionHand.MAIN_HAND) {
			baseSpeed = (float)this.original.getAttributeValue(Attributes.ATTACK_SPEED);
		} else {
			baseSpeed = (float)(this.isOffhandItemValid() ? 
					this.original.getAttributeValue(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get()) : this.original.getAttributeBaseValue(Attributes.ATTACK_SPEED));
		}
		
		return this.getModifiedAttackSpeed(this.getAdvancedHoldingItemCapability(hand), baseSpeed);
	}
	
	public float getModifiedAttackSpeed(CapabilityItem itemCapability, float baseSpeed) {
		AttackSpeedModifyEvent event = new AttackSpeedModifyEvent(this, itemCapability, baseSpeed);
		this.eventListeners.triggerEvents(EventType.MODIFY_ATTACK_SPEED_EVENT, event);
		float weight = this.getWeight();
		
		if (weight > 40.0F) {
			float attenuation = Mth.clamp(this.getOriginal().level().getGameRules().getInt(EpicFightGamerules.WEIGHT_PENALTY), 0, 100) / 100.0F;
			return event.getAttackSpeed() + (-0.1F * (weight / 40.0F) * Math.max(event.getAttackSpeed() - 0.8F, 0.0F) * attenuation);
		} else {
			return event.getAttackSpeed();
		}
	}
	
	public double getWeaponAttribute(Attribute attribute, ItemStack itemstack) {
		AttributeInstance attrInstance = new AttributeInstance(attribute, (ai)->{});
		
		Set<AttributeModifier> itemModifiers = Set.copyOf(CapabilityItem.getAttributeModifiers(attribute, EquipmentSlot.MAINHAND, itemstack, this));
		Set<AttributeModifier> mainhandModifiers = Set.copyOf(CapabilityItem.getAttributeModifiers(attribute, EquipmentSlot.MAINHAND, this.original.getMainHandItem(), this));
		
		double baseValue = this.original.getAttribute(attribute) == null ? attribute.getDefaultValue() : this.original.getAttribute(attribute).getBaseValue();
		attrInstance.setBaseValue(baseValue);
		
		for (AttributeModifier modifier : this.original.getAttribute(attribute).getModifiers()) {
			if (!itemModifiers.contains(modifier) && !mainhandModifiers.contains(modifier)) {
				attrInstance.addTransientModifier(modifier);
			}
		}
		
		for (AttributeModifier modifier : itemModifiers) {
			if (!attrInstance.hasModifier(modifier)) {
				attrInstance.addTransientModifier(modifier);
			}
		}
		
		CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapabilityOr(itemstack, null);
		
		if (itemCapability != null) {
			for (AttributeModifier modifier : itemCapability.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(attribute)) {
				if (!attrInstance.hasModifier(modifier)) {
					attrInstance.addTransientModifier(modifier);
				}
			}
		}
		
		return attrInstance.getValue();
	}
	
	@Override
	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
		float fallDist = this.original.fallDistance;
		boolean onGround = this.original.onGround;
		boolean offhandValid = this.isOffhandItemValid();
		
		ItemStack mainHandItem = this.getOriginal().getMainHandItem();
		ItemStack offHandItem = this.getOriginal().getOffhandItem();
		Collection<AttributeModifier> mainHandAttributes = CapabilityItem.getAttributeModifiers(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getMainHandItem(), this);
		Collection<AttributeModifier> offHandAttributes = this.isOffhandItemValid() ? CapabilityItem.getAttributeModifiers(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getOffhandItem(), this) : Set.of();
		
		// Prevents crit and sweeping edge effect
		this.epicFightDamageSource = damageSource;
		this.original.attackStrengthTicker = Integer.MAX_VALUE;
		this.original.fallDistance = 0.0F;
		this.original.onGround = false;
		this.setOffhandDamage(hand, mainHandItem, offHandItem, offhandValid, mainHandAttributes, offHandAttributes);
		this.original.attack(target);
		this.recoverMainhandDamage(hand, mainHandItem, offHandItem, mainHandAttributes, offHandAttributes);
		this.epicFightDamageSource = null;
		this.original.fallDistance = fallDist;
		this.original.onGround = onGround;
		
		return super.attack(damageSource, target, hand);
	}
	
	@Override
	public EpicFightDamageSource getDamageSource(StaticAnimation animation, InteractionHand hand) {
		EpicFightDamageSources damageSources = EpicFightDamageSources.of(this.original.level());
		EpicFightDamageSource damagesource = damageSources.playerAttack(this.original).setAnimation(animation);
		damagesource.setImpact(this.getImpact(hand));
		damagesource.setArmorNegation(this.getArmorNegation(hand));
		damagesource.setHurtItem(this.getOriginal().getItemInHand(hand));
		damagesource.setBasicAttack(this.getSkill(SkillSlots.BASIC_ATTACK).getDataManager().getDataValue(SkillDataKeys.BASIC_ATTACK_ACTIVATE.get()));
		
		return damagesource;
	}
	
	@Override
	public void cancelAnyAction() {
		super.cancelAnyAction();
		this.resetSkillCharging();
	}
	
	public float getMaxStamina() {
		AttributeInstance maxStamina = this.original.getAttribute(EpicFightAttributes.MAX_STAMINA.get());
		return (float)(maxStamina == null ? 0 : maxStamina.getValue());
	}
	
	public float getStamina() {
		return this.getMaxStamina() <= 0.0F ? 0.0F : this.original.getEntityData().hasItem(STAMINA) ? this.original.getEntityData().get(STAMINA).floatValue() : 0.0F;
	}
	
	public float getModifiedStaminaConsume(float amount) {
		float attenuation = Mth.clamp(this.original.level().getGameRules().getInt(EpicFightGamerules.WEIGHT_PENALTY), 0, 100) / 100.0F;
		float weight = this.getWeight();

		return ((weight / 40.0F - 1.0F) * attenuation + 1.0F) * amount;
	}
	
	public boolean hasStamina(float amount) {
		return this.getStamina() > amount;
	}
	
	public void setStamina(float value) {
		if (this.original.getEntityData().hasItem(STAMINA)) {
			float f1 = Mth.clamp(value, 0.0F, this.getMaxStamina());
			this.original.getEntityData().set(STAMINA, f1);
		}
	}
	
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource) {
		return this.consumeForSkill(skill, consumeResource, skill.getDefaultConsumptionAmount(this));
	}
	
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource, float amount) {
		return this.consumeForSkill(skill, consumeResource, amount, false);
	}
	
	/**
	 * Client : Checks if a player has enough resource
	 * Server : Checks and consumes the resource if it meets the condition
	 * @param amount
	 * @return check result
	 * Use this 
	 */
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource, float amount, boolean activateConsumeForce) {
		SkillConsumeEvent skillConsumeEvent = new SkillConsumeEvent(this, skill, consumeResource, amount);
		this.getEventListener().triggerEvents(EventType.SKILL_CONSUME_EVENT, skillConsumeEvent);
		
		if (skillConsumeEvent.isCanceled()) {
			return false;
		}
		
		if (skillConsumeEvent.getResourceType().predicate.canExecute(skill, this, amount)) {
			if (!this.isLogicalClient()) {
				skillConsumeEvent.getResourceType().consumer.consume(skill, (ServerPlayerPatch)this, amount);
			}
			
			return true;
		} else if (activateConsumeForce) {
			if (!this.isLogicalClient()) {
				skillConsumeEvent.getResourceType().consumer.consume(skill, (ServerPlayerPatch)this, amount);
			}
		}
		
		return false;
	}
	
	public void resetActionTick() {
		this.tickSinceLastAction = 0;
	}
	
	public int getTickSinceLastAction() {
		return this.tickSinceLastAction;
	}
	
	public void startSkillCharging(ChargeableSkill chargingSkill) {
		chargingSkill.startCharging(this);
		this.lastChargingTick = this.original.tickCount;
		this.chargingSkill = chargingSkill;
	}

	public void resetSkillCharging() {
		if (this.chargingSkill != null) {
			this.chargingAmount = 0;
			this.chargingSkill.resetCharging(this);
			this.chargingSkill = null;
		}
	}

	public boolean isChargingSkill() {
		return this.chargingSkill != null;
	}
	
	public boolean isChargingSkill(Skill chargingSkill) {
		return this.chargingSkill == chargingSkill;
	}
	
	public int getLastChargingTick() {
		return this.lastChargingTick;
	}
	
	public void setChargingAmount(int amount) {
		if (this.isChargingSkill()) {
			this.chargingAmount = Math.min(amount, this.getChargingSkill().getMaxChargingTicks());
		} else {
			this.chargingAmount = 0;
		}
	}
	
	public int getChargingAmount() {
		return this.chargingAmount;
	}
	
	public float getSkillChargingTicks(float partialTicks) {
		return this.isChargingSkill() ? (this.original.tickCount - this.getLastChargingTick() - 1.0F) + partialTicks : 0;
	}
	
	public int getSkillChargingTicks() {
		return this.isChargingSkill() ? Math.min(this.original.tickCount - this.getLastChargingTick(), this.chargingSkill.getMaxChargingTicks()) : 0;
	}
	
	public int getAccumulatedChargeAmount() {
		return this.getChargingSkill() != null ? this.getChargingSkill().getChargingAmount(this) : 0;
	}

	public ChargeableSkill getChargingSkill() {
		return this.chargingSkill;
	}
	
	public boolean isInAir() {
		return this.original.isFallFlying() || this.currentLivingMotion == LivingMotions.FALL;
	}
	
	@Override
	public boolean shouldMoveOnCurrentSide(ActionAnimation actionAnimation) {
		return this.isLogicalClient();
	}
	
	public void openSkillBook(ItemStack itemstack, InteractionHand hand) {
	}
	
	@Override
	public void onFall(LivingFallEvent event) {
		FallEvent fallEvent = new FallEvent(this, event);
		this.getEventListener().triggerEvents(EventType.FALL_EVENT, fallEvent);
		super.onFall(event);
		
		this.setAirborneState(false);
	}
	
	public void toggleMode() {
		switch (this.playerMode) {
		case MINING:
			this.toBattleMode(true);
			break;
		case BATTLE:
			this.toMiningMode(true);
			break;
		}
	}
	
	public void toMode(PlayerMode playerMode, boolean synchronize) {
		switch (playerMode) {
		case MINING:
			this.toMiningMode(synchronize);
			break;
		case BATTLE:
			this.toBattleMode(synchronize);
			break;
		}
	}
	
	public PlayerMode getPlayerMode() {
		return this.playerMode;
	}
	
	public void toMiningMode(boolean synchronize) {
		if (this.playerMode == PlayerMode.MINING) {
			return;
		}
		
		if (this.battleModeRestricted) {
			this.battleModeRestricted = false;
		}
		
		ChangePlayerModeEvent prepareModelEvent = new ChangePlayerModeEvent(this, PlayerMode.MINING);
		
		if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
			this.playerMode = prepareModelEvent.getPlayerMode();
		}
	}
	
	public void toBattleMode(boolean synchronize) {
		if (this.playerMode == PlayerMode.BATTLE) {
			return;
		}
		
		ChangePlayerModeEvent prepareModelEvent = new ChangePlayerModeEvent(this, PlayerMode.BATTLE);
		
		if (!MinecraftForge.EVENT_BUS.post(prepareModelEvent)) {
			this.playerMode = prepareModelEvent.getPlayerMode();
		}
	}
	
	public boolean isBattleMode() {
		return this.playerMode == PlayerMode.BATTLE;
	}
	
	@Override
	public double getXOld() {
		return this.xo;
	}
	
	@Override
	public double getYOld() {
		return this.yo;
	}
	
	@Override
	public double getZOld() {
		return this.zo;
	}
	
	@Override
	public float getYRot() {
		return this.modelYRot;
	}
	
	@Override
	public void setYRot(float yRot) {
		if (this.useModelYRot) {
			this.setModelYRot(yRot, true);
		} else {
			this.original.setYRot(yRot);
		}
	}
	
	@Override
	public float getYRotLimit() {
		return 180.0F;
	}
	
	@Override
	public StaticAnimation getHitAnimation(StunType stunType) {
		if (this.original.getVehicle() != null) {
			return Animations.BIPED_HIT_ON_MOUNT;
		} else {
			return switch (stunType) {
				case LONG -> Animations.BIPED_HIT_LONG;
				case SHORT, HOLD -> Animations.BIPED_HIT_SHORT;
				case KNOCKDOWN -> Animations.BIPED_KNOCKDOWN;
				case NEUTRALIZE -> Animations.BIPED_COMMON_NEUTRALIZED;
				case FALL -> Animations.BIPED_LANDING;
				case NONE -> null;
				default -> null;
			};
		}
	}
	
	public enum PlayerMode {
		MINING, BATTLE
	}
}