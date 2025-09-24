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

## When to Use These Tools

Use these scripts when:
- Forking MineColonies and changing the mod ID
- Renaming your mod ID
- Blueprint files show "missing mod" warnings in logs
- Blueprints reference the wrong mod namespace

## Important Notes

1. **Backup First**: Always backup your blueprint files before running these scripts
2. **Same Length IDs**: For best NBT compatibility, keep mod IDs the same length
3. **Test After**: Verify that blueprints load correctly after updates
4. **Binary Data**: These files contain binary NBT data, so text editors may show garbled content

## Troubleshooting

- **No files updated**: Check if blueprint files exist and contain the target mod ID
- **Compilation errors**: Verify the new mod ID matches your Constants.java MOD_ID
- **Missing blocks warnings**: Run the blueprint updater if you see mod reference mismatches