# SlimColonies

**Self-sufficient colonies that actually help you**

SlimColonies is a fork of [MineColonies](https://github.com/ldtteam/minecolonies) where colonists take care of themselves. Build your colony, assign workers, and get back to
playing Minecraft while they handle the rest. No
babysitting, no events, no demands - just helpful NPCs that enhance your world.

## Philosophy

For players who want the automation and assistance of colonies without the city management gameplay. While MineColonies offers deep colony simulation for those who enjoy that
complexity, here your colonists are self-sufficient helpers who work alongside you without requiring active management.

## What Makes Colonies "Demanding"?

Many features in the original MineColonies require constant player intervention or force unnecessary complexity:

- **Research-Gated Basic Functionality**: Simple quality-of-life features like efficient ordering (25% threshold) were locked behind research, causing couriers to spam small orders
  and waste time instead of working properly by default.

- **Arbitrary Management Systems**: Happiness, diseases, and raids created busywork that pulled players away from actually building and exploring to babysit their colonists.

- **Overcomplicated Progression**: Basic worker efficiency was tied to research requirements and item costs, making colonies feel like a chore to maintain rather than helpful
  automation.

SlimColonies removes these pain points and makes sensible behaviors the default, so your colonists work intelligently from day one without requiring a PhD in colony management.

## Simplification Progress

### ✅ Completed

#### Major System Removals

- [x] Removed happiness system
- [x] Removed raid system
- [x] Removed disease system
- [x] Removed mercenary system
- [x] Removed custom farmland and crops
- [x] Removed custom foods and recipes
- [x] Removed druid guard type
- [x] Simplify guard mechanics
- [x] Removed custom worldgen system

#### Research & Progression Simplification

- [x] Removed unnecessary quests that should be default (moved to default behavior)
- [x] Removed trample prevention research
- [x] Removed research item cost requirements
- [x] Removed crafting recipe capacity research requirements (max capacity by default)
- [x] Removed minimum order quantity research (25% threshold ordering now default)

#### Minor Changes & Quality of Life

- [x] Simplified food mechanic
- [x] Removed working in rain mechanic
- [x] Removed various weapons
- [x] Removed courier building level carrying capacity restrictions
- [x] Remove Scimitar
- [x] Remove Plate Armor
- [x] Simplified visitor recruitment (no cost)
- [x] Removed scrolls

#### Misc & Other

- [x] Many other small changes

### 📋 Planned Changes

- [ ] Add starvation mechanic - citizens should lose health and eventually die when saturation reaches 0
- [ ] Improve compatibility with Farmer's Delight
- More...

## For Users

### Creating an Issue

SlimColonies crashes? Have a suggestion? Found a bug? Create an issue!

1. Make sure your issue hasn't already been answered or fixed.
2. Go to the [issues page](https://github.com/haggbart/SlimColonies/issues).
3. Click `New Issue`
4. Fill in the form with details.
5. Click `Submit New Issue`.

## For Developers

### IntelliJ Setup

1. Install [Java JDK 17](https://adoptopenjdk.net/)
2. Clone the repository: `git clone https://github.com/haggbart/SlimColonies.git`
3. Open the project folder in IntelliJ (right-click `build.gradle` → `Open Folder as IntelliJ Project`)
4. When prompted, select `Auto Import` and ensure a valid Gradle and JVM are selected
5. Refresh Gradle (click the refresh icon in the Gradle tool window)

If Gradle synchronization fails:
- Check `File → Project Structure → Project → Project SDK` is set to JDK 17
- Check `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM` is set to JDK 17

### Development Workflow

1. Make code changes
2. Build the project: `./gradlew build`
3. Regenerate data files (if needed): `./gradlew runData`
4. Test in-game: `./gradlew runClient`

### Troubleshooting

- If Gradle tasks fail due to memory issues, adjust settings in `~/.gradle/gradle.properties`
- If libraries can't be resolved after branch changes, refresh Gradle again

### Contributing

#### Submitting a PR

Found a bug in our code? Think you can make it more efficient? Want to help in general? Great!

1. If you haven't already, create a GitHub account.
2. Click the `Fork` icon located at the top right.
3. Make your changes and commit them.
    * If you're making changes locally, you'll have to do `git commit -a` and `git push` in your command line (or with GitKraken stage the changes, commit them, and push them
      first).
4. Click `Pull Request` in the middle.
5. Click 'New pull request' to create a pull request for this comparison, enter your PR's title, and create a detailed description telling us what you changed.
6. Click `Create pull request` and wait for feedback!
