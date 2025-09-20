# Happiness System Removal Documentation

This document tracks all locations where the happiness system was removed and where fixed values were implemented as replacements.

## Core Classes Completely Removed

### API Classes
- `/src/main/java/com/minecolonies/api/entity/citizen/happiness/` (entire package deleted)
  - `CitizenHappinessHandler.java`
  - `ExpirationBasedHappinessModifier.java`
  - `HappinessFactorTypeEntry.java`
  - `HappinessFunction.java`
  - `HappinessFunctionEntry.java`
  - `HappinessRegistry.java`
  - `IHappinessFunction.java`
  - `IHappinessModifier.java`
  - `ITimeBasedHappinessModifier.java`
  - `StaticHappinessSupplier.java`

### Core Classes
- `/src/main/java/com/minecolonies/core/entity/citizen/citizenhandlers/CitizenHappinessHandler.java`
- `/src/main/java/com/minecolonies/api/util/constant/HappinessConstants.java`
- `/src/main/java/com/minecolonies/core/client/gui/citizen/HappinessWindowCitizen.java`
- `/src/main/resources/assets/minecolonies/gui/citizen/happiness.xml`

### Quest System
- `/src/main/java/com/minecolonies/core/quests/rewards/HappinessRewardTemplate.java`
- `/src/main/java/com/minecolonies/apiimp/initializer/ModHappinessFactorTypeInitializer.java`

## Fixed Values Implementation

### 1. Colony Overall Happiness
**File**: `/src/main/java/com/minecolonies/core/colony/Colony.java:1718`
```java
@Override
public double getOverallHappiness()
{
    return 10.0; // Default happiness value
}
```

### 2. Colony View Happiness
**File**: `/src/main/java/com/minecolonies/core/colony/ColonyView.java:1029`
```java
public double getOverallHappiness()
{
    return 10.0; // Default happiness value without happiness system
}
```

### 3. Citizen Level Cap Calculation
**File**: `/src/main/java/com/minecolonies/core/entity/citizen/citizenhandlers/CitizenSkillHandler.java:92`
```java
final int levelCap = 10; // Default level cap without happiness
```

**File**: `/src/main/java/com/minecolonies/core/entity/citizen/citizenhandlers/CitizenSkillHandler.java:158`
```java
final int levelCap = 10; // Default level cap without happiness
```

### 4. Citizen Data Level Cap
**File**: `/src/main/java/com/minecolonies/core/colony/CitizenData.java:1720`
```java
// Changed from happiness-based calculation to fixed value
final int levelCap = 10; // Default level cap without happiness
```

### 5. Job Idle Severity Constants
**File**: `/src/main/java/com/minecolonies/api/colony/jobs/IJob.java:255-265`
```java
default int getIdleSeverity(boolean isDemand)
{
    if(isDemand)
    {
        return 14; // Default idle days for demands
    }
    else
    {
        return 7; // Default idle days for complains
    }
}
```

## Interface/API Changes

### 1. ICitizenData Interface
**File**: `/src/main/java/com/minecolonies/api/colony/ICitizenData.java`
- Removed: `ICitizenHappinessHandler getCitizenHappinessHandler();`

### 2. ICitizenDataView Interface
**File**: `/src/main/java/com/minecolonies/api/colony/ICitizenDataView.java`
- Removed: `getHappiness()` and `getHappinessHandler()` methods

### 3. ICitizenManager Interface
**File**: `/src/main/java/com/minecolonies/api/colony/managers/interfaces/ICitizenManager.java`
- Removed: `void injectModifier(final IHappinessModifier modifier);`

### 4. IMinecoloniesAPI Interface
**File**: `/src/main/java/com/minecolonies/api/IMinecoloniesAPI.java`
- Removed happiness registry methods

## Sound System Changes

### File: `/src/main/java/com/minecolonies/api/util/SoundUtils.java:135`
```java
// Happiness system removed - always play happy sound
playSoundAtCitizenWith(worldIn, pos, EventType.HAPPY, citizen);
```

## Locations Where Happiness Modifiers Were Removed

### 1. Food Consumption
**File**: `/src/main/java/com/minecolonies/api/util/ItemStackUtils.java:951`
- Removed great food happiness bonus
- Was: `citizen.getCitizenData().getCitizenHappinessHandler().addModifier(...)`

