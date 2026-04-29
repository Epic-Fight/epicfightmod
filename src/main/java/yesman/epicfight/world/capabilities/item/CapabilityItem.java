package yesman.epicfight.world.capabilities.item;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPChangeSkill;
import yesman.epicfight.network.server.SPSetRemotePlayerSkill;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent.ComboCounterHandler;

public class CapabilityItem {
	public static CapabilityItem EMPTY = CapabilityItem.builder().build();
	protected static List<AnimationAccessor<? extends AttackAnimation>> commonAutoAttackMotion;
	protected final WeaponCategory weaponCategory;
	
	static {
		commonAutoAttackMotion = Lists.newArrayList();
		commonAutoAttackMotion.add(Animations.FIST_AUTO1);
		commonAutoAttackMotion.add(Animations.FIST_AUTO2);
		commonAutoAttackMotion.add(Animations.FIST_AUTO3);
		commonAutoAttackMotion.add(Animations.FIST_DASH);
		commonAutoAttackMotion.add(Animations.FIST_AIR_SLASH);
	}
	
	public static List<AnimationAccessor<? extends AttackAnimation>> getBasicAutoAttackMotion() {
		return commonAutoAttackMotion;
	}
	
	public static List<AttributeModifier> getAttributeModifiers(Attribute attribute, EquipmentSlot slot, ItemStack itemstack, @Nullable LivingEntityPatch<?> entitypatch) {
		List<AttributeModifier> attributeModifiers = Lists.newArrayList();
		
		itemstack.getAttributeModifiers(slot).forEach((attribute$1, modifier) -> {
			if (attribute$1 == attribute) {
				attributeModifiers.add(modifier);
			}
		});
		
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(itemstack);
		
		if (!itemCap.isEmpty()) {
			itemCap.getAttributeModifiers(slot, entitypatch).forEach((attribute$1, modifier) -> {
				if (attribute$1 == attribute) {
					attributeModifiers.add(modifier);
				}
			});
		}
		
		return attributeModifiers;
	}
	
	protected static boolean validateAttribute(LivingEntityPatch<?> patch, Attribute attributeHolder) {
        return patch.getOriginal().getAttributes().hasAttribute(attributeHolder);
    }
	
	protected Map<Style, Map<Attribute, AttributeModifier>> attributeMap;
	protected Collider collider;
	
	protected CapabilityItem(CapabilityItem.Builder builder) {
		this.weaponCategory = builder.category;
		this.collider = builder.collider;
		
		ImmutableMap.Builder<Style, Map<Attribute, AttributeModifier>> attributeMapbuilder = ImmutableMap.builder();
		
		for (Map.Entry<Style, Map<Attribute, AttributeModifier>> entry : builder.attributeMap.entrySet()) {
			attributeMapbuilder.put(entry.getKey(), (entry.getValue()));
		}
		
		this.attributeMap = attributeMapbuilder.build();
	}
	
