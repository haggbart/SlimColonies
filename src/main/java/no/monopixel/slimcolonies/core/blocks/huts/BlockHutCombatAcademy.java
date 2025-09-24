package no.monopixel.slimcolonies.core.blocks.huts;

import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import no.monopixel.slimcolonies.api.colony.buildings.registry.BuildingEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Block of the combat academy camp.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy>
{
    @NotNull
    @Override
    public String getHutName()
    {
        return "blockhutcombatacademy";
    }

    @Override
    public BuildingEntry getBuildingEntry()
    {
        return ModBuildings.combatAcademy.get();
    }
}
