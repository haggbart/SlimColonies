# SlimColonies Tools

This directory contains utility scripts for maintaining the SlimColonies mod.

## update_blueprints.py

Updates mod IDs in blueprint files and pack metadata. Blueprint files are gzipped NBT format files that contain references to mod IDs. The pack.json files are JSON metadata files that specify mod dependencies.

**Features:**

- Updates all `.blueprint` files in the project
- Updates all `pack.json` metadata files
- Handles gzipped NBT format properly
- Provides progress feedback for both file types
- Supports custom mod IDs
- Includes safety warnings for different length IDs

**Usage:**

```bash
# Basic usage (replaces minecolonies with slimcolonies)
python3 tools/update_blueprints.py

# Custom mod IDs
python3 tools/update_blueprints.py --old-id oldmod --new-id newmod

# Custom blueprint directory
python3 tools/update_blueprints.py --blueprint-dir custom/path/to/blueprints
```

## Workflow

When pulling updated blueprints from upstream MineColonies:

1. Copy the blueprints from `../minecolonies/src/main/resources/blueprints/minecolonies` to `src/main/resources/blueprints/slimcolonies`
2. Run: `python3 tools/update_blueprints.py`
3. The script automatically updates both `.blueprint` files and `pack.json` metadata files
