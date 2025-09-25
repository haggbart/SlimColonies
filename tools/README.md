# SlimColonies Tools

This directory contains utility scripts for maintaining the SlimColonies mod.

## Scripts

### `update_blueprints.py`

A Python script for updating mod IDs in blueprint files. Blueprint files are gzipped NBT format files that contain references to mod IDs.

**Features:**

- Updates all `.blueprint` files in the project
- Handles gzipped NBT format properly
- Provides progress feedback
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

### `update_blueprints.sh`

A Bash script alternative for updating blueprint files using text replacement on decompressed data.

**Usage:**

```bash
# Basic usage (replaces minecolonies with slimcolonies)
./tools/update_blueprints.sh

# Custom mod IDs
./tools/update_blueprints.sh oldmod newmod
```
