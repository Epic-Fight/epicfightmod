# Migration Guide for 21.16.4 to 21.17.1
## Introduction
This migration guide is for developers using the 21.16.4 version of the mod meant to transition from the old event-driven API to the new registry-driven API.
There are a lot of changes to the public API that involve registering new weapon types.

## From Event Driven to Registry Driven
Note that the old event-driven API is no longer supported. It's still there and functional to give time to migrate, but it would be better to use the new registry-driven API.

This registry-driven API uses the same registration process as any normal NeoForged registration for basic things like Item and Block. Use `DeferredRegsiter<>` for registering any of the features.

Entire weapons can be made via DeferredRegister. Check out in the Samples section for an example.

Make sure to check ProviderConditonal for built-in helper methods that can be used to make the registration easier.

In a way the old event-driven API forced modularity, this new API allows for the option to register a weapon monolithically, which is easier for newcomers to understand. For Epic Fight, these are registered modularly. Modular registration allows for reusability and better scaling.

| Old Feature (21.16.4)                                                                     | New Feature (21.17.1)                                                                           | Note                                                                 |
|:------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------|
| **Event-Driven API**                                                                      | **Registry-Driven API**                                                                         | Now uses `DeferredRegister` (NeoForge standard).                     |
| [`ExCapData`](src/main/java/yesman/epicfight/api/ex_cap/modules/core/data/ExCapData.java) | [`WeaponModifier`](src/main/java/yesman/epicfight/api/ex_cap/data/modifier/WeaponModifier.java) | Legacy support remains but is marked `@Deprecated`.                  |
| `hitSound(SoundEvent)`                                                                    | `hitSound(Holder<SoundEvent>)`                                                                  | Modified to prevent `NullPointerException` during init.              |
| Forced Modularity                                                                         | **Monolithic or Modular**                                                                       | You now have the choice to register weapons all-at-once or in parts. |

## Other Changes
- Select `WeaponCapability.Builder` methods has been tweaked and deprecated.
  - `hitSound(SoundEvent)` has been replaced with `hitSound(Holder<SoundEvent>)` allowing for safe checking.
  - The same applies to swingSound and hitParticle. These have all been modified to use the holder to avoid early resolution causing `NullPointerExceptions`
## Retirements
- `ExCapData` is no longer supported and kept for backwards compatibility. It's currently marked `@Deprecated`
  - The replacement is `WeaponModifier` which is done via DeferredRegister. See the example below.

## Samples

Here's a modifier registration example:
```java
public static final ModifierRegister REGISTRY = ModifierRegister.create(EpicFight.MODID);

    public static final DeferredModifier BOKKEN = REGISTRY.registerModifier("bokken", () -> WeaponModifier.builder()
            .target(EpicFightItemCapabilityPresets.BOKKEN)
            .addConditionalModifier(ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.BERSERKER, SkillSlots.PASSIVE1, false, false))
            .addMovesetModifier(CapabilityItem.Styles.OCHS, Moveset.builder()
                    .addLivingMotionsRecursive(Animations.BIPED_HOLD_GREATSWORD,
                            LivingMotions.IDLE, LivingMotions.JUMP, LivingMotions.KNEEL, LivingMotions.SNEAK,
                            LivingMotions.SWIM, LivingMotions.FLY, LivingMotions.CREATIVE_FLY, LivingMotions.CREATIVE_IDLE)
                    .addLivingMotionsRecursive(Animations.BIPED_WALK_GREATSWORD,
                            LivingMotions.WALK, LivingMotions.CHASE)
                    .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
                    .addLivingMotionModifier(LivingMotions.BLOCK, Animations.GREATSWORD_GUARD)
                    .addComboAttacks(
                            Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2,
                            Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH
                    )
                    .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.STEEL_WHIRLWIND.get())));
```

Here's an example of making a bokken (Note: that this is done monolithically, you can still register it modularly by reusing movesets)
```java
    public static final ItemPresetRegister REGISTRY = ItemPresetRegister.create(EpicFight.MODID);


public static final DeferredWeapon BOKKEN = REGISTRY.registerWeapon("bokken", () ->
            WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.SWORD)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .collider(ColliderPreset.SWORD)
                    .setTierValues(0, 0, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.SWORD_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.SWORD_DUAL)
                    .addConditionals(EpicFightProviderConditionals.DUAL_SWORDS, EpicFightProviderConditionals.DEFAULT_1H_WIELD_STYLE)
                    /* Monolithic Registration Example */
                    .addMoveset(CapabilityItem.Styles.SHEATH, Moveset.builder()
                            .addLivingMotionsRecursive(Animations.BIPED_HOLD_TACHI,
                                    LivingMotions.IDLE, LivingMotions.KNEEL, LivingMotions.WALK, LivingMotions.CHASE, LivingMotions.RUN,
                                    LivingMotions.SNEAK, LivingMotions.SWIM, LivingMotions.FLOAT, LivingMotions.FALL)
                            .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                            .addComboAttacks(
                                    Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3,
                                    Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH
                            )
                            .addMountAttacks(Animations.SWORD_MOUNT_ATTACK)
                            .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.RUSHING_TEMPO.get()))
                    .addConditional(ProviderConditional.createSkillCondition(CapabilityItem.Styles.SHEATH, EpicFightSkills.SWORD_MASTER,
                            SkillSlots.PASSIVE1, false, false))
                    /* End Monolithic Registration Example */
                    .addTag(EpicFight.identifier("sword")));
```
Here's an example of making a moveset this is for the base greatsword moveset.
```java
public static final MovesetRegister REGISTRY = MovesetRegister.create(EpicFight.MODID);

    public static final DeferredMoveset GREATSWORD_2H = REGISTRY.registerMoveset("greatsword_2h",
            () -> Moveset.builder()
                    .addLivingMotionsRecursive(Animations.BIPED_HOLD_GREATSWORD,
                            LivingMotions.IDLE, LivingMotions.JUMP, LivingMotions.KNEEL, LivingMotions.SNEAK,
                            LivingMotions.SWIM, LivingMotions.FLY, LivingMotions.CREATIVE_FLY, LivingMotions.CREATIVE_IDLE)
                    .addLivingMotionsRecursive(Animations.BIPED_WALK_GREATSWORD,
                            LivingMotions.WALK, LivingMotions.CHASE)
                    .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
                    .addLivingMotionModifier(LivingMotions.BLOCK, Animations.GREATSWORD_GUARD)
                    .addComboAttacks(
                            Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2,
                            Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH
                    )
                    .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.STEEL_WHIRLWIND.get())
    );
```
Here's an example of registering a reusable conditional:
```java
public static final ProviderConditionalRegister REGISTRY = ProviderConditionalRegister.create(EpicFight.MODID);

    // One-handed wield style that is created as a default
    public static final DeferredConditional DEFAULT_1H_WIELD_STYLE = REGISTRY.registerConditional(
            "default_1h_wield_style",
            () -> ProviderConditional.createDefault(CapabilityItem.Styles.ONE_HAND, true)
    );
```

Remember, check ProviderConditional for helper methods to make the registration easier.