# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### For Devs

- Deprecated `EpicFightMod.rl` and added `EpicFightMod.identifier` since
  [Mojang renamed `ResourceLocation` to
  `Identifier` in 1.21.11](https://neoforged.net/news/21.11release/#renaming-of-resourcelocation-to-identifier).

## [20.14.3] - 2025-12-11

### Fixed

- Fixed datapack animations not loading properly

### Changed

- Changed the asset license to All Rights Reserved, whereas the source code license keeps GNU GPLv3.
    - This means, we still allow forks of our project, but they're required to replace assets into custom and
      their original-made ones to redistribute.
    - You're still allowed to use assets via datapack editor or addons. Nevertheless, we can claim copyright
      of our assets if we find some abuse, or your behaviors that don't respect our efforts on it.

## [20.14.2] - 2025-12-10

### Fixed

- Fixed the target indicator invisible for non Epic Fight patched entities
- Fixed the lock-on target not being synced to the server when using mouse snap to change the target

### Changed

- Now Lock-on automatically searches a new target if there is no currently focusing entity
- Now TPS mode applies 8 directional movement to the player

## [20.14.1] - 2025-12-07

### Added

- Added a client config to allow skipping the third-person front perspective when
  toggling the camera perspective (i.e., F5).
  [#2205](https://github.com/Epic-Fight/epicfight/issues/2205)
- Improved arrow key navigation (`↑`, `↓`, `→`, `←`) in the skill editor screen, including proper scrolling support. [#2203](https://github.com/Epic-Fight/epicfight/issues/2203)
- **[Controlify]** Added native controller support for the skill editor screen and disabled virtual mouse behavior.
- An option that you can always activate the TPS perspective, which was only activated when aiming with ranged weapons.
- An option screen where you can set up the camera position in TPS perspective
- An auto-tracking functionality that aligns the player's look to the crosshair when aiming and striking entities.
- A new keybind that moves camera freely while locking on any entity to search another target
- A lock-on snapping feature that cycles lock-on entities in the screen by snapping mouse left or right
- An auto-targeting functionality that searches a next target when the current lock-on entity is dead
- An option to toggle lock-on snapping and auto target
- An option to set the maximum distance that the player can focus entities
- See the devlog [here](https://www.patreon.com/posts/tps-camera-and-141028682)
- Epic Fight's TPS perspective will be automatically disabled when a conflicting mod, such
  as [Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)
  or [Better Third Person](https://modrinth.com/mod/better-third-person), is detected to prevent issues.
- Explicit Shoulder Surfing compatibility with the new enhanced lock-on (credit [Exopandora](https://github.com/Exopandora)).

### Fixed

- Fixed a bug that allowed the player to replace the current skill slot even during cooldown.
- Fixed the mining crosshair not to show in vanilla mode
- Fixed the massive memory consume on loading the game caused by animation loads

### For Devs

- New API feature: Events
    - Replaces the mod-loader event system into Epic Fight API, as we planning to support multi-loader developer environment
    - The feautre is still WIP, supporting only events for EpicFightCameraAPI
    - We will eventually replace all Forge/Neoforge events owned by Epic Fight into Events
- Rename the experimental enum `EpicFightInputActions` to `EpicFightInputAction` to follow Java naming
  conventions. [#2194](https://github.com/Epic-Fight/epicfight/issues/2194)
- Removed AirAttack and its related fields (SkillCategory, SkillSlot) to merge air slash and combo attacks as one skill
- Updated the experimental Epic Fight's input API to support using custom input actions that are not a
  `EpicFightInputAction`.
  [#2194](https://github.com/Epic-Fight/epicfight/issues/2194)
- Deprecated `ClientEngine#isBattleMode` and added `ClientEngine#isEpicFightMode` for a smoother migration when porting
  from MC 1.20.1 to 1.21.1
- Added API JAR file, which includes classes under `yesman/epicfight/api/**` only, to allow consumers to compile against
  Epic Fight public API only.
    - **Note:** Keep in mind that Epic Fight public APIs are still being stabilized, and breaking changes may occur.

## [20.13.6] - 2025-11-12

### Fixed

- Fixed a regression where the weapon’s innate skill tooltip did not trigger.
  [#2198](https://github.com/Epic-Fight/epicfight/pull/2198)

## [20.13.5] - 2025-11-11

### Fixed

- Fixed a regression where the Phantom Ascent skill was triggered when
  pressing the jump key while any screen was open (e.g., inventory, chat).
  [#2170](https://github.com/Epic-Fight/epicfight/issues/2170)
- Fixed a crash when right click some blocks from Supplementaries
  [#2187](https://github.com/Epic-Fight/epicfight/issues/2187)
- Fixed patron capes always being default
- Fixed an unintended mechanism where you weren't able to attack in Epic Fight
  mode while Preference Work is set to Switch Mode, so it works in the same way
  as Item Auto Switching option, an old config where automatically sets player
  mode depending on the item that player holds

### Changed

- Refactored the code to eliminate unnecessary native GLFW calls,
  optimizing whether key down checks are performed per tick, by adapting
  Minecraft vanilla `KeyMapping`, which may also potentially fix other compatibility issues with other mods.

### Added

- Built-in Controlify integration for controller support.
  No need to install
  [Epic Fight: Controlify](https://www.curseforge.com/minecraft/mc-mods/epic-fight-controlify) anymore.
  Install only
  [Controlify: Forgified](https://www.curseforge.com/minecraft/mc-mods/controlify-forgified-unofficial) on 1.20.1

### For Devs

- Adjusted `MixinWitherBoss` to follow best practices and prevent potential conflicts with other mods in production
  environments.

## [20.13.4] - 2025-11-04

### Changed
- Updated the default config to disable the mine block highlight guide.

### Fixed
- Fixed a crash when joining a world with BadOptimizations installed. [BadOptimizations#108](https://github.com/imthosea/BadOptimizations/issues/108), [#2160](https://github.com/Epic-Fight/epicfight/issues/2160).
- Fixed a crash when joining a world with [Ecliptic Seasons](https://www.curseforge.com/minecraft/mc-mods/ecliptic-seasons) installed.

## [20.13.3] - 2025-11-01

### Bugfix
- Fixed Dedicated Server Crash
- Fixed Epic Fight potentially breaking other controller mods
- Fixed the player can't take blocking when holding weapons from Simplyswords
- Fixed the player's blocking and digging animations not removing under certain conditions (usually when it's combined with dodge skills)
- Blocked the player editing signs when 'resolve_key_conflicts' is set to 'interaction'
- Optimized texture files so that save 23% of size from the original

### For Devs
- Refactored the input system to be less dependent on vanilla Minecraft, allowing other controller mods to provide Epic Fight integration.

## [20.13.2] - 2025-10-24

### Bugfix
- Fixed the 'resolve_key_conflicts' option not to be applied when the player is vanilla mode
- Fixed the 'resolve_key_conflicts' messing up the door state
- Fixed the Ender dragon can't hurt the player

### Configuration
- Added a new config option that you can disable Minecraft model while in vanilla mode (Same as old 'filter animation' option)
- Added a new config option that determines how 'item_preference' works
> - Adaptive: same as current work
> - Switch mode: like the old Epic Fight's 'auto_switching' items, it switches the player mode when the player changes a main hand item, forcing the next behavior depending on the player mode

### Changes
- Made the stun shield persistent

## [20.13.1] - 2025-10-20

### Bugfix
- Fixed the players unable to turn camera in first-person on ladders when y rotation is 180 (or -180)
- Fixed the Technician not rewarding stamina

### Configuration
- Expanded the mining block guide option to configure both crosshair and block highlight overlay
- The config option 'resolve_key_conflicts' has been changed to `key_conflict_resolve_scope` which can cancel the vanilla actions when guard key conflicts with item use key

### Skill and Skill UI
- Added a replace cooldown for each skill slot (#2021)
- Added scrolling to the skill editor and slot selector to further enhance addon extensibility

### Shoulder surfing compatibility
- Players now follow camera when they're taking specific Epic Fight actions (attacks, blocking)

### Etc
- Now players can suppress movements of combo attacks by pressing the sneak key
- Enhanced the block highlight so that it only stains an opaque part