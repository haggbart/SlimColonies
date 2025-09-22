# SlimColonies Project

## Overview

This is a stripped-down fork of MineColonies with many features removed to make colonies less demanding.

## Development Commands

### Building and Running

- `./gradlew runClient` - Build and launch the game client for testing
- `./gradlew runServer` - Build and launch dedicated server
- `./gradlew runData` - Regenerate data files (recipes, loot tables, etc.)
- `./gradlew clean build` - Clean and rebuild entire project

### Setup (if needed)

- `./gradlew genIntellijRuns` - Generate IntelliJ IDEA run configurations

### Testing

- `./gradlew test` - Run unit tests (currently no tests, so don't need to run this)
- `./gradlew runClient` - Test in single-player mode
- `./gradlew compileJava 2>&1 | grep -E "(error:|BUILD|FAILED)"` - Show only errors (filter warnings)

## Development Workflow

1. Make code changes
2. Stop the game if running
3. Run `./gradlew runClient` (automatically rebuilds changed files)
4. Test changes in-game

## Project Structure

- `/src/main/java` - Main Java source code
- `/src/main/resources` - Resources (textures, configs, lang files)
- `/src/api/java` - API code
- `/src/datagen` - Data generation code
- `build.gradle` - Build configuration
- `gradle.properties` - Gradle settings

## Notes

- The project uses Gradle incremental builds - only changed files are recompiled
- No hot-reload support - must restart game to see changes
- After changing build files, run `./gradlew clean build`
