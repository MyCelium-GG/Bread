# Bread

**About 3x the FPS where your game struggles.** Bread skips drawing what you cannot see or would not notice, and adds zoom, an FPS counter and the small things vanilla should do. Every feature can be switched off.

![Bread off: 19 FPS, Bread on: 67 FPS, in a base with 1,332 item frames, 300 armored mobs and 1,500 dropped stacks](https://wiki.mycelium-gg.org/bread/img/compare-base.png)

## More FPS in busy places
- **Hidden behind walls**: mobs, chests, signs and item frames you cannot see are not drawn. Nothing flickers when you walk around a corner.
- **Far detail**: far-away item frames, signs and dropped items are skipped, far mobs skip their armor, piles of dropped items become one item, items are drawn flat, and far mob name tags and enchantment shimmer are skipped. Zoom in and everything keeps its detail.
- **Fewer particles** at once, **lighter dropped items**, items and XP orbs that **merge from farther apart**, and **30 FPS while the game is in the background**.

![Benchmark: busy base 36 to 114 FPS (3.1x), behind a wall 36 to 314 FPS (8.8x)](https://wiki.mycelium-gg.org/bread/img/benchmark.png)

Measured, not guessed: Bread off and on in the same run, averaged over 40 seconds each. Works next to Sodium and Lithium, which speed up terrain and game logic. Bread speeds up the things standing in the world.

![The same base from behind a wall: 19 FPS without Bread, 170 with](https://wiki.mycelium-gg.org/bread/img/compare-wall.png)

## Zoom and FPS counter
Hold **C** to zoom and scroll to zoom further. Turn on the FPS counter with **/bread fps**, and your coordinates, facing and biome with **/bread coords**.

![Zoom: normal view and holding C](https://wiki.mycelium-gg.org/bread/img/zoom.png)

## See what it does
**/bread info** shows exactly what Bread skipped in the last frame. **/bread** opens a menu with every option.

![/bread info: entities, chests and signs skipped, flat items, faces saved](https://wiki.mycelium-gg.org/bread/img/info.png)

## Small things vanilla should do
- Right-click a grown crop to **harvest and replant** it
- **Double doors** open together
- Click a ladder with a ladder to **extend it**
- **Bone meal** grows sugar cane and cactus
- A **shovel** turns paths and farmland back into dirt
- Put **villagers on a lead**
- **Feather Falling** boots do not trample crops
- Give a sheared **snow golem** its pumpkin back

The performance features, zoom and FPS counter work on any server. The tweaks need Bread on the server or in singleplayer.

Requires [Mycel](https://www.curseforge.com/minecraft/mc-mods/mycel). Fabric, Forge and NeoForge, Minecraft 26.1 to 26.3. [Wiki](https://wiki.mycelium-gg.org/bread/) · [Discord](https://discord.gg/UvQNsKWMp3)
