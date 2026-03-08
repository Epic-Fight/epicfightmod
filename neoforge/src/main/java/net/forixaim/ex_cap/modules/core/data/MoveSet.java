package net.forixaim.ex_cap.modules.core.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.forixaim.ex_cap.modules.core.managers.MovesetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.*;
import java.util.function.BiFunction;
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
    private final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> skillSpecificGuardAnimations;
    private final Skill weaponPassiveSkill;
    private final AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
    private final Predicate<LivingEntityPatch<?>> sheathRender;
    private final BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> customMotion;
    private final Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultGuardAnimations;
    public final ResourceLocation registryIdentifier;

    public MoveSet(MoveSetBuilder builder)
    {
        registryIdentifier = builder.registryIdentifier;
        this.mountAttackAnimations = builder.mountAttackAnimations;
        this.sheathRender = builder.sheathRender;
        this.comboAttackAnimations = builder.comboAttackAnimations;
        this.livingMotionModifiers = builder.livingMotionModifiers;
        this.skillSpecificGuardAnimations = builder.skillSpecificGuardAnimations;
        this.defaultGuardAnimations = builder.defaultGuardAnimations;
        this.weaponInnateSkill = builder.weaponInnateSkill;
        this.weaponPassiveSkill = builder.weaponPassiveSkill;
        this.revelationAnimation = builder.revelationAnimation;
        this.customMotion = builder.motion;
    }

    public BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> getCustomMotion()
    {
        return customMotion;
    }

    public Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> getDefaultGuardAnimations() {
        return defaultGuardAnimations;
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

    public Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> getSkillSpecificGuardAnimations() {
        return skillSpecificGuardAnimations;
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
        protected final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> skillSpecificGuardAnimations;
        protected final Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultGuardAnimations;
        protected Skill weaponPassiveSkill;
        protected Predicate<LivingEntityPatch<?>> sheathRender;
        protected AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
        protected BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> motion;
        protected ResourceLocation parent;

        public MoveSetBuilder()
        {
            mountAttackAnimations = Lists.newArrayList();
            sheathRender = livingEntityPatch -> false;
            comboAttackAnimations = Lists.newArrayList();
            livingMotionModifiers = Maps.newHashMap();
            skillSpecificGuardAnimations = Maps.newHashMap();
            defaultGuardAnimations = Maps.newHashMap();
            motion = (a, b) -> null;
            weaponInnateSkill = null;
            weaponPassiveSkill = null;
            revelationAnimation = null;
        }

        public MoveSetBuilder parent(ResourceLocation parent)
        {
            this.parent = parent;
            return this;
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
        public final MoveSetBuilder addGuardAnimations(GuardSkill.BlockType blockType, AnimationManager.AnimationAccessor<? extends StaticAnimation>... animation)
        {
            defaultGuardAnimations.computeIfAbsent(blockType, (key) -> Lists.newArrayList()).addAll(Arrays.asList(animation));
            return this;
        }

        public final MoveSetBuilder addSkillSpecificGuardAnimations(Skill guardSkill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> animations)
        {
            if (guardSkill instanceof GuardSkill)
            {
                skillSpecificGuardAnimations.computeIfAbsent(guardSkill, (key) -> Maps.newHashMap()).putAll(animations);
            }
            return this;
        }

        private static @NotNull List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getAnimationAccessors(JsonElement jsonElement)
        {
            List<JsonElement> attacks = jsonElement.getAsJsonArray().asList();
            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> autocomboAnims = Lists.newArrayList();

            attacks.forEach(attacksElement -> {
                autocomboAnims.add(AnimationManager.byKey(attacksElement.getAsString()));
            });
            return autocomboAnims;
        }

        private static Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifiers(Map<String, JsonElement> map)
        {
            Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> result = Maps.newHashMap();
            map.forEach((key, jsonElement) -> result.put(LivingMotion.ENUM_MANAGER.get(key), AnimationManager.byKey(jsonElement.getAsString())));
            return result;
        }

        @SuppressWarnings("unchecked")
        public static MoveSetBuilder deserialize(JsonElement jsonObject) throws JsonParseException
        {
            MoveSetBuilder result = new MoveSetBuilder();
            try {
                if (jsonObject.isJsonObject())
                {
                    JsonObject json = jsonObject.getAsJsonObject();
                    if (json.has("combo_attack"))
                    {
                        result.addComboAttacks(getAnimationAccessors(json.get("combo_attack").getAsJsonArray()).toArray(AnimationManager.AnimationAccessor[]::new));
                    }
                    if (json.has("mount_attack"))
                    {
                        result.addMountAttacks(getAnimationAccessors(json.get("mount_attack").getAsJsonArray()).toArray(AnimationManager.AnimationAccessor[]::new));
                    }
                    if (json.has("innate_skill"))
                    {
                        result.addInnateSkill((itemStack, playerPatch) -> EpicFightRegistries.SKILL.get(ResourceLocation.parse(json.get("innate_skill").getAsString())));
                    }
                    if (json.has("living_animations"))
                    {
                        getLivingMotionModifiers(json.get("living_animations").getAsJsonObject().asMap()).forEach(result::addLivingMotionModifier);
                    }
                    if (json.has("weapon_passive"))
                    {
                        result.setPassiveSkill(EpicFightRegistries.SKILL.get(ResourceLocation.parse(json.get("weapon_passive").getAsString())));
                    }
                    if (json.has("guard_motions"))
                    {
                        json.get("guard_motions").getAsJsonObject().asMap().forEach((s, jsonElement) ->
                        {
                            GuardSkill.BlockType blockType = GuardSkill.BlockType.valueOf(s.toUpperCase(Locale.ROOT));
                            result.addGuardAnimations(blockType, getAnimationAccessors(jsonElement.getAsJsonArray()).toArray(AnimationManager.AnimationAccessor[]::new));
                        });
                    }
                    if (json.has("revelation_attack"))
                    {
                        result.revelationAttack(AnimationManager.byKey(json.get("revelation_attack").getAsString()));
                    }
                    if (json.has("sheath_render"))
                    {
                        result.shouldRenderSheath(livingEntityPatch -> json.get("sheath_render").getAsBoolean());
                    }
                }
            } catch (RuntimeException e) {
                throw new JsonParseException("Failed to parse moveset json: " + e.getMessage());
            }
            return result;
        }


        private MoveSetBuilder merge() {
            MoveSetBuilder result = new MoveSetBuilder();

            Deque<MoveSetBuilder> hierarchy = new ArrayDeque<>();
            MoveSetBuilder current = this;

            // Collect parent chain
            while (current != null) {
                hierarchy.push(current);
                current = MovesetManager.getBuilder(current.parent);
            }

            // Apply from root → child
            while (!hierarchy.isEmpty()) {
                MoveSetBuilder builder = hierarchy.pop();

                if (!builder.comboAttackAnimations.isEmpty()) {
                    result.comboAttackAnimations.clear();
                    result.comboAttackAnimations.addAll(builder.comboAttackAnimations);
                }

                if (!builder.mountAttackAnimations.isEmpty()) {
                    result.mountAttackAnimations.clear();
                    result.mountAttackAnimations.addAll(builder.mountAttackAnimations);
                }

                if (!builder.livingMotionModifiers.isEmpty()) {
                    result.livingMotionModifiers.putAll(builder.livingMotionModifiers);
                }

                if (!builder.skillSpecificGuardAnimations.isEmpty()) {
                    result.skillSpecificGuardAnimations.clear();
                    result.skillSpecificGuardAnimations.putAll(builder.skillSpecificGuardAnimations);
                }

                if (builder.weaponInnateSkill != null)
                    result.weaponInnateSkill = builder.weaponInnateSkill;

                if (builder.weaponPassiveSkill != null)
                    result.weaponPassiveSkill = builder.weaponPassiveSkill;

                if (builder.sheathRender != null)
                    result.sheathRender = builder.sheathRender;

                if (builder.revelationAnimation != null)
                    result.revelationAnimation = builder.revelationAnimation;

                if (builder.motion != null)
                    result.motion = builder.motion;
            }

            return result;
        }

        public MoveSet build()
        {
            return new MoveSet(merge());
        }
    }
}
