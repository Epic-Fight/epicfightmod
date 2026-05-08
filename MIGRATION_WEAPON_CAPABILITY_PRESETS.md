# Migration Guide from 1.20.1-20.14.15.1/20.14.17 -> 21.17.1
(Note: If you are migrating from using the old ExCap API see [this guide instead.](MIGRATION_OLD_EXCAP.md))
## Introduction
This guide primarily describes from migrating your WeaponCapabilityPresets to the new ExCap API.

## What is ExCap?
ExCap stands for ***Ex***tensible ***Cap***abilities. ExCap's dynamic weapon modifications were streamlined into [Weapon Modifiers](src/main/java/yesman/epicfight/api/ex_cap/data/modifier/WeaponModifier.java). Compared to `WeaponCapabilityPresets`, ExCap is a much simpler system for registering capabilities compared to using

```java
Function<Item, CapabilityItem.Builder> PRESET;
```

Compared to `WeaponCapabilityPresets` which forces you to not only use a lambda but also to force you to build weapons monolithically, perfect for beginners but terrible if you want to scale. Additionally players must hook into the `WeaponCapabilityPresetRegisterEvent` (and spam `ResourceLocations`) and it does get repetitive and tedious.

ExCap allows you to register the presets you've made in one way, simple.
1. Create your DeferredRegister for your WeaponCapabilityPreset. Registry Keys are in `EpicFightRegistries`
2. Define your Weapon Capability Preset, All you need is the builder. No fancy lambda wrapping. (See `Samples` tab)
3. Register the DeferredRegister in your mod's constructor.

Notable Things:
Please use `addMoveset` and `addConditional` instead of `styleProvider` and `newStyleCombo`. Sure they will work but not only they are `@Depreacted` but also marked for removal in the future.

ExCap gives the option to register weapons monolithically but also allows options for modularity. Including creating your own reusable movesets and conditionals. 

Both `Moveset` and `ProviderConditional` are registered in the same way.

## `Moveset` and `ProviderConditional`: The Modular Approach
The one way players can do modularity is by creating their own `Moveset` and `ProviderConditional`. Both Movesets and Conditionals have a `parent()` method that gives inheiritance allowing much better reusability. (See `Samples` tab)

## Weapon Modifiers: ExCap's Core
Weapon Modifiers are the new way to modify weapons dynamically before a world load or upon a /reload. Weapon Modfiiers allows players to add their own movesets, conditionals and potentially more.

## Custom Data
ExCap allows the registration of custom data. Either as WeaponData or as MovesetData. Currently, it's for more advanced users and usually there to give modders an easy external way to add custom data to weapons.

## Samples
Below are some samples of how to use ExCap and how they compare to WeaponCapabilityPresets

Longsword (Weapon Capability Presets)
```java
public static final Function<Item, CapabilityItem.Builder> LONGSWORD = (item) -> {
    WeaponCapability.Builder builder = WeaponCapability.builder().category(WeaponCategories.LONGSWORD)
			.styleProvider((playerpatch) -> {
				if (playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == WeaponCategories.SHIELD) {
					return Styles.ONE_HAND;
				} else if (playerpatch instanceof PlayerPatch<?> tplayerpatch) {
					return tplayerpatch.getSkill(SkillSlots.WEAPON_INNATE).isActivated() ? Styles.OCHS : Styles.TWO_HAND;
				}
				
				return Styles.TWO_HAND;
			})
			.collider(ColliderPreset.LONGSWORD)
			.canBePlacedOffhand(false)
			.newStyleCombo(Styles.ONE_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.newStyleCombo(Styles.OCHS, Animations.LONGSWORD_LIECHTENAUER_AUTO1, Animations.LONGSWORD_LIECHTENAUER_AUTO2, Animations.LONGSWORD_LIECHTENAUER_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.SHARP_STAB)
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.LIECHTENAUER)
			.innateSkill(Styles.OCHS, (itemstack) -> EpicFightSkills.LIECHTENAUER)
			.livingMotionModifier(Styles.COMMON, LivingMotions.IDLE, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.CHASE, Animations.BIPED_WALK_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.SNEAK, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.KNEEL, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.JUMP, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.SWIM, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
			.livingMotionModifier(Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.KNEEL, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.JUMP, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
			.livingMotionModifier(Styles.OCHS, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
			.addTag(EpicFightMod.identifier("longsword"));
        if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
        return builder;
};

@SubscribeEvent 
public static void registerCapabilities(WeaponCapabilityPresetRegistryEvent event) {
    event.getTypeEntry().put(EpicFightMod.identifier("longsword"), WeaponCapabilityPresets.LONGSWORD);
}
```

Longsword (ExCap)

