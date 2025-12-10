# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- Fixed datapack animations not loading properly

## [21.14.2] - 2025-12-10

### Fixed

- Fixed the target indicator invisible for non Epic Fight patched entities
- Fixed the lock-on target not being synced to the server when using mouse snap to change the target
- Fixed the player being dark in inventory screen
- Fixed the camera jittering in TPS mode

### Changed

- Now Lock-on automatically searches a new target if there is no currently focusing entity
- Now TPS mode applies 8 directional movement to the player

## [21.14.1] - 2025-12-07

### Added

- Added a client config to allow skipping the third-person front perspective when
  toggling the camera perspective (i.e., F5).
  [#2205](https://github.com/Epic-Fight/epicfight/issues/2205)
- Improved arrow key navigation (`↑`, `↓`, `→`, `←`) in the skill editor screen, including proper scrolling support. [#2203](https://github.com/Epic-Fight/epicfight/issues/2203)
- **[Controlify]** Added native controller support for the skill editor screen and disabled virtual mouse behavior.
- An option that you can always activate the TPS perspective, which was only activated when aiming with ranged weapons.
- An option screen where you can set up the camera position in TPS perspective
- An auto-tracking functionality that aligns the player's look to the crosshair when aiming and striking entities.
- A new keybind that moves the camera freely while locking on any entity to search another target
- A lock-on snapping feature that cycles lock-on entities in the screen by snapping mouse left or right
- An auto-targeting functionality that searches a next target when the current lock-on entity is dead
- An option to toggle lock-on snapping and auto target
- An option to set the maximum distance that the player can focus on entities
- See the devlog [here](https://www.patreon.com/posts/tps-camera-and-141028682)
- Epic Fight's TPS perspective will be automatically disabled when a conflicting mod, such
  as [Shoulder Surfing Reloaded](https://modrinth.com/mod/shoulder-surfing-reloaded)
  or [Better Third Person](https://modrinth.com/mod/better-third-person), is detected to prevent issues.
- Explicit Shoulder Surfing compatibility with the new
  enhanced lock-on (credit [Exopandora](https://github.com/Exopandora)).
  [#2258](https://github.com/Epic-Fight/epicfight/issues/2258)

### Fixed

- Fixed a bug that allowed the player to replace the current skill slot even during cooldown.
- **Fixed:** The **Shoulder Surfing compatibility module** was not being registered,
  causing issues with its intended functionality.
- Fixed the mining crosshair not to show in vanilla mode
- Fixed the massive memory consume on loading the game caused by animation loads
- Disabled the `AzureLib` and `AzureLibArmor` compatibility modules for version `3.X.X` and newer as a workaround to
  address breaking changes and prevent crashes.
- Fixed the player kicked from a dedicated server when other players are equipping Adaptive Skin skill

### For Devs

- Rename the experimental enum `EpicFightInputActions` to `EpicFightInputAction` to follow Java naming
  conventions. [#2194](https://github.com/Epic-Fight/epicfight/issues/2194)
- Removed AirSlash and its related fields (SkillCategory, SkillSlot) to merge air slash and combo attacks as one skill
- Updated the experimental Epic Fight's input API to support using custom input actions that are not a
  `EpicFightInputAction`.
- Extracted vanilla input actions from `EpicFightInputAction` into `MinecraftInputAction`.
  [#2194](https://github.com/Epic-Fight/epicfight/issues/2194)
  [#2194](https://github.com/Epic-Fight/epicfight/issues/2194)
- New API feature: Hooks
    - Replace the mod-loader event system into Epic Fight API, as we're planning to support multi-loader developer environment
    - The feature is still WIP, supporting only events for EpicFightCameraAPI
    - We will eventually replace all Forge/NeoForge events owned by Epic Fight into Hooks
- Added API JAR file, which includes classes under `yesman/epicfight/api/**` only, to allow consumers to compile against
  Epic Fight public API only.
    - **Note:** Keep in mind that Epic Fight public APIs are still being stabilized, and breaking changes may occur. 

## [21.13.5] - 2025-11-12

### Fixed

- Fixed a regression where the Phantom Ascent skill was triggered when
  pressing the jump key while any screen was open (e.g., inventory, chat).
  [#2170](https://github.com/Epic-Fight/epicfight/issues/2170)
- Fixed a regression where the weapon’s innate skill tooltip did not trigger.
  [#2198](https://github.com/Epic-Fight/epicfight/pull/2197)
- Fixed a crash when right click some blocks from Supplementaries
  [#2187](https://github.com/Epic-Fight/epicfight/issues/2187)
- Fixed a cosmetic configuration button always inactivated
- Fixed an unintended mechanism where you weren't able to attack in Epic Fight
  mode while Preference Work is set to Switch Mode, so it works in the same way
  as Item Auto Switching option, an old config where automatically sets player
  mode depending on the item that player holds

### Changed

- Refactored the code to eliminate unnecessary native GLFW calls,
  optimizing whether key down checks are performed per tick, by adapting
  Minecraft vanilla `KeyMapping`, which may also potentially fix other compatibility issues with other mods.
- Avoid registering mixins for non-existing third-party mods to avoid spamming the console log and prevent unnecessary operations.

### For Devs
- Adopted KeyConflictContext for each keybind as documented by [Neoforge](https://docs.neoforged.net/docs/misc/keymappings/#ikeyconflictcontext) to avoid potential problem from inconsistency
- Made GUARD and DODGE CombatKeyMapping, to activate only in Epic Fight mode
- Adjusted `MixinWitherBoss` to follow best practices and prevent potential conflicts with other mods in production environments.

## [21.13.4] - 2025-11-04

### Changed
- Updated the default config to disable the mine block highlight guide.

### Fixed
- Fixed a crash when joining a world with BadOptimizations installed. [BadOptimizations#108](https://github.com/imthosea/BadOptimizations/issues/108), [#2160](https://github.com/Epic-Fight/epicfight/issues/2160).
- Fixed a crash when joining a world with [Ecliptic Seasons](https://www.curseforge.com/minecraft/mc-mods/ecliptic-seasons) installed.

## [21.13.3] - 2025-11-01

### Added
- Added data-driven button guides for improved flexibility and customization.

### Changed
- All changes from Epic Fight 20.13.3
- Refined the dodge guide and introduced a new guide for weapon innate skill tooltips.

### Fixed
- Fixed the camera not switching when aiming ranged weapons
- Fixed an issue where sneaking movement speed was incorrect when using a controller.
- Resolved a crash that occurred when loading on dedicated servers.

## [21.13.1] - 2025-10-21

### Fixed
- Ported from Epic Fight 20.13.1
- Fixed the crash when equipping Geckolib armors
- Fixed the armor's texture to follow the render property first
- Restored Skin layer 3d compatibility
- Fixed the first-person player model transform broken when using a shaderpack
