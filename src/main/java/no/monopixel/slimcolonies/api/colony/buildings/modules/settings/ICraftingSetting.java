package no.monopixel.slimcolonies.api.colony.buildings.modules.settings;

import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.requestsystem.token.IToken;
import no.monopixel.slimcolonies.api.crafting.IRecipeStorage;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Crafting Setting.
 */
public interface ICraftingSetting extends ISetting<IToken<?>>
{
    /**
     * Get the setting value.
     * @return the current value.
     */
    IRecipeStorage getValue(final IBuilding building);

    /**
     * Get the setting value.
     * @return the current value.
     */
    IRecipeStorage getValue(final IBuildingView building);

    /**
     * Get the list of all settings.
     * @param building server side building.
     * @return the list.
     */
    List<ItemStack> getSettings(final IBuilding building);

    /**
     * Get the list of all settings.
     * @param buildingView client side building.
     * @return a copy of the list.
     */
    List<ItemStack> getSettings(final IBuildingView buildingView);

    /**
     * Set the setting to a specific index.
     * @param value the value to set.
     */
    void set(final IRecipeStorage value);
}