`Registry Class`
```java
public static final ItemPresetRegister REGISTRY = ItemPresetRegister.create(EpicFight.MODID);

public static final DeferredWeapon LONGSWORD = REGISTRY.registerWeapon("longsword",
        () -> WeaponCapability.builder()
                .category(CapabilityItem.WeaponCategories.LONGSWORD)
                .collider(ColliderPreset.LONGSWORD)
                .hitSound(EpicFightSounds.BLADE_HIT)
                .canBePlacedOffhand(true)
                .setTierValues(0, 0d, 0.0, 0.0)
                .addMoveset(CapabilityItem.Styles.ONE_HAND, Moveset.builder()
                        .parent("epicfight:longsword/generated/two_hand")
                        .addLivingMotionModifier(LivingMotions.BLOCK_SHIELD, Animations.BIPED_BLOCK)
                        .addLivingMotionModifier(LivingMotions.BLOCK, Animations.BIPED_BLOCK)
                        .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.SHARP_STAB.get()))
                .addMoveset(CapabilityItem.Styles.TWO_HAND, Moveset.builder()
                        .addLivingMotionsRecursive(Animations.BIPED_HOLD_LONGSWORD,
                                LivingMotions.IDLE, LivingMotions.SNEAK, LivingMotions.KNEEL,
                                LivingMotions.JUMP, LivingMotions.SWIM)
                        .addLivingMotionsRecursive(Animations.BIPED_WALK_LONGSWORD,
                                LivingMotions.WALK, LivingMotions.CHASE)
                        .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
                        .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                        .addComboAttacks(
                                Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3,
                                Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH
                        )
                        .addMountAttacks(Animations.SWORD_MOUNT_ATTACK)
                        .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.LIECHTENAUER.get())
                        .shouldRenderSheath(livingEntityPatch -> true))
                .addMoveset(CapabilityItem.Styles.OCHS, Moveset.builder()
                        .addLivingMotionsRecursive(Animations.BIPED_HOLD_LIECHTENAUER,
                                LivingMotions.IDLE, LivingMotions.SNEAK, LivingMotions.KNEEL,
                                LivingMotions.JUMP, LivingMotions.SWIM)
                        .addLivingMotionsRecursive(Animations.BIPED_WALK_LIECHTENAUER,
                                LivingMotions.WALK, LivingMotions.CHASE)
                        .addLivingMotionModifier(LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
                        .addLivingMotionModifier(LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
                        .addComboAttacks(
                                Animations.LONGSWORD_LIECHTENAUER_AUTO1, Animations.LONGSWORD_LIECHTENAUER_AUTO2, Animations.LONGSWORD_LIECHTENAUER_AUTO3,
                                Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH
                        )
                        .addMountAttacks(Animations.SWORD_MOUNT_ATTACK)
                        .addInnateSkill((itemStack, playerPatch) -> EpicFightSkills.LIECHTENAUER.get()))
                .addConditionals(ProviderConditional.createDefault(CapabilityItem.Styles.TWO_HAND, false), ProviderConditional.createWeaponCategory(CapabilityItem.Styles.ONE_HAND, CapabilityItem.WeaponCategories.SHIELD, InteractionHand.OFF_HAND, true), ProviderConditional.createSkillCondition(CapabilityItem.Styles.OCHS, EpicFightSkills.LIECHTENAUER, SkillSlots.WEAPON_INNATE, true, false))
                .addTag(EpicFight.identifier("longsword"))
);
```
`Mod Constructor`
```java
RegistryClassName.REGISTRY.register(modEventBus);
```

A 7-line reduction, and 150 fewer characters.
If done modularly, the new setup would've looked like this after moving movesets and conditionals to their own files.

```java
public static final DeferredWeapon LONGSWORD = REGISTRY.registerWeapon("longsword",
            () -> WeaponCapability.builder()
                    .category(CapabilityItem.WeaponCategories.LONGSWORD)
                    .collider(ColliderPreset.LONGSWORD)
                    .hitSound(EpicFightSounds.BLADE_HIT)
                    .canBePlacedOffhand(true)
                    .setTierValues(0, 0d, 0.0, 0.0)
                    .addMoveset(CapabilityItem.Styles.ONE_HAND, EpicFightMovesets.LONGSWORD_1H)
                    .addMoveset(CapabilityItem.Styles.TWO_HAND, EpicFightMovesets.LONGSWORD_2H)
                    .addMoveset(CapabilityItem.Styles.OCHS, EpicFightMovesets.LIECHTENAUER)
                    .addConditionals(EpicFightProviderConditionals.LIECHTENAUER_CONDITION, EpicFightProviderConditionals.DEFAULT_2H_WIELD_STYLE, EpicFightProviderConditionals.SHIELD_OFFHAND)
                    .addTag(EpicFight.identifier("longsword"))
    );
```

The Modular approach is only a mere 13 lines of code and nearly cuts the character count by 70%

Parenting can get a functional weapon in just one or two lines. Allowing you to just make new movesets and conditionals and inherit from the parent.
```java
    public static final DeferredWeapon BOKKEN = REGISTRY.registerWeapon("bokken", () ->
            WeaponCapability.builder()
                    .parent(SWORD)
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
                    .addConditionals(ProviderConditional.createSkillCondition(CapabilityItem.Styles.SHEATH, EpicFightSkills.SWORD_MASTER,
                            SkillSlots.PASSIVE1, false, false))
                    .addTag(EpicFight.identifier("bokken")));
```
Or if you chose to do it modularly, or a hybrid approach.
```java
public static final DeferredWeapon BOKKEN = REGISTRY.registerWeapon("bokken", () ->
        WeaponCapability.builder()
                .parent(SWORD)
                .addMoveset(CapabilityItem.Styles.SHEATH, EpicFightMovesets.TACHI_2H)
                .addConditionals(ProviderConditional.createSkillCondition(CapabilityItem.Styles.SHEATH, EpicFightSkills.SWORD_MASTER,
                        SkillSlots.PASSIVE1, false, false))
                .addTag(EpicFight.identifier("bokken")));
```