	public void modifyItemTooltip(ItemStack itemstack, List<Component> itemTooltip, LivingEntityPatch<?> entitypatch) {
		Style style = this instanceof RangedWeaponCapability ? Styles.RANGED : this.getStyle(entitypatch);
		
		/// TODO: Lazy Fix for crash https://mclo.gs/cfIdSOY. Need more inspection what causes this
		if (style == null) {
			return;
		}
		
		itemTooltip.add(1, Component.translatable(EpicFightMod.MODID + ".style." + style.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.DARK_GRAY));
		
		int index = 0;
		boolean modifyIn = false;

		for (int i = 0; i < itemTooltip.size(); i++) {
			Component textComp = itemTooltip.get(i);
			index = i;
			
			if (this.findComponentArgument(textComp, Attributes.ATTACK_SPEED.getDescriptionId()) != null) {
				modifyIn = true;
				break;
			}
		}
		
		index++;
		
		Map<Attribute, AttributeModifier> attribute = this.getDamageAttributesInCondition(style);
		
		if (attribute != null) {
			if (!modifyIn) {
				itemTooltip.add(index, Component.literal(""));
				index++;
				itemTooltip.add(index, Component.translatable("epicfight.gui.attribute").withStyle(ChatFormatting.GRAY));
				index++;
			}

			Attribute armorNegation = EpicFightAttributes.ARMOR_NEGATION.get();
			Attribute impact = EpicFightAttributes.IMPACT.get();
			Attribute maxStrikes = EpicFightAttributes.MAX_STRIKES.get();
			
			if (attribute.containsKey(armorNegation)) {
				double value = attribute.get(armorNegation).getAmount() + entitypatch.getOriginal().getAttribute(armorNegation).getBaseValue();

				if (value > 0.0D) {
					itemTooltip.add(index, Component.literal(" ").append(Component.translatable(armorNegation.getDescriptionId() + ".value", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value))));
				}
			}
			
			if (attribute.containsKey(impact)) {
				double value = attribute.get(impact).getAmount() + entitypatch.getOriginal().getAttribute(impact).getBaseValue();

				if (value > 0.0D) {
					int i = itemstack.getEnchantmentLevel(Enchantments.KNOCKBACK);
					value *= (1.0F + i * 0.12F);
					itemTooltip.add(index++, Component.literal(" ").append(Component.translatable(impact.getDescriptionId() + ".value", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value))));
				}
			}
			
