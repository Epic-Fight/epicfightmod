package yesman.epicfight.world.capabilities.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent;
import yesman.epicfight.world.entity.eventlistener.ComboCounterHandleEvent.ComboCounterHandler;

public class WeaponCapability extends CapabilityItem {
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Function<LivingEntityPatch<?>, Style> stylegetter;
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Skill passiveSkill;
	protected final SoundEvent smashingSound;
	protected final SoundEvent hitSound;
	protected final HitParticleType hitParticle;
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotions;
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Map<Style, Function<ItemStack, Skill>> innateSkill;
	@Deprecated(since = "1.21.1", forRemoval = true)
	protected final Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
	protected final boolean canBePlacedOffhand;
	@Deprecated
	protected final Function<Style, Boolean> comboCancel;
	protected final ComboCounterHandler comboCounterHandler;
	protected final ZoomInType zoomInType;
	protected final float reach;
	
	/// A custom capability tag that ease identifying categories
    ///
    /// Weapon capabilities have registry name of their weapon type builder
    protected Set<ResourceLocation> customTags;
	
	protected WeaponCapability(CapabilityItem.Builder builder) {
		super(builder);
		
		WeaponCapability.Builder weaponBuilder = (WeaponCapability.Builder)builder;
		
		this.autoAttackMotions = weaponBuilder.autoAttackMotionMap;
		this.innateSkill = weaponBuilder.innateSkillByStyle;
		this.livingMotionModifiers = weaponBuilder.livingMotionModifiers;
		this.stylegetter = weaponBuilder.styleProvider;
		this.weaponCombinationPredicator = weaponBuilder.weaponCombinationPredicator;
		this.passiveSkill = weaponBuilder.passiveSkill;
		this.smashingSound = weaponBuilder.swingSound;
		this.hitParticle = weaponBuilder.hitParticle;
		this.hitSound = weaponBuilder.hitSound;
		this.canBePlacedOffhand = weaponBuilder.canBePlacedOffhand;
		this.comboCancel = weaponBuilder.comboCancel;
		this.comboCounterHandler = weaponBuilder.comboCounterHandler;
		this.zoomInType = weaponBuilder.zoomInType;
		this.reach = weaponBuilder.reach;
		this.customTags = Collections.unmodifiableSet(weaponBuilder.customTags);
	}
	
