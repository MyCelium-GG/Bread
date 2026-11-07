# Changelog

All notable changes to Bread are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Bread is versioned semantically (`MAJOR.MINOR.PATCH`).
It runs on Minecraft 26.3, 26.2 and 26.1 (Fabric + Forge + NeoForge, Java 25) and requires
[Mycel](https://github.com/MyCelium-GG/MyCel-Lib) (`my.celium.org`).

## [1.1.0] - 2026-11-07

### Added
- Name tags of far-away mobs are not drawn (past 32 blocks, players keep theirs): text is costly in farms full of named mobs.
- The enchantment shimmer on far-away items and in item frames is skipped (past 24 blocks), one draw pass less per item.
- Coordinates overlay: your position, facing and biome in the FPS counter's corner. Turn it on with `/bread coords` or the Coords button in `/bread`.
- Both distances are new settings (`name_tag_distance`, `glint_distance`, 0 = vanilla), stretch while zooming and show up in `/bread info`.

## [1.0.0] - 2026-11-06

### Added
- Mobs, chests, signs and item frames fully behind walls are not drawn (checked on a background thread; off when Entity Culling is installed).
- Distance limits for item frames, paintings, armor stands, dropped items, signs, banners and heads; far mobs skip armor and held items; far dropped stacks are drawn as one item; far items are drawn flat. Zooming stretches every limit.
- A particle cap, lighter client physics for dropped items and XP orbs, larger merge distances for items and XP orbs on the server, and an FPS limit while the window is in the background.
- Zoom on C (rebindable), scroll to zoom further.
- FPS counter, in any corner.
- `/bread` with a menu, `/bread info` (what was skipped in the last frame), `fps`, `on`, `off`, `config`, `toggle` and `set`.
- Tweaks: right-click harvest, double doors, ladder extending, bone meal on sugar cane and cactus, shovel back to dirt, villagers on leads, Feather Falling saves crops, pumpkins back on snow golems.
- Anonymous stats (which features are on, the number settings, FPS, how often features are used) so we know what to improve. They follow your answer on Mycel's stats screen; nothing else is asked. What is sent: https://wiki.mycelium-gg.org/privacy#stats-bread
