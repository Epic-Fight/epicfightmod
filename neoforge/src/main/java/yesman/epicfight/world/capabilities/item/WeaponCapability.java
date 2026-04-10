package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.netty.util.internal.StringUtil;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.MainFrameAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.event.types.player.ModifyComboCounter;
import yesman.epicfight.api.ex_cap.modules.core.data.MoveSet;
import yesman.epicfight.api.ex_cap.modules.core.provider.CoreWeaponCapabilityProvider;
import yesman.epicfight.api.ex_cap.modules.core.provider.ProviderConditional;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.*;
import java.util.function.Function;

public class WeaponCapability extends CapabilityItem {
    protected final CoreWeaponCapabilityProvider coreProvider;
    @Deprecated
	protected final Function<LivingEntityPatch<?>, Style> stylegetter;
    @Deprecated
	protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
    @Deprecated
	protected final Skill passiveSkill;
    protected final boolean offHandAlone;
	protected final SoundEvent smashingSound;
	protected final SoundEvent hitSound;
	protected final HitParticleType hitParticle;
    protected final Map<Style, MoveSet> moveSets;
    @Deprecated
	protected final Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotions;
    @Deprecated
	protected final Map<Style, Function<ItemStack, Skill>> innateSkill;
    @Deprecated
	protected final Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
	protected final boolean canBePlacedOffhand;
    @Deprecated
    protected final Function<Style, Boolean> comboCancel;
    protected final ModifyComboCounter.ComboCounterHandler comboCounterHandler;
	protected final ZoomInType zoomInType;
	protected final float reach;

    /// A custom capability tag that ease identifying categories
    ///
    /// Weapon capabilities have registry name of their weapon type builder
    protected Set<ResourceLocation> customTags;

	protected WeaponCapability(WeaponCapability.Builder builder) {
		super(builder);
        this.coreProvider = builder.provider;
        this.moveSets = builder.moveSets;
        this.offHandAlone = builder.offHandAlone;
        this.autoAttackMotions = builder.autoAttackMotionMap;
		this.innateSkill = builder.innateSkillByStyle;
		this.livingMotionModifiers = builder.livingMotionModifiers;
		this.stylegetter = builder.styleProvider;
		this.weaponCombinationPredicator = builder.weaponCombinationPredicator;
		this.passiveSkill = builder.passiveSkill;
		this.smashingSound = builder.swingSound;
		this.hitParticle = builder.hitParticle;
		this.hitSound = builder.hitSound;
		this.canBePlacedOffhand = builder.canBePlacedOffhand;
		this.comboCancel = builder.comboCancel;
        this.comboCounterHandler = builder.comboCounterHandler;
		this.zoomInType = builder.zoomInType;
		this.reach = builder.reach;
        this.customTags = Collections.unmodifiableSet(builder.customTags);
	}



    public MoveSet getCurrentSet(LivingEntityPatch<?> patch)
    {
        Style style = getStyle(patch);
        return moveSets.getOrDefault(style, moveSets.get(Styles.COMMON));
    }

    private AnimationAccessor<? extends StaticAnimation> processGuard(List<AnimationAccessor<? extends StaticAnimation>> motions, GuardSkill.BlockType blockType, PlayerPatch<?> playerpatch, SkillContainer container, int counter)
    {
        if (!motions.isEmpty()) {
            AnimationAccessor<? extends StaticAnimation> result = motions.get(counter % motions.size());
            if (blockType == GuardSkill.BlockType.ADVANCED_GUARD && !playerpatch.isLogicalClient()) {
                result = motions.get(container.getDataManager().getDataValue(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER) % motions.size());
                container.getDataManager().setDataSyncF(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER, count -> count + 1);
            }
            return result;
        }
        return null;
    }

