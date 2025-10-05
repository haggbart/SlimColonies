package no.monopixel.slimcolonies.api.entity.pathfinding.registry;

import net.minecraft.world.entity.Mob;
import no.monopixel.slimcolonies.api.ISlimColoniesAPI;
import no.monopixel.slimcolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;

import java.util.function.Function;
import java.util.function.Predicate;

public interface IPathNavigateRegistry
{

    static IPathNavigateRegistry getInstance()
    {
        return ISlimColoniesAPI.getInstance().getPathNavigateRegistry();
    }

    IPathNavigateRegistry registerNewPathNavigate(Predicate<Mob> selectionPredicate, Function<Mob, AbstractAdvancedPathNavigate> navigateProducer);

    AbstractAdvancedPathNavigate getNavigateFor(Mob entityLiving);
}
