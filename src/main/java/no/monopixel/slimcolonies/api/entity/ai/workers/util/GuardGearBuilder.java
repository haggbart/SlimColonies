package no.monopixel.slimcolonies.api.entity.ai.workers.util;

import no.monopixel.slimcolonies.api.equipment.ModEquipmentTypes;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

public final class GuardGearBuilder
{
    /**
     * Private constructor to hide implicit one.
     */
    private GuardGearBuilder()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Build a list of armor for the guard.
     *
     * @return the new list.
     */
    public static List<GuardGear> buildGearForLevel()
    {
        final List<GuardGear> armorList = new ArrayList<>();
        armorList.add(new GuardGear(ModEquipmentTypes.boots.get(), EquipmentSlot.FEET));
        armorList.add(new GuardGear(ModEquipmentTypes.chestplate.get(), EquipmentSlot.CHEST));
        armorList.add(new GuardGear(ModEquipmentTypes.helmet.get(), EquipmentSlot.HEAD));
        armorList.add(new GuardGear(ModEquipmentTypes.leggings.get(), EquipmentSlot.LEGS));
        return armorList;
    }

}