    @Override
    public AnimationAccessor<? extends StaticAnimation> getGuardMotion(GuardSkill skill, GuardSkill.BlockType blockType, PlayerPatch<?> playerpatch)
    {
        MoveSet currentSet = getCurrentSet(playerpatch);
        SkillContainer container = playerpatch.getSkill(SkillSlots.GUARD);
        int counter = blockType == GuardSkill.BlockType.ADVANCED_GUARD && container.getDataManager().hasData(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER) ? container.getDataManager().getDataValue(EpicFightSkillDataKeys.PARRY_MOTION_COUNTER) : 0;
        if (currentSet != null) {
            Map<Skill, Map<GuardSkill.BlockType, List<AnimationAccessor<? extends StaticAnimation>>>> skillSpecificGuardMotions = currentSet.getSkillSpecificGuardAnimations();
            Map<GuardSkill.BlockType, List<AnimationAccessor<? extends StaticAnimation>>> defaultGuardMotions = currentSet.getDefaultGuardAnimations();
            if (skillSpecificGuardMotions != null && skillSpecificGuardMotions.containsKey(skill) && skillSpecificGuardMotions.get(skill).containsKey(blockType)) {
                List<AnimationAccessor<? extends StaticAnimation>> motions = skillSpecificGuardMotions.get(skill).get(blockType);
                return processGuard(motions, blockType, playerpatch, container, counter);
            } else if (defaultGuardMotions != null && defaultGuardMotions.containsKey(blockType)) {
                List<AnimationAccessor<? extends StaticAnimation>> motions = defaultGuardMotions.get(blockType);
                return processGuard(motions, blockType, playerpatch, container, counter);
            }
        }
        return super.getGuardMotion(skill, blockType, playerpatch);
    }

