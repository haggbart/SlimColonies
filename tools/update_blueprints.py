#!/usr/bin/env python3
"""
Blueprint Mod ID Updater for SlimColonies

This script updates all .blueprint and pack.json files in the project to replace
'minecolonies' references with 'slimcolonies' (or any other mod ID).

The blueprint files are gzipped NBT format files that contain mod references.
The pack.json files are JSON metadata files that specify mod dependencies.

Usage:
    python3 update_blueprints.py
    python3 update_blueprints.py --old-id minecolonies --new-id slimcolonies
"""

import gzip
import os
import sys
import json
import argparse
from pathlib import Path

def update_blueprint(filepath, old_id='minecolonies', new_id='slimcolonies'):
    """Update a blueprint file, replacing old mod ID with new mod ID."""
    try:
        with gzip.open(filepath, 'rb') as f:
            data = f.read()

        # Note: Both IDs should ideally be the same length for NBT format
        data = data.replace(old_id.encode('utf-8'), new_id.encode('utf-8'))

        with gzip.open(filepath, 'wb') as f:
            f.write(data)

        return True
    except Exception as e:
        print(f"Error processing {filepath}: {e}")
        return False

def update_pack_json(filepath, old_id='minecolonies', new_id='slimcolonies'):
    """Update a pack.json file, replacing old mod ID with new mod ID in the mods array."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)

        if 'mods' in data and isinstance(data['mods'], list):
            data['mods'] = [new_id if mod == old_id else mod for mod in data['mods']]

        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')

        return True
    except Exception as e:
        print(f"Error processing {filepath}: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description='Update mod IDs in blueprint files')
    parser.add_argument('--old-id', default='minecolonies',
                       help='Old mod ID to replace (default: minecolonies)')
    parser.add_argument('--new-id', default='slimcolonies',
                       help='New mod ID (default: slimcolonies)')
    parser.add_argument('--blueprint-dir', default='src/main/resources/blueprints',
                       help='Blueprint directory path (default: src/main/resources/blueprints)')

    args = parser.parse_args()

    if len(args.old_id) != len(args.new_id):
        print(f"Warning: Old ID '{args.old_id}' ({len(args.old_id)} chars) and "
              f"new ID '{args.new_id}' ({len(args.new_id)} chars) have different lengths.")
        print("This may cause issues with NBT format.")
        response = input("Continue anyway? (y/n): ")
        if response.lower() != 'y':
            print("Aborted.")
            return

    blueprint_dir = Path(args.blueprint_dir)
    if not blueprint_dir.exists():
        print(f"Error: Blueprint directory '{blueprint_dir}' does not exist!")
        return

    blueprint_files = list(blueprint_dir.glob('**/*.blueprint'))
    pack_json_files = list(blueprint_dir.glob('**/pack.json'))

    if not blueprint_files and not pack_json_files:
        print(f"No blueprint or pack.json files found in {blueprint_dir}")
        return

    blueprint_success = 0
    blueprint_total = len(blueprint_files)

    if blueprint_total > 0:
        print(f"\nUpdating {blueprint_total} blueprint files...")
        print(f"Replacing '{args.old_id}' with '{args.new_id}'")

        for i, filepath in enumerate(blueprint_files):
            if update_blueprint(filepath, args.old_id, args.new_id):
                blueprint_success += 1

            if (i + 1) % 100 == 0:
                print(f"Processed {i + 1}/{blueprint_total} files...")

        print(f"Successfully updated {blueprint_success} out of {blueprint_total} blueprint files")

    json_success = 0
    json_total = len(pack_json_files)

    if json_total > 0:
        print(f"\nUpdating {json_total} pack.json files...")
        print(f"Replacing '{args.old_id}' with '{args.new_id}' in mods array")

        for filepath in pack_json_files:
            if update_pack_json(filepath, args.old_id, args.new_id):
                json_success += 1

        print(f"Successfully updated {json_success} out of {json_total} pack.json files")

    print(f"\n=== Summary ===")
    print(f"Blueprint files: {blueprint_success}/{blueprint_total}")
    print(f"Pack.json files: {json_success}/{json_total}")
    print(f"Total: {blueprint_success + json_success}/{blueprint_total + json_total}")

if __name__ == "__main__":
    main()