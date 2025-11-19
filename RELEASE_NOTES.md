# Epic Fight in Minecraft 1.21.1 Changelog
# Changelog on publishing websites and Discord will be parsed between version header ([x.x.x] - yyyy-mm-dd) and (For Devs) section

## [21.14.1] - Unreleased

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

### Fixed

- Fixed a bug that allowed the player to replace the current skill slot even during cooldown.
- **Fixed:** The **Shoulder Surfing compatibility module** was not being registered,
  causing issues with its intended functionality.

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
