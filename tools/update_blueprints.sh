#!/bin/bash
# Blueprint Mod ID Updater for SlimColonies
#
# This bash script updates blueprint files by decompressing them,
# running text replacements, and recompressing them.
#
# Usage:
#   ./update_blueprints.sh
#   ./update_blueprints.sh old_id new_id
#
# Default: Replaces 'minecolonies' with 'slimcolonies'

OLD_ID=${1:-minecolonies}
NEW_ID=${2:-slimcolonies}
BLUEPRINT_DIR="src/main/resources/blueprints"

if [ ! -d "$BLUEPRINT_DIR" ]; then
    echo "Error: Blueprint directory '$BLUEPRINT_DIR' does not exist!"
    exit 1
fi

echo "Updating blueprint files in $BLUEPRINT_DIR"
echo "Replacing '$OLD_ID' with '$NEW_ID'"

count=0
total=0
errors=0

for file in $(find "$BLUEPRINT_DIR" -name "*.blueprint" -type f); do
    ((total++))

    # Use LC_ALL=C to handle binary data properly
    # Replace both "old_id:" and standalone "old_id"
    LC_ALL=C gunzip -c "$file" 2>/dev/null | \
    LC_ALL=C sed -e "s/${OLD_ID}:/${NEW_ID}:/g" -e "s/\b${OLD_ID}\b/${NEW_ID}/g" | \
    gzip > "${file}.tmp" 2>/dev/null

    if [ $? -eq 0 ] && [ -s "${file}.tmp" ]; then
        mv "${file}.tmp" "$file"
        ((count++))
    else
        rm -f "${file}.tmp"
        ((errors++))
    fi

    # Progress indicator every 100 files
    if [ $((total % 100)) -eq 0 ]; then
        echo "Processed $total files..."
    fi
done

echo ""
echo "Results:"
echo "  Total files processed: $total"
echo "  Successfully updated: $count"
echo "  Errors: $errors"

if [ $count -gt 0 ]; then
    echo "Blueprint update completed successfully!"
else
    echo "No files were updated. Check if blueprint files exist and contain the target mod ID."
fi