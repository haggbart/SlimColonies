package no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.plantation.modules.specific;

import no.monopixel.slimcolonies.api.colony.buildingextensions.IBuildingExtension;
import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import no.monopixel.slimcolonies.api.equipment.registry.EquipmentTypeEntry;
import no.monopixel.slimcolonies.core.colony.buildings.workerbuildings.plantation.modules.generic.BoneMealedPlantModule;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import static no.monopixel.slimcolonies.api.research.util.ResearchConstants.PLANTATION_NETHER;

/**
 * Planter module for growing {@link Items#CRIMSON_FUNGUS} and {@link Items#CRIMSON_ROOTS}.
 * <br/>
 * Requirements:
 * <ol>
 *     <li>All requirements from {@link BoneMealedPlantModule}</li>
 * </ol>
 */
public class CrimsonPlantsPlantModule extends BoneMealedPlantModule
{
    /**
     * The chance a worker has to work on this field.
     */
    private static final int CHANCE = 35;

    /**
     * Default constructor.
     *
     * @param field    the field instance this module is working on.
     * @param fieldTag the tag of the field anchor block.
     * @param workTag  the tag of the working positions.
     * @param item     the item which is harvested.
     */
    public CrimsonPlantsPlantModule(final IBuildingExtension field, final String fieldTag, final String workTag, final Item item)
    {
        super(field, fieldTag, workTag, item);
    }

    @Override
    protected int getPercentageChance()
    {
        return CHANCE;
    }

    @Override
    public ResourceLocation getRequiredResearchEffect()
    {
        return PLANTATION_NETHER;
    }

    @Override
    public EquipmentTypeEntry getRequiredTool()
    {
        return ModEquipmentTypes.none.get();
    }
}