    @Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
        MoveSet set = getCurrentSet(playerpatch);
        if (set == null) {
            //Fallback
            List<AnimationAccessor<? extends AttackAnimation>> attacks = autoAttackMotions.getOrDefault(getStyle(playerpatch), autoAttackMotions.get(Styles.COMMON));
            if (attacks == null || attacks.isEmpty()) {
                return super.getAutoAttackMotion(playerpatch);
            }
            return attacks;
        }
		return set.getComboAttackAnimations();
	}
	
	@Override
	public final Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
        MoveSet set = getCurrentSet(playerpatch);
        if (set == null) {
            //Fallback Logic
            if (innateSkill.get(getStyle(playerpatch)) == null)
                return null;
            return innateSkill.get(getStyle(playerpatch)).apply(itemstack);
        }
        return set.getWeaponInnateSkill() == null ? null : set.getWeaponInnateSkill().apply(itemstack, playerpatch);
	}
	
	@Override
	public Skill getPassiveSkill(PlayerPatch<?> playerPatch) {
		MoveSet set = getCurrentSet(playerPatch);
        if (set == null) {
            //Fallback logic
            return getPassiveSkill();
        }
        return set.getWeaponPassiveSkill();
	}

    /// Legacy method
    public Skill getPassiveSkill()
    {
        return passiveSkill;
    }

	@Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion(PlayerPatch<?> playerpatch) {
        MoveSet set = getCurrentSet(playerpatch);
        if (set == null) {
            //Fallback logic
            return this.autoAttackMotions.get(Styles.MOUNT);
        }
        return set.getMountAttackAnimations();
    }

    /// Legacy method used by addons
    @Deprecated
    public final List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion()
    {
        return this.autoAttackMotions.get(Styles.MOUNT);
    }
	
	@Override @NotNull
	public Style getStyle(LivingEntityPatch<?> entityPatch) {
        Style style = coreProvider.getStyle(entityPatch);
        if (style == null)
        {
            //Fallback
            return this.stylegetter.apply(entityPatch);
        }
        return style;
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
    public int handleComboCounter(ModifyComboCounter.Causal causal, PlayerPatch<?> entitypatch, @Nullable AnimationAccessor<? extends MainFrameAnimation> nextAnimation, int original) {
        return this.comboCounterHandler.handleComboCounter(this, causal, entitypatch, nextAnimation, original);
    }

	@Override
	public ZoomInType getZoomInType() {
		return this.zoomInType;
	}

    @Override
	public Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifier(LivingEntityPatch<?> player, InteractionHand hand) {
		MoveSet set = getCurrentSet(player);
        if (set == null || set.getLivingMotionModifiers() == null)
        {
            //Fallback to legacy
            if (this.livingMotionModifiers == null || hand == InteractionHand.OFF_HAND) {
                return super.getLivingMotionModifier(player, hand);
            }
            Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> motions = this.livingMotionModifiers.getOrDefault(this.getStyle(player), Maps.newHashMap());
            this.livingMotionModifiers.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);

            return motions;
        }
        Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> result = Maps.newHashMap();
        result.putAll(set.getLivingMotionModifiers());
        return result;
	}
	
	@Override
	public UseAnim getUseAnimation(LivingEntityPatch<?> entityPatch) {
        MoveSet set = getCurrentSet(entityPatch);
        if (set == null || set.getLivingMotionModifiers() == null)
        {
            //Fallback
            if (this.livingMotionModifiers != null) {
                Style style = this.getStyle(entityPatch);
                if (this.livingMotionModifiers.containsKey(style)) {
                    if (this.livingMotionModifiers.get(style).containsKey(LivingMotions.BLOCK)) {
                        return UseAnim.BLOCK;
                    }
                }
            }
        }
		else if (set.getLivingMotionModifiers().containsKey(LivingMotions.BLOCK)) {
            return UseAnim.BLOCK;
        }
		return UseAnim.NONE;
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return offHandAlone;
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
        Boolean valid = coreProvider.checkVisibleOffHand(entitypatch);
        if (valid == null) {
            valid = super.checkOffhandValid(entitypatch) || weaponCombinationPredicator.apply(entitypatch);
        } else {
            valid = valid || super.checkOffhandValid(entitypatch);
        }
        return valid;
	}
	
	@Override
	public boolean availableOnHorse(LivingEntityPatch<?> entityPatch) {
        MoveSet set = getCurrentSet(entityPatch);
        if (set == null || set.getMountAttackAnimations() == null || set.getMountAttackAnimations().isEmpty())
		    return availableOnHorse();
        return true;
	}

    @Override
    public boolean availableOnHorse() {
        return this.autoAttackMotions.containsKey(Styles.MOUNT);
    }

    @Override
	public float getReach() {
		return this.reach;
	}
	
	public static WeaponCapability.Builder builder() {
        return new Builder();
	}

    @Override
    public LivingMotion getLivingMotion(LivingEntityPatch<?> entitypatch, InteractionHand hand) {
        MoveSet set = getCurrentSet(entitypatch);
        if (set == null || set.getCustomMotion().apply(entitypatch, hand) == null)
            return super.getLivingMotion(entitypatch, hand);
        return set.getCustomMotion().apply(entitypatch, hand);
    }

    public boolean hasMatchingTag(ResourceLocation rl) {
        return customTags.contains(rl);
    }

    public Set<ResourceLocation> getTags() {
        return customTags;
    }

    /// All fields marked with {@link Deprecated} have been moved to {@link MoveSet} and exist as legacy fallback options to prevent addons from breaking.
    public static class Builder extends CapabilityItem.Builder<WeaponCapability.Builder> {
		CoreWeaponCapabilityProvider provider;
        @Deprecated
        Function<LivingEntityPatch<?>, Style> styleProvider;
        @Deprecated
		Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
        @Deprecated
		Skill passiveSkill;
		SoundEvent swingSound;
		SoundEvent hitSound;
		HitParticleType hitParticle;
        Map<Style, MoveSet> moveSets;
        double baseAP;
        double aPScaling;
        double impactBase;
        double impactScaling;
        @Deprecated
		Map<Style, List<AnimationAccessor<? extends AttackAnimation>>> autoAttackMotionMap;
        @Deprecated
		Map<Style, Function<ItemStack, Skill>> innateSkillByStyle;
        @Deprecated
		Map<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> livingMotionModifiers;
        @Deprecated
		Function<Style, Boolean> comboCancel;
        ModifyComboCounter.ComboCounterHandler comboCounterHandler;
		boolean canBePlacedOffhand;
		ZoomInType zoomInType;
		float reach;
        boolean offHandAlone;

        Set<ResourceLocation> customTags = new HashSet<> ();

        public Builder copy() {
            Builder copy = new Builder();
            copy.constructor = this.constructor;
            copy.provider = this.provider.copy();
            copy.category = this.category;
            copy.styleProvider = this.styleProvider;
            copy.weaponCombinationPredicator = this.weaponCombinationPredicator;
            copy.passiveSkill = this.passiveSkill;
            copy.offHandAlone = this.offHandAlone;
            copy.collider = this.collider;
            copy.attributeMap.putAll(this.attributeMap);

            copy.swingSound = this.swingSound;
            copy.hitSound = this.hitSound;
            copy.hitParticle = this.hitParticle;

            copy.comboCancel = this.comboCancel;
            copy.comboCounterHandler = this.comboCounterHandler;

            copy.canBePlacedOffhand = this.canBePlacedOffhand;
            copy.zoomInType = this.zoomInType;
            copy.reach = this.reach;

            if (this.moveSets != null) {
                copy.moveSets = Maps.newHashMap();
                copy.moveSets.putAll(this.moveSets);
            }
            if (this.autoAttackMotionMap != null) {
                copy.autoAttackMotionMap = Maps.newHashMap();
                for (Map.Entry<Style, List<AnimationAccessor<? extends AttackAnimation>>> entry
                        : this.autoAttackMotionMap.entrySet()) {

                    copy.autoAttackMotionMap.put(
                            entry.getKey(),
                            Lists.newArrayList(entry.getValue())
                    );
                }
            }

            if (this.innateSkillByStyle != null) {
                copy.innateSkillByStyle = Maps.newHashMap(this.innateSkillByStyle);
            }

            if (this.livingMotionModifiers != null) {
                copy.livingMotionModifiers = Maps.newHashMap();

                for (Map.Entry<Style, Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> entry
                        : this.livingMotionModifiers.entrySet()) {

                    copy.livingMotionModifiers.put(
                            entry.getKey(),
                            Maps.newHashMap(entry.getValue())
                    );
                }
            }

            copy.customTags.addAll(this.customTags);

            return copy;
        }
		
		protected Builder() {
            this.provider = new CoreWeaponCapabilityProvider();
            this.offHandAlone = false;
			this.constructor = WeaponCapability::new;
			this.styleProvider = (entitypatch) -> Styles.ONE_HAND;
			this.weaponCombinationPredicator = (entitypatch) -> false;
			this.passiveSkill = null;
			this.swingSound = EpicFightSounds.WHOOSH.get();
			this.hitSound = EpicFightSounds.BLUNT_HIT.get();
            this.moveSets = Maps.newHashMap();
			this.hitParticle = EpicFightParticles.HIT_BLADE.get();
			this.autoAttackMotionMap = Maps.newHashMap();
			this.innateSkillByStyle = Maps.newHashMap();
			this.livingMotionModifiers = null;
			this.canBePlacedOffhand = true;
			this.comboCancel = (style) -> true;
            this.comboCounterHandler = ModifyComboCounter.ComboCounterHandler.DEFAULT_COMBO_HANDLER;
			this.zoomInType = ZoomInType.NONE;
			this.reach = 0.2F;
            this.baseAP = 0;
            this.aPScaling = 1;
            this.impactBase = 1;
            this.impactScaling = 1;
		}

        public Builder offHandAlone(final boolean offHandAlone) {
            this.offHandAlone = offHandAlone;
            return this;
        }
		
		public Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
			this.styleProvider = styleProvider;
			return this;
		}

        public Builder setTierValues(double baseAP, double aPScaling, double impactBase, double impactScaling)
        {
            this.baseAP = baseAP;
            this.aPScaling = aPScaling;
            this.impactBase = impactBase;
            this.impactScaling = impactScaling;
            return this;
        }

        /**
         * This is not to be called statically and only called during registration.
         * @param tier the tier value used by Yesman
         */
        public void modifyTierAttributes(int tier)
        {
            if (tier != 0) this.addStyleAttibutes(Styles.COMMON, EpicFightAttributes.ARMOR_NEGATION, EpicFightAttributes.getArmorNegationModifier(baseAP + aPScaling * tier));
            this.addStyleAttibutes(Styles.COMMON, EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(impactBase + impactScaling * tier));
        }
		
		public Builder passiveSkill(Skill passiveSkill) {
			this.passiveSkill = passiveSkill;
			return this;
		}
		
		public Builder swingSound(SoundEvent swingSound) {
			this.swingSound = swingSound;
			return this;
		}

        public Builder addConditionals(ProviderConditional... conditionals)
        {
            provider.addConditional(conditionals);
            return this;
        }

        public Builder addConditionals(List<ProviderConditional> conditionals)
        {
            provider.addConditional(conditionals);
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

        public Builder addMoveSet(Style style, MoveSet.MoveSetBuilder moveSet) {
            moveSets.put(style, moveSet.build());
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

        public static WeaponCapability.Builder deserializeBuilder(ResourceLocation id, JsonElement element) throws JsonParseException
        {
            WeaponCapability.Builder builder = builder();
            JsonObject tag = element.getAsJsonObject();

            //Unlike the Legacy WeaponType deserialization method, this is much more simple and strict.

            try {
                if (!tag.has("category") || StringUtil.isNullOrEmpty(tag.get("category").getAsString())) {
                    throw new IllegalArgumentException("Define weapon category.");
                }

                builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.get("category").getAsString()));
                builder.collider(ColliderPreset.deserializeSimpleCollider(TagParser.parseTag(tag.get("collider").getAsString())));

                if (tag.has("hit_particle")) {
                    ParticleType<?> particleType = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(tag.get("hit_particle").getAsString()));
                    builder.hitParticle((HitParticleType)particleType);
                }

                if (tag.has("swing_sound")) {
                    SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(tag.get("swing_sound").getAsString()));
                    builder.swingSound(sound);
                }

                if (tag.has("hit_sound")) {
                    SoundEvent sound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(tag.get("hit_sound").getAsString()));
                    builder.hitSound(sound);
                }

                if (tag.has("custom_tags")) {
                    for (JsonElement customTagElement : tag.get("custom_tags").getAsJsonArray()) {
                        builder.addTag(ResourceLocation.parse(customTagElement.getAsString()));
                    }
                }
            } catch (Exception e) {
                throw new JsonParseException(e.getMessage());
            }

            builder.addTag(id);

            return builder;
        }

		
		public Builder livingMotionModifier(Style wieldStyle, LivingMotion livingMotion, AnimationAccessor<? extends StaticAnimation> animation) {
			if (AnimationManager.checkNull(animation)) {
                EpicFight.LOGGER.warn("Unable to put an empty animation to weapon capability builder: {}, {}", livingMotion, animation);
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
		
		@SafeVarargs
		public final Builder newStyleCombo(Style style, AnimationAccessor<? extends AttackAnimation>... animation) {
			this.autoAttackMotionMap.put(style, Lists.newArrayList(animation));
			return this;
		}
		
		public Builder weaponCombinationPredicator(Function<LivingEntityPatch<?>, Boolean> predicator) {
			this.weaponCombinationPredicator = predicator;
			return this;
		}
		
		public Builder innateSkill(Style style, Function<ItemStack, Skill> innateSkill) {
			this.innateSkillByStyle.put(style, innateSkill);
			return this;
		}

        /// @Deprecated - Use more sensitive version [#comboCounterHandler]
        @Deprecated
		public Builder comboCancel(Function<Style, Boolean> comboCancel) {
			this.comboCancel = comboCancel;
			return this;
		}

        public Builder comboCounterHandler(ModifyComboCounter.ComboCounterHandler comboHandler) {
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