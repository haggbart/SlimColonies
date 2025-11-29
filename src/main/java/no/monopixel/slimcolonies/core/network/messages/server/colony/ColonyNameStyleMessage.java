package no.monopixel.slimcolonies.core.network.messages.server.colony;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.ICivilianData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.core.colony.CitizenData;
import no.monopixel.slimcolonies.core.network.messages.server.AbstractColonyServerMessage;

import java.util.*;

/**
 * Message to set the colony name style.
 */
public class ColonyNameStyleMessage extends AbstractColonyServerMessage
{
    private String style;

    public ColonyNameStyleMessage()
    {
        super();
    }

    public ColonyNameStyleMessage(final IColony colony, final String style)
    {
        super(colony);
        this.style = style;
    }

    @Override
    protected void onExecute(NetworkEvent.Context ctxIn, boolean isLogicalServer, IColony colony)
    {
        colony.setNameStyle(style);

        final Random random = new Random();
        final Map<String, String> deadParentNameCache = new HashMap<>();
        final Set<String> livingCitizenNames = new HashSet<>();

        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName() != null)
            {
                livingCitizenNames.add(citizen.getName());
            }
        }

        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (hasNoParents(citizen))
            {
                renameFirstGeneration(citizen, random, colony, deadParentNameCache, livingCitizenNames);
            }
        }

        livingCitizenNames.clear();
        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName() != null)
            {
                livingCitizenNames.add(citizen.getName());
            }
        }

        for (final ICitizenData citizen : colony.getCitizenManager().getCitizens())
        {
            if (!hasNoParents(citizen))
            {
                updateCivilianName(citizen, random, colony, deadParentNameCache, livingCitizenNames);
            }
        }

        for (final ICivilianData visitor : colony.getVisitorManager().getCivilianDataMap().values())
        {
            updateCivilianName(visitor, random, colony, deadParentNameCache, livingCitizenNames);
        }
    }

    private static boolean hasNoParents(final ICitizenData citizen)
    {
        final var parents = citizen.getParents();
        return parents == null ||
            ((parents.getA() == null || parents.getA().isEmpty()) &&
                (parents.getB() == null || parents.getB().isEmpty()));
    }

    private static String getOrGenerateDeadParentName(
        final String oldParentName,
        final boolean isFemale,
        final Random random,
        final IColony colony,
        final Map<String, String> deadParentNameCache)
    {
        return deadParentNameCache.computeIfAbsent(oldParentName,
            k -> CitizenData.generateName(random, isFemale, colony, colony.getCitizenNameFile()));
    }

    private static void renameFirstGeneration(
        final ICitizenData citizen,
        final Random random,
        final IColony colony,
        final Map<String, String> deadParentNameCache,
        final Set<String> livingCitizenNames)
    {
        final String oldName = citizen.getName();
        updateCivilianName(citizen, random, colony, deadParentNameCache, livingCitizenNames);
        final String newName = citizen.getName();

        if (!oldName.equals(newName))
        {
            updateChildrenParentNames(citizen, oldName, newName, colony);
        }
    }

    private static void updateChildrenParentNames(final ICitizenData parent, final String oldName, final String newName, final IColony colony)
    {
        for (final Integer childId : parent.getChildren())
        {
            final ICitizenData child = colony.getCitizenManager().getCivilian(childId);
            if (child != null && child.getParents() != null)
            {
                final var parents = child.getParents();
                final String parentA = oldName.equals(parents.getA()) ? newName : parents.getA();
                final String parentB = oldName.equals(parents.getB()) ? newName : parents.getB();
                child.setParents(parentA, parentB);
            }
        }
    }

    private static boolean isParentDead(final String parentName, final Set<String> livingCitizenNames)
    {
        if (parentName == null || parentName.isEmpty())
        {
            return true;
        }

        return !livingCitizenNames.contains(parentName);
    }

    private static void updateCivilianName(
        final ICivilianData civilian,
        final Random random,
        final IColony colony,
        final Map<String, String> deadParentNameCache,
        final Set<String> livingCitizenNames)
    {
        if (civilian == null)
        {
            return;
        }

        if (civilian instanceof CitizenData citizen && !hasNoParents(citizen))
        {
            final var parents = citizen.getParents();
            String parentA = parents.getA();
            String parentB = parents.getB();

            if (isParentDead(parentA, livingCitizenNames))
            {
                parentA = getOrGenerateDeadParentName(parentA, false, random, colony, deadParentNameCache);
            }

            if (isParentDead(parentB, livingCitizenNames))
            {
                parentB = getOrGenerateDeadParentName(parentB, true, random, colony, deadParentNameCache);
            }

            if (!parentA.equals(parents.getA()) || !parentB.equals(parents.getB()))
            {
                citizen.setParents(parentA, parentB);
            }

            citizen.generateName(random, parentA, parentB, colony.getCitizenNameFile());
        }
        else
        {
            final String newName = CitizenData.generateName(random, civilian.isFemale(), colony, colony.getCitizenNameFile());
            civilian.setName(newName);
        }

        civilian.getEntity().ifPresent(entity -> entity.setCustomName(Component.literal(civilian.getName())));
    }

    @Override
    protected void toBytesOverride(FriendlyByteBuf buf)
    {
        buf.writeUtf(style);
    }

    @Override
    protected void fromBytesOverride(FriendlyByteBuf buf)
    {
        this.style = buf.readUtf(32767);
    }
}
