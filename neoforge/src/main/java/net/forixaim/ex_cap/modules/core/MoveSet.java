package net.forixaim.ex_cap.modules.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MoveSet 
{
    //TODO: Deal with this later...
    //private static final HashMultimap<Class<?>, MoveSet> MOVESETS = HashMultimap.create();
    //private static final ResourceLocation CLASS_TO_MOVESET = ResourceLocation.fromNamespaceAndPath(EpicFightEXCapability.MODID, "class_to_moveset");
    //private static final ResourceLocation MOVESET_TO_ID = ResourceLocation.fromNamespaceAndPath(EpicFightEXCapability.MODID, "moveset_to_id");
    //public static final ResourceKey<Registry<MoveSet>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(EpicFightEXCapability.MODID, "moveset"));

    private final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAttackAnimations;
    private final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> mountAttackAnimations;
    private final Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingMotionModifiers;
    private final BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill;
    private final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> guardAnimations;
    private final Skill weaponPassiveSkill;
    private final AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
    private final Predicate<LivingEntityPatch<?>> sheathRender;
    private final BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> customMotion;

    public final ResourceLocation registryIdentifier;

    public MoveSet(MoveSetBuilder builder)
    {
        registryIdentifier = builder.registryIdentifier;
        this.mountAttackAnimations = builder.mountAttackAnimations;
        this.sheathRender = builder.sheathRender;
        this.comboAttackAnimations = builder.comboAttackAnimations;
        this.livingMotionModifiers = builder.livingMotionModifiers;
        this.guardAnimations = builder.guardAnimations;
        this.weaponInnateSkill = builder.weaponInnateSkill;
        this.weaponPassiveSkill = builder.weaponPassiveSkill;
        this.revelationAnimation = builder.revelationAnimation;
        this.customMotion = builder.motion;
    }

    public BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> getCustomMotion()
    {
        return customMotion;
    }

    public AnimationManager.AnimationAccessor<? extends AttackAnimation> getRevelation()
    {
        return revelationAnimation;
    }

    public Predicate<LivingEntityPatch<?>> shouldRenderSheath()
    {
        return sheathRender;
    }

    public static MoveSetBuilder builder()
    {
        return new MoveSetBuilder();
    }

    public Skill getWeaponPassiveSkill() {
        return weaponPassiveSkill;
    }

    public List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getMountAttackAnimations()
    {
        return mountAttackAnimations;
    }

    public BiFunction<ItemStack, PlayerPatch<?>, Skill> getWeaponInnateSkill() {
        return weaponInnateSkill;
    }

    public Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>>  getGuardAnimations() {
        return guardAnimations;
    }

    public List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getComboAttackAnimations() {
        return comboAttackAnimations;
    }

    public Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifiers() {
        return livingMotionModifiers;
    }

    /**
     * Allows for
     */
    public static class MoveSetBuilder
    {
        protected ResourceLocation registryIdentifier;
        protected final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAttackAnimations;
        protected final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> mountAttackAnimations;
        protected final Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingMotionModifiers;
        protected BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill;
        protected final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> guardAnimations;
        protected Skill weaponPassiveSkill;
        protected Predicate<LivingEntityPatch<?>> sheathRender;
        protected AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
        protected BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> motion;

        public MoveSetBuilder()
        {
            mountAttackAnimations = Lists.newArrayList();
            sheathRender = livingEntityPatch -> false;
            comboAttackAnimations = Lists.newArrayList();
            livingMotionModifiers = Maps.newHashMap();
            guardAnimations = Maps.newHashMap();
            motion = (a, b) -> null;
            weaponInnateSkill = null;
            weaponPassiveSkill = null;
            revelationAnimation = null;
        }

        public MoveSetBuilder identifier(ResourceLocation identifier)
        {
            this.registryIdentifier = identifier;
            return this;
        }

        public MoveSetBuilder setMotionPredicate(BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> lambda)
        {
            this.motion = lambda;
            return this;
        }

        public MoveSetBuilder revelationAttack(AnimationManager.AnimationAccessor<? extends AttackAnimation> attack)
        {
            revelationAnimation = attack;
            return this;
        }

        public MoveSetBuilder shouldRenderSheath(Predicate<LivingEntityPatch<?>> sheathRender)
        {
            this.sheathRender = sheathRender;
            return this;
        }

        public MoveSetBuilder setPassiveSkill(Skill newPassiveSkill)
        {
            this.weaponPassiveSkill = newPassiveSkill;
            return this;
        }

        @SafeVarargs
        public final MoveSetBuilder addMountAttacks(AnimationManager.AnimationAccessor<? extends AttackAnimation>... attackAnimations)
        {
            mountAttackAnimations.addAll(Arrays.asList(attackAnimations));
            return this;
        }

        public MoveSetBuilder addComboAttacks(AnimationManager.AnimationAccessor<? extends AttackAnimation>... attackAnimations)
        {
            comboAttackAnimations.addAll(Arrays.asList(attackAnimations));
            return this;
        }

        public MoveSetBuilder addLivingMotionModifier(LivingMotion livingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation)
        {
            livingMotionModifiers.put(livingMotion, animation);
            return this;
        }

        public MoveSetBuilder addInnateSkill(BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill)
        {
            this.weaponInnateSkill = weaponInnateSkill;
            return this;
        }

        public MoveSetBuilder addLivingMotionsRecursive(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, LivingMotion... motions)
        {
            for (LivingMotion livingMotion : motions)
            {
                livingMotionModifiers.put(livingMotion, animation);
            }
            return this;
        }

        @SafeVarargs
        public final MoveSetBuilder addGuardAnimations(Skill guardSkill, GuardSkill.BlockType blockType, AnimationManager.AnimationAccessor<? extends StaticAnimation>... animation)
        {
            if (guardSkill instanceof GuardSkill)
            {
                guardAnimations.computeIfAbsent((GuardSkill) guardSkill, (guardSkill1 -> Maps.newHashMap())).computeIfAbsent(blockType, blockType1 -> Lists.newArrayList()).addAll(Arrays.asList(animation));
            }

            return this;
        }

        public MoveSetBuilder easyAddGuardAnimations(Skill guardSkill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> animations)
        {
            animations.forEach((blockType, animation) -> this.addGuardAnimations(guardSkill, blockType, animation.toArray(new AnimationManager.AnimationAccessor[0])));
            return this;
        }

        public MoveSet build()
        {
            return new MoveSet(this);
        }
    }
}
