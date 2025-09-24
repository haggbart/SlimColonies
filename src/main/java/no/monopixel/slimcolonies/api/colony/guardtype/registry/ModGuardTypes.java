package no.monopixel.slimcolonies.api.colony.guardtype.registry;

import no.monopixel.slimcolonies.api.colony.guardtype.GuardType;
import no.monopixel.slimcolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public final class ModGuardTypes
{

    public static final ResourceLocation KNIGHT_ID = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation RANGER_ID = new ResourceLocation(Constants.MOD_ID, "ranger");

    public static RegistryObject<GuardType> knight;
    public static RegistryObject<GuardType> ranger;

    private ModGuardTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModGuardTypes but this is a Utility class.");
    }
}
