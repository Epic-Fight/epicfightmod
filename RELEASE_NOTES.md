# Epic Fight in Minecraft 1.21.1 Changelog
# Changelog on publishing websites and Discord will be parsed between version header ([x.x.x] - yyyy-mm-dd) and (For Devs) section

## [21.13.5] - Unreleased

### Fixed

- Fixed a regression where the Phantom Ascent skill was triggered when
  pressing the jump key while any screen was open (e.g., inventory, chat).
  [#2170](https://github.com/Epic-Fight/epicfight/issues/2170)

### Changed

- Refactored the code to eliminate unnecessary native GLFW calls,
  optimizing whether key down checks are performed per tick, by adapting
  Minecraft vanilla `KeyMapping`, which may also potentially fix other compatibility issues with other mods.
- Avoid registering mixins for non-existing third-party mods to avoid spamming the console log and prevent unnecessary operations.

### For Devs
- Adopted KeyConflictContext for each keybind as documented by [Neoforge](https://docs.neoforged.net/docs/misc/keymappings/#ikeyconflictcontext) to avoid potential problem from inconsistency
- Made GUARD and DODGE CombatKeyMapping, to activate only in Epic Fight mode

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
