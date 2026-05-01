package yesman.epicfight.world.capabilities.entitypatch.player;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.api.event.types.player.SkillConsumeEvent;
import yesman.epicfight.api.event.types.player.TickPlayerEpicFightModeEvent;
import yesman.epicfight.api.event.types.player.TogglePlayerModeEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightExpandedEntityDataAccessors;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.common.ComboAttacks;
import yesman.epicfight.skill.modules.ChargeableSkill;
import yesman.epicfight.skill.modules.HoldableSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.emote.PlayerEmoteSlots;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.damagesource.EpicFightDamageSources;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.data.ExpandedSyncedData;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class PlayerPatch<T extends Player> extends LivingEntityPatch<T> {
	protected static final float PLAYER_SCALE = 0.9375F;
	
	protected final PlayerSkills playerSkills = new PlayerSkills(this);
    protected final PlayerEmoteSlots emoteSlots = new PlayerEmoteSlots();
	protected PlayerMode playerMode = PlayerMode.EPICFIGHT;
	protected boolean battleModeRestricted;
	
	protected float modelYRotO;
	protected float modelYRot;
	protected boolean useModelYRot;
	protected int tickSinceLastAction;
	protected int staminaRegenAwaitTicks;
	protected int lastChargingTick;
	protected int chargingTicks;
	protected HoldableSkill holdingSkill;
	
	// Manage the previous position here because playerpatch#tick called before entity#travel method.
	protected double xo;
	protected double yo;
	protected double zo;
	
	// Manage the player's horizontal delta movement here instead of directly modifying entity#xxa, entity#zza (it causes potential issues in terms of mod compatibility)
	public double dx;
	public double dz;
	
	public PlayerPatch(T entity) {
		super(entity);

        // Register permanent events
        this.getEventListener().registerEvent(
            EpicFightEventHooks.Animation.START_ACTION,
            event -> {
                this.resetActionTick();
            },
            IdentifierProvider.permanent()
        );
	}
	
	@Override
	public void onJoinWorld(T entity, Level level, boolean worldgenSpawn) {
		super.onJoinWorld(entity, level, worldgenSpawn);
		
		PlayerSkills skillCapability = this.getPlayerSkills();
		skillCapability.skillContainers[SkillSlots.COMBO_ATTACKS.universalOrdinal()].setSkill(EpicFightSkills.COMBO_ATTACKS.get());
		skillCapability.skillContainers[SkillSlots.KNOCKDOWN_WAKEUP.universalOrdinal()].setSkill(EpicFightSkills.KNOCKDOWN_WAKEUP.get());
		this.tickSinceLastAction = 0;
		this.staminaRegenAwaitTicks = 30;
	}
	
	@Override
	protected void registerExpandedEntityDataAccessors(final ExpandedSyncedData expandedSynchedData) {
		super.registerExpandedEntityDataAccessors(expandedSynchedData);
		expandedSynchedData.register(EpicFightExpandedEntityDataAccessors.STAMINA);
	}
	
	@Override
	public void initAnimator(Animator animator) {
		super.initAnimator(animator);
		
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
	
	public void copyOldData(PlayerPatch<?> old, boolean isDeath) {
		this.getPlayerSkills().copyFrom(old.getPlayerSkills());
        this.getEmoteSlots().copyFrom(old.getEmoteSlots());
		
		if (!isDeath) {
			old.getPlayerSkills().listSkillContainers().forEach(skillContainer -> {
				skillContainer.transferDataTo(this.getPlayerSkills().getSkillContainerFor(skillContainer.getSlot()));
			});
			
			CompoundTag oldData = new CompoundTag();
			old.expandedSynchedData.saveData(oldData);
			this.expandedSynchedData.load(oldData);
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
	public OpenMatrix4f getModelMatrix(float partialTick) {
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
		
		return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, oYRot, yRot, partialTick, scale, scale, scale);
	}
	
	@Override
	public void preTickServer() {
		super.preTickServer();
		
		if (this.state.canBasicAttack()) {
			this.tickSinceLastAction++;
		}
		
		if (!this.state.inaction()) {
			if (this.staminaRegenAwaitTicks > 0) this.staminaRegenAwaitTicks--;
		}
		
		float stamina = this.getStamina();
		float maxStamina = this.getMaxStamina();
		float staminaRegen = (float)this.original.getAttributeValue(EpicFightAttributes.STAMINA_REGEN);
		
		if (staminaRegen > 0.0F) {
			int regenWhenLessThan = 30 - (900 / (int)(30 * staminaRegen));
			
			if (stamina < maxStamina && this.staminaRegenAwaitTicks <= regenWhenLessThan) {
				float staminaFactor = 1.0F + (float)Math.pow((stamina / (maxStamina - stamina * 0.5F)), 2);
				this.setStamina(stamina + maxStamina * 0.01F * staminaFactor * staminaRegen);
			}
		}
		
		if (maxStamina < stamina) {
			this.setStamina(maxStamina);
		}
	}
	
	@Override
	public void preTick() {
		if (this.playerMode == PlayerMode.EPICFIGHT || this.battleModeRestricted) {
            TickPlayerEpicFightModeEvent tickEpicFightModeEvent = new TickPlayerEpicFightModeEvent(this);
            EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE.postWithListener(tickEpicFightModeEvent, this.getEventListener());

			if (tickEpicFightModeEvent.isCanceled()) {
				if (this.playerMode == PlayerMode.EPICFIGHT) {
					this.toVanillaMode(false);
					this.battleModeRestricted = true;
				}
			} else {
				if (this.battleModeRestricted) {
					this.battleModeRestricted = false;
					this.toEpicFightMode(false);
				}
			}
		}
		
		if (!this.isLogicalClient() || this.original.isLocalPlayer()) {
			this.getPlayerSkills().listSkillContainers().forEach(SkillContainer::update);
		}
		
		this.modelYRotO = this.modelYRot;
		
		super.preTick();
		
		// Cancel using item depending on player state
		if (!this.state.canUseItem()) {
			this.cancelItemUse();
		}
		
		// When turning is locked, stop synching the entity patch's y rotation to the original entity
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
		
		this.xo = this.original.getX();
		this.yo = this.original.getY();
		this.zo = this.original.getZ();
	}
	
	/**
	 * Use {@link PlayerPatch#getSkillContainerFor} instead to check null
	 **/
	public SkillContainer getSkill(Skill skill) {
		if (skill == null) {
			return null;
		}
		
		return this.getPlayerSkills().getSkillContainer(skill);
	}
	
	public Optional<SkillContainer> getSkillContainerFor(Skill skill) {
		if (skill == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(this.getPlayerSkills().getSkillContainer(skill));
	}
	
	public SkillContainer getSkill(SkillSlot slot) {
		return this.getSkill(slot.universalOrdinal());
	}
	
	public SkillContainer getSkill(int slotIndex) {
		return this.getPlayerSkills().getSkillContainerFor(slotIndex);
	}
	
	public PlayerSkills getPlayerSkills() {
		return this.playerSkills;
	}

    public PlayerEmoteSlots getEmoteSlots() {
        return this.emoteSlots;
    }

	@Override
	public void writeData(CompoundTag compound) {
		super.writeData(compound);
		this.playerSkills.write(compound);
        this.emoteSlots.serialize(compound);
	}
	
	@Override
	public void readData(CompoundTag compound) {
		super.readData(compound);
		this.playerSkills.read(compound);
        this.emoteSlots.deserialize(compound, this.getLevel().registryAccess());
	}

	public double getWeaponAttribute(Holder<Attribute> attribute, ItemStack itemstack) {
		AttributeInstance attrInstance = new AttributeInstance(attribute, attrInstance$2 -> {});
		
		Set<AttributeModifier> itemModifiers = Set.copyOf(CapabilityItem.getAttributeModifiersAsWeapon(attribute, EquipmentSlot.MAINHAND, itemstack, this));
		Set<AttributeModifier> mainhandModifiers = Set.copyOf(CapabilityItem.getAttributeModifiersAsWeapon(attribute, EquipmentSlot.MAINHAND, this.original.getMainHandItem(), this));
		
		double baseValue = this.original.getAttribute(attribute) == null ? attribute.value().getDefaultValue() : Objects.requireNonNull(this.original.getAttribute(attribute)).getBaseValue();
		attrInstance.setBaseValue(baseValue);
		
		for (AttributeModifier modifier : Objects.requireNonNull(this.original.getAttribute(attribute)).getModifiers()) {
			if (!itemModifiers.contains(modifier) && !mainhandModifiers.contains(modifier)) {
				attrInstance.addTransientModifier(modifier);
			}
		}
		
		for (AttributeModifier modifier : itemModifiers) {
			if (!attrInstance.hasModifier(modifier.id())) {
				attrInstance.addTransientModifier(modifier);
			}
		}
		
		EpicFightCapabilities.getItemCapability(itemstack).ifPresent(itemCapability -> {
			for (AttributeModifier modifier : itemCapability.getAttributeModifiers(this).get(attribute)) {
				if (!attrInstance.hasModifier(modifier.id())) {
					attrInstance.addTransientModifier(modifier);
				}
			}
		});
		
		return attrInstance.getValue();
	}
	
	@Override
	public AttackResult attack(EpicFightDamageSource damageSource, Entity target, InteractionHand hand) {
		float fallDist = this.original.fallDistance;
		boolean onGround = this.original.onGround();
		boolean offhandValid = this.isOffhandItemValid();
		
		ItemStack mainHandItem = this.getOriginal().getMainHandItem();
		ItemStack offHandItem = this.getOriginal().getOffhandItem();
		Collection<AttributeModifier> mainHandAttributes = CapabilityItem.getAttributeModifiersAsWeapon(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getMainHandItem(), this);
		Collection<AttributeModifier> offHandAttributes = this.isOffhandItemValid() ? CapabilityItem.getAttributeModifiersAsWeapon(Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND, this.original.getOffhandItem(), this) : Set.of();
		
		this.epicFightDamageSource = damageSource;
		// Prevents crit and sweeping edge effect
		this.original.attackStrengthTicker = Integer.MAX_VALUE;
		this.original.fallDistance = 0.0F;
		this.original.setOnGround(false);
		this.setOffhandDamage(hand, mainHandItem, offHandItem, offhandValid, mainHandAttributes, offHandAttributes);
		this.original.attack(target);
		this.recoverMainhandDamage(hand, mainHandItem, offHandItem, mainHandAttributes, offHandAttributes);
		this.epicFightDamageSource = null;
		this.original.fallDistance = fallDist;
		this.original.setOnGround(onGround);
		
		return super.attack(damageSource, target, hand);
	}
	
	@Override
	public EpicFightDamageSource getDamageSource(AnimationAccessor<? extends StaticAnimation> animation, InteractionHand hand) {
		EpicFightDamageSource damagesource =
			EpicFightDamageSources
				.playerAttack(this.original)
				.setAnimation(animation)
				.setBaseArmorNegation(this.getArmorNegation(hand))
				.setBaseImpact(this.getImpact(hand))
				.setUsedItem(this.getOriginal().getItemInHand(hand));
		
		boolean chargeWeapon = animation.get().isComboAttackAnimation() || this.getAnimator().getVariables().get(ComboAttacks.COMBO, animation).orElse(false);
		damagesource.setChargeWeapon(chargeWeapon);
		
		return damagesource;
	}
	
	@Override
	public void cancelItemUse() {
		super.cancelItemUse();
		this.resetHolding();
	}
	
	public float getMaxStamina() {
		AttributeInstance maxStamina = this.original.getAttribute(EpicFightAttributes.MAX_STAMINA);
		return (float)(maxStamina == null ? 0 : maxStamina.getValue());
	}
	
	public float getStamina() {
		return this.getMaxStamina() <= 0.0F ? 0.0F : this.getExpandedSynchedData().get(EpicFightExpandedEntityDataAccessors.STAMINA);
	}
	
	public float getModifiedStaminaConsume(float amount) {
		float attenuation = Mth.clamp(EpicFightGameRules.WEIGHT_PENALTY.getRuleValue(this.getOriginal().level()), 0, 100) / 100.0F;
		float weight = this.getWeight();

		return ((weight / 40.0F - 1.0F) * attenuation + 1.0F) * amount;
	}
	
	public boolean hasStamina(float amount) {
		return this.getStamina() >= amount;
	}
	
	public void setStamina(float value) {
		float f1 = Mth.clamp(value, 0.0F, this.getMaxStamina());
		this.getExpandedSynchedData().set(EpicFightExpandedEntityDataAccessors.STAMINA, f1);
	}

	public void clampMaxAttributes() {
		float currentHealth = this.original.getHealth();
		float maxHealth = this.original.getMaxHealth();
		
		if (currentHealth > maxHealth) {
			this.original.setHealth(maxHealth);
		}
		
		float currentStamina = this.getStamina();
		float maxStamina = this.getMaxStamina();
		
		if (currentStamina > maxStamina) {
			this.setStamina(maxStamina);
		}
	}
	
	/**
	 * Consume resource by default amount
	 */
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource) {
		return this.consumeForSkill(skill, consumeResource, skill.getDefaultConsumptionAmount(this));
	}
	
	/**
	 * Consume resource with custom amount
	 */
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource, float amount) {
		return this.consumeForSkill(skill, consumeResource, amount, false, null);
	}
	
	/**
	 * Consume resource with arguments when requested by a client
	 */
	@ApiStatus.Internal
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource, @Nullable CompoundTag args) {
		return this.consumeForSkill(skill, consumeResource, skill.getDefaultConsumptionAmount(this), false, args);
	}
	
	/**
	 * Client : Checks if a player has enough resource
	 * Server : Checks and consumes the resource if it meets the condition
	 * @param amount how much resource should it consume
	 * @return check result
	 */
	public boolean consumeForSkill(Skill skill, Skill.Resource consumeResource, float amount, boolean activateConsumeForce, @Nullable CompoundTag args) {
		Optional<SkillContainer> oContainer = this.getSkillContainerFor(skill);
		
		if (oContainer.isEmpty()) {
			return false;
		}
		
		SkillContainer skillContainer = oContainer.get();
		SkillConsumeEvent skillConsumeEvent = new SkillConsumeEvent(this, skill, consumeResource, amount, args);
        EpicFightEventHooks.Player.CONSUME_SKILL.postWithListener(skillConsumeEvent, this.getEventListener());

		if (skillConsumeEvent.isCanceled()) {
			return false;
		}
		
		float modifiedAmount = skillConsumeEvent.getAmount();
		
		if (skillConsumeEvent.getResourceType().predicate.canExecute(skillContainer, this, modifiedAmount)) {
			if (!this.isLogicalClient()) {
				skillConsumeEvent.getResourceType().consumer.consume(skillContainer, (ServerPlayerPatch)this, modifiedAmount);
			}
			
			return true;
		} else if (activateConsumeForce) {
			if (!this.isLogicalClient()) {
				skillConsumeEvent.getResourceType().consumer.consume(skillContainer, (ServerPlayerPatch)this, modifiedAmount);
			}
		}
		
		return false;
	}
	
	public void resetActionTick() {
		this.tickSinceLastAction = 0;
		this.staminaRegenAwaitTicks = 30;
	}
	
	public int getTickSinceLastAction() {
		return this.tickSinceLastAction;
	}
	
	public void setStaminaRegenAwaitTicks(int tick) {
		this.staminaRegenAwaitTicks = tick;
	}
	
	public int getStaminaRegenAwaitTicks() {
		return this.staminaRegenAwaitTicks;
	}

	public boolean startSkillHolding(HoldableSkill holdableSkill) {
		Optional<SkillContainer> containerOptional = this.getSkillContainerFor(holdableSkill.asSkill());
		
		if (containerOptional.isEmpty()) {
			return false;
		} else {
			holdableSkill.startHolding(containerOptional.get());
			this.lastChargingTick = this.original.tickCount;
			this.holdingSkill = holdableSkill;
			return true;
		}
	}
	
	public void resetHolding() {
		if (this.holdingSkill != null) {
			if (this.holdingSkill instanceof ChargeableSkill) {
				this.chargingTicks = 0;
			}
			
			this.holdingSkill.resetHolding(this.getSkill(this.holdingSkill.asSkill()));
			this.holdingSkill = null;
		}
	}

	public boolean isHoldingAny() {
		return this.holdingSkill != null;
	}

	public boolean isHoldingSkill(Skill holdingSkill) {
		return this.holdingSkill == holdingSkill;
	}

    /// Returns the last charging start tick
	public int getLastChargingTick() {
		return this.lastChargingTick;
	}
	
	public void setChargingTicks(int amount) {
		if (this.isHoldingAny() && this.getHoldingSkill() instanceof ChargeableSkill chargeableSkill) {
			this.chargingTicks = Math.clamp(amount, 0, chargeableSkill.getMaxChargingTicks());
		} else {
			this.chargingTicks = 0;
		}
	}

    /// Returns a raw charging ticks
	public int getChargingTicks() {
		return this.chargingTicks;
	}

    /// Returns a raw charging ticks with partial tick
	public float getSkillChargingTicks(float partialTick) {
		return this.isHoldingAny() ? (this.original.tickCount - this.getLastChargingTick() - 1.0F) + partialTick : 0;
	}

    /// Returns a charging ticks with holding skill check and clamped by the holding skill's max ticks
	public int getSkillChargingTicks() {
		return this.isHoldingAny() && this.holdingSkill instanceof ChargeableSkill chargingSkill ? Math.min(this.original.tickCount - this.getLastChargingTick(), chargingSkill.getMaxChargingTicks()) : 0;
	}

	public int getAccumulatedChargeTicks() {
		return this.getHoldingSkill() instanceof ChargeableSkill ? this.chargingTicks : 0;
	}
	
	public HoldableSkill getHoldingSkill() {
		return this.holdingSkill;
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

	public void toggleMode() {
		switch (this.playerMode) {
		case VANILLA -> this.toEpicFightMode(true);
		case EPICFIGHT -> this.toVanillaMode(true);
		}
	}
	
	public void toMode(PlayerMode playerMode, boolean synchronize) {
		switch (playerMode) {
		case VANILLA -> this.toVanillaMode(synchronize);
		case EPICFIGHT -> this.toEpicFightMode(synchronize);
		}
	}
	
	public PlayerMode getPlayerMode() {
		return this.playerMode;
	}
	
	public void toVanillaMode(boolean synchronize) {
		if (this.playerMode == PlayerMode.VANILLA) {
			return;
		}
		
		if (this.battleModeRestricted) {
			this.battleModeRestricted = false;
		}

        TogglePlayerModeEvent togglePlayerModeEvent = new TogglePlayerModeEvent(this, PlayerMode.VANILLA);
        EpicFightEventHooks.Player.TOGGLE_MODE.postWithListener(togglePlayerModeEvent, this.getEventListener());

		if (!togglePlayerModeEvent.isCanceled()) {
			this.playerMode = togglePlayerModeEvent.getPlayerMode();
		}
	}
	
	public void toEpicFightMode(boolean synchronize) {
		if (this.playerMode == PlayerMode.EPICFIGHT) {
			return;
		}

        TogglePlayerModeEvent togglePlayerModeEvent = new TogglePlayerModeEvent(this, PlayerMode.EPICFIGHT);
        EpicFightEventHooks.Player.TOGGLE_MODE.postWithListener(togglePlayerModeEvent, this.getEventListener());

        if (!togglePlayerModeEvent.isCanceled()) {
            this.playerMode = togglePlayerModeEvent.getPlayerMode();
        }
	}
	
	public boolean isEpicFightMode() {
		return this.playerMode == PlayerMode.EPICFIGHT;
	}
	
	public boolean isVanillaMode() {
		return this.playerMode == PlayerMode.VANILLA;
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
	public float getYRotO() {
		return this.modelYRotO;
	}
	
	@Override
	public void setYRotO(float yRot) {
		this.modelYRotO = yRot;
	}
	
	@Override
	public void setYRot(float yRot) {
		this.setModelYRot(yRot, true);
	}
	
	@Override
	public float getYRotLimit() {
		return 180.0F;
	}

    /// Play a local sound
    public void playLocalSound(Holder<SoundEvent> sound) {
    }

	@Override
	public AnimationAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
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
	
	public double checkXTurn(double xRot) {
		return xRot;
	}
	
	public double checkYTurn(double yRot) {
		return yRot;
	}
	
	@Override
	public Faction getFaction() {
		return Factions.NEUTRAL;
	}
	
	public enum PlayerMode {
		VANILLA, EPICFIGHT
	}
}