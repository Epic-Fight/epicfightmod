package yesman.epicfight.api.ex_cap.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.data.modifier.RenderModifier;
import yesman.epicfight.api.ex_cap.managers.MovesetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.deferred.holders.DeferredCustomData;
import yesman.epicfight.registry.deferred.holders.DeferredMoveset;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Moveset
{
    private final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAttackAnimations;
    private final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> mountAttackAnimations;
    private final Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingMotionModifiers;
    private final BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill;
    private final Map<Skill, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardPoses;
    private final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> skillSpecificGuardAnimations;
    private final Holder<Skill> weaponPassiveSkill;
    private final Map<Holder<CustomData<?>>, Object> customData;
    private final AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
    private final Predicate<LivingEntityPatch<?>> sheathRender;
    private final BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> customMotion;
    private final Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultGuardAnimations;
    private final RenderModifier modifier;

    public Moveset(Builder builder)
    {
        this.mountAttackAnimations = builder.mountAttackAnimations;
        this.sheathRender = builder.sheathRender;
        this.comboAttackAnimations = builder.comboAttackAnimations;
        this.livingMotionModifiers = builder.livingMotionModifiers;
        this.skillSpecificGuardAnimations = builder.skillSpecificGuardAnimations;
        this.guardPoses = builder.guardPoses;
        this.customData = ImmutableMap.copyOf(builder.customData);
        this.modifier = builder.modifier;
        this.defaultGuardAnimations = builder.defaultGuardAnimations;
        this.weaponInnateSkill = builder.weaponInnateSkill;
        this.weaponPassiveSkill = builder.weaponPassiveSkill;
        this.revelationAnimation = builder.revelationAnimation;
        this.customMotion = builder.motion;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCustomData(Holder<CustomData<T>> holder)
    {
        try {
            return (T) customData.get(holder);
        } catch (Exception e) {
            return null;
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(DeferredCustomData<CustomData<T>> holder)
    {
        try {
            return (T) customData.get(holder);
        } catch (Exception e) {
            return null;
        }
    }

    public RenderModifier getRenderModifier() {
        return modifier;
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

    public static Builder builder()
    {
        return new Builder();
    }

    public Holder<Skill> getWeaponPassiveSkill() {
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

    public Map<Skill, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> getGuardPoses() {
        return guardPoses;
    }

    public Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifiers() {
        return livingMotionModifiers;
    }



    /**
     * A fluent builder for creating {@link Moveset} instances.
     * <p>
     * This builder defines the animations, skills, and behavioral logic associated with a specific
     * weapon style. It supports a hierarchical inheritance system via {@link #parent(ResourceLocation)},
     * allowing builders to override or extend properties from existing movesets.
     * </p>
     * <h3>Key Features:</h3>
     * <ul>
     * <li><b>Animation Overriding:</b> Define custom poses for specific {@link LivingMotion} types.</li>
     * <li><b>Combat Logic:</b> Configure combo attacks, mount attacks, and innate skills.</li>
     * <li><b>Dynamic Predicates:</b> Use {@link #setMotionPredicate} to swap motions based on entity state.</li>
     * </ul>
     */
    public static class Builder
    {
        protected final Map<Holder<CustomData<?>>, Object> customData;
        protected final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> comboAttackAnimations;
        protected final List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> mountAttackAnimations;
        protected final Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> livingMotionModifiers;
        protected BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill;
        protected final Map<Skill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> skillSpecificGuardAnimations;
        protected final Map<Skill, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> guardPoses;
        protected final Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> defaultGuardAnimations;
        protected Holder<Skill> weaponPassiveSkill;
        protected Predicate<LivingEntityPatch<?>> sheathRender;
        protected AnimationManager.AnimationAccessor<? extends AttackAnimation> revelationAnimation;
        protected BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> motion;
        protected ResourceLocation parent;
        protected RenderModifier modifier;

        public Builder()
        {
            mountAttackAnimations = Lists.newArrayList();
            sheathRender = livingEntityPatch -> false;
            comboAttackAnimations = Lists.newArrayList();
            livingMotionModifiers = Maps.newHashMap();
            skillSpecificGuardAnimations = Maps.newHashMap();
            defaultGuardAnimations = Maps.newHashMap();
            guardPoses = Maps.newHashMap();
            modifier = null;
            motion = (a, b) -> null;
            weaponInnateSkill = null;
            weaponPassiveSkill = null;
            revelationAnimation = null;
            this.customData = Maps.newHashMap();
        }

        /**
         * Sets the parent moveset to inherit properties from.
         * <p>
         * During the {@link #build()} process, the system will recursively merge properties from
         * the parent hierarchy. Local definitions in this builder will override parent values.
         * </p>
         * @param parent The {@link ResourceLocation} of the parent moveset.
         * @return This builder for chaining.
         */
        public Builder parent(ResourceLocation parent)
        {
            this.parent = parent;
            return this;
        }


        public void addCustomData(Map<DeferredCustomData<? extends CustomData<?>>, Object> dataMap)
        {
            customData.putAll(dataMap);
        }

        public void registerCustomData(Holder<CustomData<?>> holder)
        {
            this.customData.put(holder, holder.value().get());
        }

        public Builder parent(DeferredMoveset moveset)
        {
            return this.parent(moveset.getId());
        }

        public Builder renderModifier(RenderModifier modifier)
        {
            this.modifier = modifier;
            return this;
        }

        public Builder setMotionPredicate(BiFunction<LivingEntityPatch<?>, InteractionHand, LivingMotion> lambda)
        {
            this.motion = lambda;
            return this;
        }

        public <T> Builder setCustomData(DeferredCustomData<? extends CustomData<T>> data, T customData)
        {
            if (this.customData.containsKey(data))
            {
                this.customData.put(data, customData);
            }
            return this;
        }

        public Builder revelationAttack(AnimationManager.AnimationAccessor<? extends AttackAnimation> attack)
        {
            revelationAnimation = attack;
            return this;
        }

        /**
         * Sets a predicate to determine when the weapon's sheath should be rendered.
         * @param sheathRender A predicate returning true if the sheath should be visible.
         * @return This builder for chaining.
         */
        public Builder shouldRenderSheath(Predicate<LivingEntityPatch<?>> sheathRender)
        {
            this.sheathRender = sheathRender;
            return this;
        }

        @SafeVarargs
        public final Builder guardSpecificHold(Skill skill, AnimationManager.AnimationAccessor<? extends StaticAnimation>... animations)
        {
            if (skill instanceof GuardSkill)
            {
                guardPoses.computeIfAbsent(skill, (k) -> Lists.newArrayList(animations));
            }
            return this;
        }

        /**
         * Assigns a passive skill to the moveset.
         * <p>
         * Passive skills are generally used for persistent effects or stance-based logic
         * (e.g., the Battojutsu passive for Uchigatanas) that do not require active
         * key presses to function but are tied to the weapon's presence.
         * </p>
         * @param newPassiveSkill The {@link Skill} to be applied as a passive effect.
         * @return This builder for chaining.
         */
        public Builder setPassiveSkill(Holder<Skill> newPassiveSkill)
        {
            this.weaponPassiveSkill = newPassiveSkill;
            return this;
        }

        /**
         * Adds animations specifically for mounted combat.
         * <p>
         * These animations are triggered when the player is riding an entity (e.g., a horse).
         * If no mount attacks are defined, the system may fall back to default or parent
         * animations depending on the hierarchy.
         * </p>
         * @param attackAnimations A varargs list of {@link AttackAnimation} accessors for mounted play.
         * @return This builder for chaining.
         */
        @SafeVarargs
        public final Builder addMountAttacks(AnimationManager.AnimationAccessor<? extends AttackAnimation>... attackAnimations)
        {
            mountAttackAnimations.addAll(Arrays.asList(attackAnimations));
            return this;
        }

        /**
         * Defines a set of animations for standard ground combat.
         * @param attackAnimations A sequence of animations used for the weapon's auto-combo.
         * @return This builder for chaining.
         */
        @SafeVarargs
        public final Builder addComboAttacks(AnimationManager.AnimationAccessor<? extends AttackAnimation>... attackAnimations)
        {
            comboAttackAnimations.addAll(Arrays.asList(attackAnimations));
            return this;
        }

        /**
         * Clears any existing combo animations and replaces them with the provided sequence.
         * Useful for modifiers that want to completely overhaul a moveset rather than append to it.
         * @param attackAnimations The new sequence of animations.
         * @return This builder for chaining.
         */
        @SafeVarargs
        public final Builder replaceComboAttacks(AnimationManager.AnimationAccessor<? extends AttackAnimation>... attackAnimations) {
            this.comboAttackAnimations.clear(); // The magic line
            this.comboAttackAnimations.addAll(Arrays.asList(attackAnimations));
            return this;
        }

        /**
         * Maps a specific {@link LivingMotion} (e.g., IDLE, WALK, RUN) to a static animation.
         * @param livingMotion The motion state to modify.
         * @param animation The animation to play during this state.
         * @return This builder for chaining.
         */
        public Builder addLivingMotionModifier(LivingMotion livingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation> animation)
        {
            livingMotionModifiers.put(livingMotion, animation);
            return this;
        }

        /**
         * Assigns a skill that is triggered by the weapon's innate activation key.
         * @param weaponInnateSkill A function providing the skill based on the current {@link ItemStack} and {@link PlayerPatch}.
         * @return This builder for chaining.
         */
        public Builder addInnateSkill(BiFunction<ItemStack, PlayerPatch<?>, Skill> weaponInnateSkill)
        {
            this.weaponInnateSkill = weaponInnateSkill;
            return this;
        }

        /**
         * A convenience method to apply a single animation to multiple {@link LivingMotion} states.
         * <p>
         * This is the preferred way to set up "Pose Sets" (e.g., applying a specific weapon-hold
         * animation to IDLE, WALK, and SNEAK simultaneously). It significantly reduces
         * boilerplate code when multiple movement states share the same visual pose.
         * </p>
         * * @param animation The {@link AnimationManager.AnimationAccessor} to be applied.
         * @param motions   A varargs list of {@link LivingMotion} states that should
         * trigger this animation.
         * @return This builder for chaining.
         */
        public Builder addLivingMotionsRecursive(AnimationManager.AnimationAccessor<? extends StaticAnimation> animation, LivingMotion... motions)
        {
            for (LivingMotion livingMotion : motions)
            {
                livingMotionModifiers.put(livingMotion, animation);
            }
            return this;
        }

        /**
         * Adds default animations for guarding/blocking based on the block type.
         * <p>
         * Use this to define the standard visual feedback for different types of blocks
         * (e.g., {@code GUARD}, {@code PARRY}, or {@code IMPACT}). These serve as the
         * baseline animations when no skill-specific override is present.
         * </p>
         * @param blockType  The {@link GuardSkill.BlockType} being configured.
         * @param animation  A varargs list of {@link StaticAnimation} accessors to play for this block.
         * @return This builder for chaining.
         */
        @SafeVarargs
        public final Builder addGuardAnimations(GuardSkill.BlockType blockType, AnimationManager.AnimationAccessor<? extends StaticAnimation>... animation)
        {
            defaultGuardAnimations.computeIfAbsent(blockType, (key) -> Lists.newArrayList()).addAll(Arrays.asList(animation));
            return this;
        }

        /**
         * Assigns specialized guard animations that only trigger when a specific skill is active.
         * <p>
         * This allows for "Stance-Specific" or "Skill-Specific" defense logic. For example,
         * a weapon might use standard guard animations normally, but switch to unique
         * "Liechtenauer" parry animations when that specific guard skill is equipped.
         * </p>
         * @param guardSkill  The {@link Skill} (must be an instance of {@link GuardSkill}) that triggers these animations.
         * @param animations  A map linking {@link GuardSkill.BlockType}s to their respective animation lists.
         * @return This builder for chaining.
         */
        public final Builder addSkillSpecificGuardAnimations(Skill guardSkill, Map<GuardSkill.BlockType, List<AnimationManager.AnimationAccessor<? extends StaticAnimation>>> animations)
        {
            if (guardSkill instanceof GuardSkill)
            {
                skillSpecificGuardAnimations.computeIfAbsent(guardSkill, (key) -> Maps.newHashMap()).putAll(animations);
            }
            return this;
        }

        @ApiStatus.Internal
        private static @NotNull List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> getAnimationAccessors(JsonElement jsonElement)
        {
            List<JsonElement> attacks = jsonElement.getAsJsonArray().asList();
            List<AnimationManager.AnimationAccessor<? extends AttackAnimation>> autocomboAnims = Lists.newArrayList();

            attacks.forEach(attacksElement -> autocomboAnims.add(AnimationManager.byKey(attacksElement.getAsString())));
            return autocomboAnims;
        }

        private static Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> getLivingMotionModifiers(Map<String, JsonElement> map)
        {
            Map<LivingMotion, AnimationManager.AnimationAccessor<? extends StaticAnimation>> result = Maps.newHashMap();
            map.forEach((key, jsonElement) -> result.put(LivingMotion.ENUM_MANAGER.get(key), AnimationManager.byKey(jsonElement.getAsString())));
            return result;
        }

        @ApiStatus.Internal
        @SuppressWarnings("unchecked")
        public static Builder deserialize(JsonElement jsonObject) throws JsonParseException
        {
            Builder result = new Builder();
            try {
                if (jsonObject.isJsonObject())
                {
                    JsonObject json = jsonObject.getAsJsonObject();
                    if (json.has("combo_attack"))
                    {
                        result.addComboAttacks(getAnimationAccessors(json.get("combo_attack").getAsJsonArray()).toArray(AnimationManager.AnimationAccessor[]::new));
                    }
                    if (json.has("guard_holds"))
                    {
                        JsonElement get = json.get("guard_holds");
                        if (get.isJsonObject())
                        {
                            get.getAsJsonObject().entrySet().forEach(entry -> {
                                Skill guard = EpicFightRegistries.SKILL.get(ResourceLocation.parse(entry.getKey()));
                                List<AnimationManager.AnimationAccessor<? extends StaticAnimation>> animations = Lists.newArrayList();
                                if (entry.getValue().isJsonArray())
                                {
                                    JsonArray array = entry.getValue().getAsJsonArray();
                                    array.forEach(element -> {
                                        if (element.isJsonPrimitive())
                                        {
                                            animations.add(AnimationManager.byKey(element.getAsJsonPrimitive().getAsString()));
                                        }
                                    });
                                }
                                if (guard != null)
                                {
                                    result.guardPoses.put(guard, animations);
                                }
                            });
                        }
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
                        result.setPassiveSkill(EpicFightRegistries.SKILL.getHolder(ResourceLocation.parse(json.get("weapon_passive").getAsString())).get());
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


        @ApiStatus.Internal
        private Builder merge() {
            Builder result = new Builder();

            Deque<Builder> hierarchy = new ArrayDeque<>();
            Builder current = this;

            while (current != null) {
                hierarchy.push(current);
                current = MovesetManager.getBuilder(current.parent);
            }

            while (!hierarchy.isEmpty()) {
                Builder builder = hierarchy.pop();

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
                    result.skillSpecificGuardAnimations.putAll(builder.skillSpecificGuardAnimations);
                }

                if (!builder.defaultGuardAnimations.isEmpty()) {
                    result.defaultGuardAnimations.putAll(builder.defaultGuardAnimations);
                }

                if (!builder.guardPoses.isEmpty()) {
                    result.guardPoses.putAll(builder.guardPoses);
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

        public Moveset build()
        {
            return new Moveset(merge());
        }
    }
}