			if (attribute.containsKey(maxStrikes)) {
				double value = attribute.get(maxStrikes).getAmount() + entitypatch.getOriginal().getAttribute(maxStrikes).getBaseValue();

				if (value > 0.0D) {
					itemTooltip.add(index++, Component.literal(" ").append(Component.translatable(maxStrikes.getDescriptionId() + ".value", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(value))));
				}
			} else {
				itemTooltip.add(index++, Component.literal(" ").append(Component.translatable(maxStrikes.getDescriptionId() + ".value", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(maxStrikes.getDefaultValue()))));
			}
		}
	}

	protected Object findComponentArgument(Component component, String key) {
		// check the content.
		if (component.getContents() instanceof TranslatableContents contents) {
			// is self?
			if (contents.getKey().equals(key)) {
				return component;
			}
			
			if (contents.getArgs() != null) {
				// check all arguments.
				for (Object arg : contents.getArgs()) {
					if (arg instanceof Component argComponent) {
						Object ret = this.findComponentArgument(argComponent, key);
						if (ret != null) {
							return ret;
						}
					}
				}
			}
		}
		// check all sibling.
		for (Component siblingComponent : component.getSiblings()) {
			Object ret = this.findComponentArgument(siblingComponent, key);
			if (ret != null) {
				return ret;
			}
		}
		
		return null;
	}
	
	public List<AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
		return getBasicAutoAttackMotion();
	}

	@Deprecated(since = "1.21.1", forRemoval = true)
	public List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion() {
		return null;
	}
	
	@Nullable
	public Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
		return null;
	}

	@Nullable @Deprecated(since = "1.21.1", forRemoval = true)
	public Skill getPassiveSkill() {
		return null;
	}
	
	public WeaponCategory getWeaponCategory() {
		return this.weaponCategory;
	}
	
	public void changeWeaponInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
		Skill weaponInnateSkill = this.getInnateSkill(playerpatch, itemstack);
		SkillContainer weaponInnateSkillContainer = playerpatch.getSkill(SkillSlots.WEAPON_INNATE);
		PayloadBundleBuilder toLocal = PayloadBundleBuilder.create();
		PayloadBundleBuilder toRemote = PayloadBundleBuilder.create();
		
		if (weaponInnateSkill != null) {
			if (weaponInnateSkillContainer.getSkill() != weaponInnateSkill) {
				weaponInnateSkillContainer.setSkill(weaponInnateSkill);
			}
			
			toLocal.and(new SPChangeSkill(SkillSlots.WEAPON_INNATE, playerpatch.getOriginal().getId(), weaponInnateSkill));
		} else {
			toLocal.and(SPSetSkillContainerValue.enable(SkillSlots.WEAPON_INNATE, true, playerpatch.getOriginal().getId()));
		}
		
		weaponInnateSkillContainer.setDisabled(weaponInnateSkill == null);
		toRemote.and(new SPSetRemotePlayerSkill(playerpatch.getOriginal().getId(), SkillSlots.WEAPON_INNATE, weaponInnateSkill));
		
		Skill passiveSkill = this.getPassiveSkill();
		SkillContainer passiveSkillContainer = playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE);
		
		if (passiveSkill != null) {
			if (passiveSkillContainer.getSkill() != passiveSkill) {
				passiveSkillContainer.setSkill(passiveSkill);
				toLocal.and(new SPChangeSkill(SkillSlots.WEAPON_PASSIVE, playerpatch.getOriginal().getId(), passiveSkill));
				toRemote.and(new SPSetRemotePlayerSkill(playerpatch.getOriginal().getId(), SkillSlots.WEAPON_PASSIVE, passiveSkill));
			}
		} else {
			passiveSkillContainer.setSkill(null);
			toLocal.and(new SPChangeSkill(SkillSlots.WEAPON_PASSIVE, playerpatch.getOriginal().getId(), null));
			toRemote.and(new SPSetRemotePlayerSkill(playerpatch.getOriginal().getId(), SkillSlots.WEAPON_PASSIVE, passiveSkill));
		}
		
		toLocal.send((first, others) -> {
			EpicFightNetworkManager.sendToPlayer(first, (ServerPlayer)playerpatch.getOriginal(), others);
		});
		
		toRemote.send((first, others) -> {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(first, (ServerPlayer)playerpatch.getOriginal(), others);
		});
	}
	
	public SoundEvent getSmashingSound() {
		return EpicFightSounds.WHOOSH.get();
	}

	public SoundEvent getHitSound() {
		return EpicFightSounds.BLUNT_HIT.get();
	}

	public Collider getWeaponCollider() {
		return this.collider != null ? this.collider : ColliderPreset.FIST;
	}

	public HitParticleType getHitParticle() {
		return EpicFightParticles.HIT_BLUNT.get();
	}
	
	public final Map<Attribute, AttributeModifier> getDamageAttributesInCondition(Style style) {
		Map<Attribute, AttributeModifier> attributes = this.attributeMap.getOrDefault(style, Maps.newHashMap());
		this.attributeMap.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(attributes::putIfAbsent);
		
		return attributes;
	}
	
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot equipmentSlot, @Nullable LivingEntityPatch<?> entitypatch) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		
		if (entitypatch != null) {
			Map<Attribute, AttributeModifier> modifierMap = this.getDamageAttributesInCondition(this.getStyle(entitypatch));
			
			if (modifierMap != null) {
				for (Entry<Attribute, AttributeModifier> entry : modifierMap.entrySet()) {
					map.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		return map;
    }
	
	public Multimap<Attribute, AttributeModifier> getAllAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
		
		for (Map<Attribute, AttributeModifier> attrMap : this.attributeMap.values()) {
			for (Entry<Attribute, AttributeModifier> entry : attrMap.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		
		return map;
    }
	
	public Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(LivingEntityPatch<?> playerpatch, InteractionHand hand) {
		return Maps.newHashMap();
	}
	
	public Style getStyle(LivingEntityPatch<?> entitypatch) {
		return this.canBePlacedOffhand() ? Styles.ONE_HAND : Styles.TWO_HAND;
	}
	
	public AnimationAccessor<? extends StaticAnimation> getGuardMotion(GuardSkill skill, GuardSkill.BlockType blockType, PlayerPatch<?> playerpatch) {
		return null;
	}
	
	public boolean canBePlacedOffhand() {
		return true;
	}
	
	/**
	 * Use {@link #handleComboCounter(PlayerPatch, AnimationAccessor)} with animation sensitive version
	 */
	@Deprecated(since = "1.21.1", forRemoval = true)
	public boolean shouldCancelCombo(LivingEntityPatch<?> entitypatch) {
		return true;
	}
	
	/**
	 * @param nextAnimation null when causal == {@link ComboCounterHandleEvent.Causal#TIME_EXPIRED}
	 */
	public int handleComboCounter(ComboCounterHandleEvent.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int original) {
		return ComboCounterHandler.DEFAULT_COMBO_HANDLER.handleComboCounter(this, causal, entitypatch, nextAnimation, original);
	}
	
	public boolean isEmpty() {
		return this == CapabilityItem.EMPTY;
	}
	
	public CapabilityItem getResult(ItemStack item) {
		return this;
	}

	@Deprecated(since = "1.21.1", forRemoval = true)
	public boolean availableOnHorse() {
		return true;
	}
	
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		return this.getStyle(entitypatch).canUseOffhand() && EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).canHoldInOffhandAlone();
	}
	
	public boolean canHoldInOffhandAlone() {
		return true;
	}
	
	public float getReach() {
		return 0.0F;
	}
	
	/**
	 * Get a custom composite living motion when holding item
	 * @param entitypatch
	 * @return
	 */
	public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, InteractionHand hand) {
		return null;
	}
	
	/**
	 * Called when player attacks with holding this item {@link AttackAnimation#attackTick}
	 * 
	 * @param entitypatch
	 * @param animation
	 */
	public void onStrike(LivingEntityPatch<?> entitypatch, AttackAnimation animation) {
	}
	
	public UseAnim getUseAnimation(LivingEntityPatch<?> entitypatch) {
		return UseAnim.NONE;
	}
	
	public ZoomInType getZoomInType() {
		return ZoomInType.NONE;
	}
	
	public enum WeaponCategories implements WeaponCategory {
		NOT_WEAPON, AXE, FIST, GREATSWORD, HOE, PICKAXE, SHOVEL, SWORD, UCHIGATANA, SPEAR, TACHI, TRIDENT, LONGSWORD, DAGGER, SHIELD, CROSSBOW, BOW;
		
		final int id;
		
		WeaponCategories() {
			this.id = WeaponCategory.ENUM_MANAGER.assign(this);
		}
		
		@Override
		public int universalOrdinal() {
			return this.id;
		}
	}
	
	public enum Styles implements Style {
		COMMON(true), ONE_HAND(true), TWO_HAND(false), MOUNT(true), RANGED(false), SHEATH(false), OCHS(false);
		
		final boolean canUseOffhand;
		final int id;
		
		Styles(boolean canUseOffhand) {
			this.id = Style.ENUM_MANAGER.assign(this);
			this.canUseOffhand = canUseOffhand;
		}
		
		@Override
		public int universalOrdinal() {
			return this.id;
		}
		
		public boolean canUseOffhand() {
			return this.canUseOffhand;
		}
	}
	
	public enum ZoomInType {
		NONE, ALWAYS, USE_TICK, AIMING, CUSTOM
	}
	
	public static CapabilityItem.Builder builder() {
		return new CapabilityItem.Builder();
	}
	
	public static class Builder {
		Function<Builder, CapabilityItem> constructor;
		Map<Style, Map<Attribute, AttributeModifier>> attributeMap;
		WeaponCategory category;
		Collider collider;
		
		protected Builder() {
			this.constructor = CapabilityItem::new;
			this.attributeMap = Maps.newHashMap();
			this.category = WeaponCategories.FIST;
			this.collider = ColliderPreset.FIST;
		}
		
		public Builder constructor(Function<Builder, CapabilityItem> constructor) {
			this.constructor = constructor;
			return this;
		}
		
		public Builder category(WeaponCategory category) {
			this.category = category;
			return this;
		}
		
		public Builder collider(Collider collider) {
			this.collider = collider;
			return this;
		}
		
		public Builder addStyleAttibutes(Style style, Pair<Attribute, AttributeModifier> attributePair) {
			Map<Attribute, AttributeModifier> map = this.attributeMap.computeIfAbsent(style, (key) -> Maps.newHashMap());
			map.put(attributePair.getFirst(), attributePair.getSecond());
			
			return this;
		}
		
		public final CapabilityItem build() {
			return this.constructor.apply(this);
		}
		
		public Collider getCollider() {
			return this.collider;
		}
	}
}
