package no.monopixel.slimcolonies.core.entity.pathfinding.pathjobs;

import no.monopixel.slimcolonies.core.entity.pathfinding.MNode;

/**
 * Interface for area based search path jobs
 */
public interface ISearchPathJob
{
    public double getEndNodeScore(MNode n);
}
