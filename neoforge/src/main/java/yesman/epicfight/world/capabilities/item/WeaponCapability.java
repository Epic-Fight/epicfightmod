package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.builders.MoveSet;
import yesman.epicfight.world.capabilities.item.builders.providers.CoreWeaponCapabilityProvider;
import yesman.epicfight.world.capabilities.item.builders.providers.ProviderConditional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class WeaponCapability extends CapabilityItem {
    protected final CoreWeaponCapabilityProvider coreProvider;
    @Deprecated
	protected final Function<LivingEntityPatch<?>, Style> stylegetter;
    @Deprecated
	protected final Function<LivingEntityPatch<?>, Boolean> weaponCombinationPredicator;
    @Deprecated
	protected final Skill passiveSkill;
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
	
	protected WeaponCapability(WeaponCapability.Builder builder) {
		super(builder);
        this.coreProvider = builder.provider;
        this.moveSets = builder.moveSets;
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
	}

    

    private MoveSet getCurrentSet(LivingEntityPatch<?> patch)
    {
        Style style = stylegetter.apply(patch);
        return moveSets.getOrDefault(style, moveSets.get(Styles.COMMON));
    }

	@Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getAutoAttackMotion(PlayerPatch<?> playerpatch) {
        MoveSet set = getCurrentSet(playerpatch);
		return set.getComboAttackAnimations();
	}
	
	@Override
	public final Skill getInnateSkill(PlayerPatch<?> playerpatch, ItemStack itemstack) {
        MoveSet set = getCurrentSet(playerpatch);
        if (set == null) {
            //Fallback Logic
            return innateSkill.get(getStyle(playerpatch)).apply(itemstack);
        }
        return set.getWeaponInnateSkill() == null ? null : set.getWeaponInnateSkill().apply(itemstack);
	}
	
	@Override
	public Skill getPassiveSkill(PlayerPatch<?> playerPatch) {
		MoveSet set = getCurrentSet(playerPatch);
        return set.getWeaponPassiveSkill();
	}

	@Override
	public final List<AnimationAccessor<? extends AttackAnimation>> getMountAttackMotion(PlayerPatch<?> playerpatch) {
        MoveSet set = getCurrentSet(playerpatch);
        return set.getMountAttackAnimations();
    }
	
	@Override @NotNull
	public Style getStyle(LivingEntityPatch<?> entitypatch) {
        Style style = coreProvider.getStyle(entitypatch);
        if (style == null)
        {
            //Fallback
            return this.stylegetter.apply(entitypatch);
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
            //Fallback
            if (this.livingMotionModifiers == null || hand == InteractionHand.OFF_HAND) {
                return super.getLivingMotionModifier(player, hand);
            }
            Map<LivingMotion, AnimationAccessor<? extends StaticAnimation>> motions = this.livingMotionModifiers.getOrDefault(this.getStyle(player), Maps.newHashMap());
            this.livingMotionModifiers.getOrDefault(Styles.COMMON, Maps.newHashMap()).forEach(motions::putIfAbsent);

            return motions;
        }
        return set.getLivingMotionModifiers();
	}
	
	@Override
	public UseAnim getUseAnimation(LivingEntityPatch<?> entitypatch) {
        MoveSet set = getCurrentSet(entitypatch);
        if (set == null || set.getLivingMotionModifiers() == null)
        {
            //Fallback
            if (this.livingMotionModifiers != null) {
                Style style = this.getStyle(entitypatch);

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
		return false;
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
		    return this.autoAttackMotions.containsKey(Styles.MOUNT);
        return true;
	}
	
	@Override
	public float getReach() {
		return this.reach;
	}
	
	public static WeaponCapability.Builder builder() {
		return new WeaponCapability.Builder();
	}
	
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

        public Builder copy() {
            Builder copy = new Builder();

            copy.provider = this.provider;
            copy.styleProvider = this.styleProvider;
            copy.weaponCombinationPredicator = this.weaponCombinationPredicator;
            copy.passiveSkill = this.passiveSkill;

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

            return copy;
        }
		
		protected Builder() {
            this.provider = new CoreWeaponCapabilityProvider();
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
		}
		
		public Builder styleProvider(Function<LivingEntityPatch<?>, Style> styleProvider) {
			this.styleProvider = styleProvider;
			return this;
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
            Arrays.stream(conditionals).forEach(provider::addConditional);
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