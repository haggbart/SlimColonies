package no.monopixel.slimcolonies.api.enchants;

import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * All our mods renchants
 */
public class ModEnchants
{
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Constants.MOD_ID);

    private ModEnchants()
    {
        // Intentionally left empty
    }

}
