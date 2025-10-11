# SlimColonies [![Join Discord](https://img.shields.io/discord/1420924294841499800?label=Discord&logo=discord)](https://discord.gg/bfKpMTTuFt)

**Colonists do their work, you do yours**

SlimColonies is a fork of [MineColonies](https://www.curseforge.com/minecraft/mc-mods/minecolonies) where colonists take care of themselves. Build your colony, assign workers, and
everyone gets back to what they're doing.

## Philosophy

MineColonies and SlimColonies serve different purposes. MineColonies offers deep colony simulation when that's what you're looking for. SlimColonies is for when you want to work
alongside your colonists without managing them.

This matters most in complex modpacks. When you're already deep in GregTech or juggling multiple demanding mods, SlimColonies lets everyone focus on their own work. It feels more
vanilla and fits naturally alongside other content. Colonists do their thing, you do yours, and you help each other grow the colony together.

## Why SlimColonies?

**Set it and forget it**: Efficient behaviors are default. No research to unlock basic functionality, no separate progression systems to manage.

**Modpack friendly**: Works alongside complex mods without competing for your attention. No happiness tracking, disease management, or raid events.

**Automation first**: Visitor recruitment is free, couriers work efficiently from day one, and workers don't need hand-holding.

## Development Status

Active development with tens of thousands of lines already removed.

## What's Different

### New Features

* ⛏️ **Miner ore priority system**: In-game GUI to set which ores miners focus on first
* 📦 **Builder scavenging**: Builders periodically "find" small amounts (1-5) of needed materials while idle

### Default Behaviors

* Max crafting recipe capacity
* 25% threshold ordering (couriers batch efficiently)
* Trample prevention
* No research item costs

### Removed Systems

* Happiness, raids, diseases, mercenaries
* Custom farmland, crops, foods, and recipes
* Druid guards and various weapons
* Scrolls

### Other Changes

* Simplified food and guard mechanics
* No working-in-rain restrictions
* No courier carrying capacity limits
* Free visitor recruitment
* ...and many more tweaks and refinements

## For Users

### Creating an Issue

Found a bug or have a suggestion?

1. Check if it's already reported
2. Go to [issues page](https://github.com/haggbart/SlimColonies/issues)
3. Click `New Issue` and fill in details

## For Developers

### IntelliJ Setup

1. Install [Java JDK 17](https://adoptopenjdk.net/)
2. Clone: `git clone https://github.com/haggbart/SlimColonies.git`
3. Open project folder in IntelliJ (right-click `build.gradle` → `Open Folder as IntelliJ Project`)
4. Select `Auto Import` and ensure valid Gradle/JVM
5. Refresh Gradle

**If sync fails:** Check that both Project SDK and Gradle JVM are set to JDK 17 in settings.

### Development Workflow

1. Make changes
2. Build: `./gradlew build`
3. Regenerate data (if needed): `./gradlew runData`
4. Test: `./gradlew runClient`

### Contributing

Found a bug or optimization? Want to help?

1. Fork the repo
2. Make your changes and commit
3. Click `Pull Request` and describe your changes
4. Submit and wait for feedback!
