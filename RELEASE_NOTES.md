# Epic Fight in Minecraft 1.20.1 Changelog
# Changelog on publishing websites and Discord will be parsed between version header ([x.x.x] - yyyy-mm-dd) and (For Devs) section

## [20.13.5] - Unreleased

### Fixed

- Fixed a regression where the Phantom Ascent skill was triggered when
  pressing the jump key while any screen was open (e.g., inventory, chat).
  [#2170](https://github.com/Epic-Fight/epicfight/issues/2170)
- Fixed a crash when right click some blocks from Supplementaries
  [#2187](https://github.com/Epic-Fight/epicfight/issues/2187)

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