	@Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
		return this.autoAttackMotions.getOrDefault(this.getStyle(playerpatch), this.autoAttackMotions.get(Styles.COMMON));
	}
	
	@Override
	public final Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
		Function<ItemStack, Skill> innateProvider = this.innateSkill.getOrDefault(this.getStyle(playerpatch), this.innateSkill.get(Styles.COMMON));
		return innateProvider == null ? null : innateProvider.apply(itemstack);
	}


	@Override @Deprecated(since = "1.21.1", forRemoval = true)
	public Skill getPassiveSkill() {
		return this.passiveSkill;
	}
	
	@Override @Deprecated(since = "1.21.1", forRemoval = true)
	public final List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion() {
		return this.autoAttackMotions.get(Styles.MOUNT);
	}
	
	@Override
	public Style getStyle(LivingEntityPatch<?> entitypatch) {
		return this.stylegetter.apply(entitypatch);
	}
	
	@Override
	public SoundEvent getSmashingSound() {
		return this.smashingSound;
	}
	
	@Override
	public SoundEvent getHitSound() {
		return this.hitSound;
	}
	
	@Override
	public HitParticleType getHitParticle() {
		return this.hitParticle;
	}
	
	@Override
	public boolean canBePlacedOffhand() {
		return this.canBePlacedOffhand;
	}
	
	@Override
	public boolean shouldCancelCombo(LivingEntityPatch<?> entitypatch) {
		return this.comboCancel.apply(this.getStyle(entitypatch));
	}
	
	@Override
	public int handleComboCounter(ComboCounterHandleEvent.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int original) {
		return this.comboCounterHandler.handleComboCounter(this, causal, entitypatch, nextAnimation, original);
	}
	
	@Override
	public ZoomInType getZoomInType() {
		return this.zoomInType;
	}
	
	@Override
	public Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(LivingEntityPatch<?> player, InteractionHand hand) {
		if (this.livingMotionModifiers == null || hand == InteractionHand.OFF_HAND) {
			return super.getLivingMotionModifier(player, hand);
		}
		
		Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> motions = this.livingMotionModifiers.getOrDefault(this.getStyle(player), Maps.newHashMap());
		this.livingMotionModifiers.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);
		
		return motions;
	}
	
	@Override
	public UseAnim getUseAnimation(LivingEntityPatch<?> playerpatch) {
		if (this.livingMotionModifiers != null) {
			Style style = this.getStyle(playerpatch);
			
			if (this.livingMotionModifiers.containsKey(style)) {
				if (this.livingMotionModifiers.get(style).containsKey(LivingMotions.BLOCK)) {
					return UseAnim.BLOCK;
				}
			}
		}
		
		return UseAnim.NONE;
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return false;
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		return super.checkOffhandValid(entitypatch) || this.weaponCombinationPredicator.apply(entitypatch);
	}
	
	@Override @Deprecated(since = "1.21.1", forRemoval = true)
	public boolean availableOnHorse() {
		return this.autoAttackMotions.containsKey(Styles.MOUNT);
	}
	
	@Override
	public float getReach() {
		return this.reach;
	}
	
	public boolean hasMatchingTag(ResourceLocation rl) {
        return customTags.contains(rl);
    }

    public Set<ResourceLocation> getTags() {
        return customTags;
    }
	
	public static WeaponCapability.Builder builder() {
		return new WeaponCapability.Builder();
	}
	
	public static class Builder extends CapabilityItem.Builder {
		@Deprecated(since = "1.21.1", forRemoval = true)
		Function<LivingEntityPatch<?>, Style> styleProvider;
		@Deprecated(since = "1.21.1", forRemoval = true)
		Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
		@Deprecated(since = "1.21.1", forRemoval = true)
		Skill passiveSkill;
		SoundEvent swingSound;
		SoundEvent hitSound;
		HitParticleType hitParticle;
		@Deprecated(since = "1.21.1", forRemoval = true)
		Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotionMap;
		@Deprecated(since = "1.21.1", forRemoval = true)
		Map<Style, Function<ItemStack, Skill>> innateSkillByStyle;
		@Deprecated(since = "1.21.1", forRemoval = true)
		Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
		Function<Style, Boolean> comboCancel;
		ComboCounterHandler comboCounterHandler;
		boolean canBePlacedOffhand;
		ZoomInType zoomInType;
		float reach;
		Set<ResourceLocation> customTags = new HashSet<> ();
		
		protected Builder() {
			this.constructor = WeaponCapability::new;
			this.styleProvider = (entitypatch) -> Styles.ONE_HAND;
			this.weaponCombinationPredicator = (entitypatch) -> false;
			this.passiveSkill = null;
			this.swingSound = EpicFightSounds.WHOOSH.get();
			this.hitSound = EpicFightSounds.BLUNT_HIT.get();
			this.hitParticle = EpicFightParticles.HIT_BLADE.get();
			this.autoAttackMotionMap = Maps.newHashMap();
			this.innateSkillByStyle = Maps.newHashMap();
			this.livingMotionModifiers = null;
			this.canBePlacedOffhand = true;
			this.comboCancel = (style) -> true;
			this.comboCounterHandler = ComboCounterHandler.DEFAULT_COMBO_HANDLER;
			this.zoomInType = ZoomInType.NONE;
			this.reach = 0.2F;
		}
		
		@Override
		public Builder category(WeaponCategory category) {
			super.category(category);
			return this;
		}
		@Deprecated(since = "1.21.1", forRemoval = true)
		public Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
			this.styleProvider = styleProvider;
			return this;
		}
		@Deprecated(since = "1.21.1", forRemoval = true)
		public Builder passiveSkill(Skill passiveSkill) {
			this.passiveSkill = passiveSkill;
			return this;
		}
		
		public Builder swingSound(SoundEvent swingSound) {
			this.swingSound = swingSound;
			return this;
		}
		
		public Builder hitSound(SoundEvent hitSound) {
			this.hitSound = hitSound;
			return this;
		}
		
		public Builder hitParticle(HitParticleType hitParticle) {
			this.hitParticle = hitParticle;
			return this;
		}
		
		public Builder collider(Collider collider) {
			this.collider = collider;
			return this;
		}
		
		public Builder canBePlacedOffhand(boolean canBePlacedOffhand) {
			this.canBePlacedOffhand = canBePlacedOffhand;
			return this;
		}
		
		public Builder reach(float reach) {
			this.reach = reach;
			return this;
		}
		
		public Builder addTag(ResourceLocation customTag) {
            this.customTags.add(customTag);
            return this;
        }
		@Deprecated(since = "1.21.1", forRemoval = true)
		public Builder livingMotionModifier(Style wieldStyle, LivingMotion livingMotion, AnimationAccessor<? extends StaticAnimation> animation) {
			if (AnimationManager.checkNull(animation)) {
				EpicFightMod.LOGGER.warn("Unable to put an empty animation to weapon capability builder: " + livingMotion + ", " + animation);
				return this;
			}
			
			if (this.livingMotionModifiers == null) {
				this.livingMotionModifiers = Maps.newHashMap();
			}
			
			if (!this.livingMotionModifiers.containsKey(wieldStyle)) {
				this.livingMotionModifiers.put(wieldStyle, Maps.newHashMap());
			}
			
			this.livingMotionModifiers.get(wieldStyle).put(livingMotion, animation);
			
			return this;
		}
		
		public Builder addStyleAttibutes(Style style, Pair<Attribute, AttributeModifier> attributePair) {
			super.addStyleAttibutes(style, attributePair);
			return this;
		}
		
		@SafeVarargs @Deprecated(since = "1.21.1", forRemoval = true)
		public final Builder newStyleCombo(Style style, AnimationAccessor<? extends AttackAnimation>... animation) {
			this.autoAttackMotionMap.put(style, Lists.newArrayList(animation));
			return this;
		}
		@Deprecated(since = "1.21.1", forRemoval = true)
		public Builder weaponCombinationPredicator(Function<LivingEntityPatch<?>, Boolean> predicator) {
			this.weaponCombinationPredicator = predicator;
			return this;
		}
		@Deprecated(since = "1.21.1")
		public Builder innateSkill(Style style, Function<ItemStack, Skill> innateSkill) {
			this.innateSkillByStyle.put(style, innateSkill);
			return this;
		}
		
		/**
		 * @Deprecated - Use more sensitive version {@link #comboCounterHandler}
		 */
		@Deprecated
		public Builder comboCancel(Function<Style, Boolean> comboCancel) {
			this.comboCancel = comboCancel;
			return this;
		}
		
		public Builder comboCounterHandler(ComboCounterHandler comboHandler) {
			this.comboCounterHandler = comboHandler;
			return this;
		}
		
		public Builder zoomInType(ZoomInType zoomInType) {
			this.zoomInType = zoomInType;
			return this;
		}
		
		public Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> getComboAnimations() {
			return ImmutableMap.copyOf(this.autoAttackMotionMap);
		}
	}
}