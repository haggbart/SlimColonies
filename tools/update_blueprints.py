#!/usr/bin/env python3
"""
Blueprint Mod ID Updater for SlimColonies

This script updates all .blueprint files in the project to replace
'minecolonies' references with 'slimcolonies' (or any other mod ID).

The blueprint files are gzipped NBT format files that contain mod references.
This script handles the binary replacement while maintaining the NBT structure.

Usage:
    python3 update_blueprints.py
    python3 update_blueprints.py --old-id minecolonies --new-id slimcolonies
"""

import gzip
import os
import sys
import argparse
from pathlib import Path

def update_blueprint(filepath, old_id='minecolonies', new_id='slimcolonies'):
    """
    Update a single blueprint file, replacing old mod ID with new mod ID.

    Args:
        filepath: Path to the blueprint file
        old_id: The mod ID to replace (default: minecolonies)
        new_id: The new mod ID (default: slimcolonies)

    Returns:
        True if successful, False otherwise
    """
    try:
        # Read the gzipped file
        with gzip.open(filepath, 'rb') as f:
            data = f.read()

        # Replace in binary data
        # Note: Both IDs should ideally be the same length for NBT format
        data = data.replace(old_id.encode('utf-8'), new_id.encode('utf-8'))

        # Write back
        with gzip.open(filepath, 'wb') as f:
            f.write(data)

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

    if not blueprint_files:
        print(f"No blueprint files found in {blueprint_dir}")
        return

    success_count = 0
    total_count = len(blueprint_files)

    print(f"Updating {total_count} blueprint files...")
    print(f"Replacing '{args.old_id}' with '{args.new_id}'")

    for i, filepath in enumerate(blueprint_files):
        if update_blueprint(filepath, args.old_id, args.new_id):
            success_count += 1

        # Progress indicator every 100 files
        if (i + 1) % 100 == 0:
            print(f"Processed {i + 1}/{total_count} files...")

    print(f"\nSuccessfully updated {success_count} out of {total_count} blueprint files")

if __name__ == "__main__":
    main()