### 2. Sleep Tracking
**File**: `/src/main/java/com/minecolonies/core/entity/ai/minimal/EntityAISleep.java:211`
- Removed sleep happiness modifier
- Was: `citizen.getCitizenData().getCitizenHappinessHandler().resetModifier(SLEPTTONIGHT)`

### 3. Damage Penalties
**File**: `/src/main/java/com/minecolonies/core/entity/citizen/EntityCitizen.java:1409`
- Removed damage happiness penalty
- Was: `getCitizenData().getCitizenHappinessHandler().addModifier(...)`

### 4. Death Penalties
**File**: `/src/main/java/com/minecolonies/core/entity/citizen/EntityCitizen.java:1536`
- Removed death happiness penalty for colony
- Was: `colony.getCitizenManager().injectModifier(new ExpirationBasedHappinessModifier(...))`

### 5. Raid Victory Bonuses
**File**: `/src/main/java/com/minecolonies/core/colony/events/raid/RaidManager.java:1081`
- Removed raid victory happiness bonus
- Was: `colony.getCitizenManager().injectModifier(new ExpirationBasedHappinessModifier(...))`

## Interaction Validators Simplified

### File: `/src/main/java/com/minecolonies/apiimp/initializer/InteractionValidatorInitializer.java`

**Homelessness Validator** (Line 253-258):
- Changed from time-based happiness tracking to simple null check
- Now: `citizen -> citizen.getHomeBuilding() == null`

**Unemployment Validator** (Line 260-265):
- Changed from time-based happiness tracking to simple null check
- Now: `citizen -> citizen.getJob() == null`

**Idle at Job Validator** (Line 267-272):
- Changed from happiness modifier days to direct idle check
- Now: `citizen -> citizen.getJob() != null && citizen.isIdleAtJob()`

**Sleep Validator** (Line 273-275):
- Disabled completely (returns false)

## GUI Components Removed

### 1. Citizen Window Happiness Tab
**File**: `/src/main/java/com/minecolonies/core/client/gui/citizen/AbstractWindowCitizen.java:44-46`
- Removed happiness tab and icon button registrations

### 2. Citizen Window Utils
**File**: `/src/main/java/com/minecolonies/core/client/gui/citizen/CitizenWindowUtils.java`
- Removed `createHappinessBar()` and `updateHappinessBar()` methods

### 3. Main Citizen Window
**File**: `/src/main/java/com/minecolonies/core/client/gui/citizen/MainWindowCitizen.java`
- Removed happiness bar creation and updates

## Research System Changes

### File: `/src/main/java/com/minecolonies/api/research/util/ResearchConstants.java`
- Removed: `public static final ResourceLocation HAPPINESS`

### File: `/src/main/java/com/minecolonies/apiimp/initializer/DefaultResearchProvider.java`
- Removed happiness effect registrations from research definitions

## Language File Changes

### File: `/src/main/resources/assets/minecolonies/lang/en_us.json`
Removed 30+ happiness-related translation keys including:
- `com.minecolonies.coremod.gui.citizen.happiness.*`
- All happiness modifier descriptions
- Happiness-related building descriptions

## Quest System Changes

### Removed Happiness Rewards
**File**: `/src/main/java/com/minecolonies/apiimp/initializer/ModQuestInitializer.java:64`
- Removed: `QuestRegistries.happinessReward` registration

**File**: `/src/main/java/com/minecolonies/api/quests/registries/QuestRegistries.java:186`
- Removed: `public static RegistryObject<RewardEntry> happinessReward;`

### Quest Files Modified
Used automated script to remove happiness reward blocks from quest JSON files in:
- `/src/main/resources/assets/minecolonies/quests/`

## Important Notes for Future Development

1. **Level Caps**: All citizen level caps are now fixed at 10. If you want dynamic level caps in the future, you'll need to implement a new system.

2. **Sound System**: Citizens always play happy sounds now. The random sound selection logic in `SoundUtils.playRandomSound()` still exists but the happiness branch always executes.

3. **Colony Happiness**: The `getOverallHappiness()` method still exists and returns 10.0 for compatibility, but no longer affects any game mechanics.

4. **Interaction Validators**: The validator system still works but uses simplified checks. The translation keys still exist in some cases.

5. **API Compatibility**: Some method signatures were preserved but gutted for easier future restoration if needed.

## Files That May Need Attention Later

- Any mods that depend on the happiness API will break
- Save files with happiness data will still load but happiness data will be ignored
- Custom research effects that modified happiness will need updating
- Quest rewards that granted happiness bonuses are now no-ops