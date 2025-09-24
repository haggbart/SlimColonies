package no.monopixel.slimcolonies.core.colony.buildings.modules;

import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.IVisitorData;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.*;
import no.monopixel.slimcolonies.api.colony.buildings.modules.*;
import no.monopixel.slimcolonies.api.colony.buildings.modules.stat.IStat;
import no.monopixel.slimcolonies.api.colony.interactionhandling.ChatPriority;
import no.monopixel.slimcolonies.api.sounds.TavernSounds;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.MathUtils;
import no.monopixel.slimcolonies.api.util.StatsUtil;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.huts.WindowHutLiving;
import no.monopixel.slimcolonies.core.colony.buildings.views.LivingBuildingView;
import no.monopixel.slimcolonies.core.colony.eventhooks.citizenEvents.VisitorSpawnedEvent;
import no.monopixel.slimcolonies.core.colony.interactionhandling.RecruitmentInteraction;
import no.monopixel.slimcolonies.core.datalistener.CustomVisitorListener;
import no.monopixel.slimcolonies.core.network.messages.client.colony.PlayMusicAtPosMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static no.monopixel.slimcolonies.api.entity.ai.statemachine.tickratestatemachine.TickRateConstants.MAX_TICKRATE;
import static no.monopixel.slimcolonies.api.util.constant.Constants.MAX_STORY;
import static no.monopixel.slimcolonies.api.util.constant.Constants.TAG_COMPOUND;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_VISITORS;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_WORK;
import static no.monopixel.slimcolonies.api.util.constant.SchematicTagConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.StatisticsConstants.NEW_VISITORS;

/**
 * Tavern building for the colony. Houses 4 citizens Plays a tavern theme on entering Spawns/allows citizen recruitment Spawns trader/quest npcs
 */
public class TavernBuildingModule extends AbstractBuildingModule implements IDefinesCoreBuildingStatsModule, IBuildingEventsModule, IPersistentModule, ITickingModule
{
    /**
     * Schematic name
     */
    public static final String TAG_VISITOR_ID = "visitor";

    /**
     * Music interval
     */
    private static final int    TWENTY_MINUTES  = 20 * 60 * 20;
    private static final String TAG_NOVISITTIME = "novisit";

    /**
     * Cooldown for the music, to not play it too much/not overlap with itself
     */
    private int musicCooldown = 0;

    /**
     * List of additional citizens
     */
    private final List<Integer> externalCitizens = new ArrayList<>();

    /**
     * List of sitting positions for this building
     */
    private final List<BlockPos> sitPositions = new ArrayList<>();

    /**
     * List of work positions for this building
     */
    private final List<BlockPos> workPositions = new ArrayList<>();

    private boolean initTags = false;

    /**
     * Penalty for not spawning visitors after a death
     */
    private int noVisitorTime = 10000;

    @Override
    public IStat<Integer> getMaxInhabitants()
    {
        if (building.getBuildingLevel() <= 0)
        {
            return (prev) -> 0;
        }

        return (prev) -> 4;
    }

    @Override
    public void onPlayerEnterBuilding(final Player player)
    {
        if (musicCooldown <= 0 && building.getBuildingLevel() > 0 && !building.getColony().isDay())
        {
            int count = 0;
            BlockPos avg = BlockPos.ZERO;
            for (final Integer id : externalCitizens)
            {
                final IVisitorData data = building.getColony().getVisitorManager().getVisitor(id);
                if (data != null)
                {
                    if (!data.getSittingPosition().equals(BlockPos.ZERO))
                    {
                        count++;
                        avg = avg.offset(data.getSittingPosition());
                    }
                }
            }

            if (count < 2)
            {
                return;
            }

            avg = new BlockPos(avg.getX() / count, avg.getY() / count, avg.getZ() / count);
            final PlayMusicAtPosMessage message = new PlayMusicAtPosMessage(TavernSounds.tavernTheme, avg, building.getColony().getWorld(), 0.7f, 1.0f);
            for (final ServerPlayer curPlayer : building.getColony().getPackageManager().getCloseSubscribers())
            {
                Network.getNetwork().sendToPlayer(message, curPlayer);
            }
            musicCooldown = TWENTY_MINUTES;
        }
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        if (musicCooldown > 0)
        {
            musicCooldown -= MAX_TICKRATE;
        }

        externalCitizens.removeIf(id -> colony.getVisitorManager().getVisitor(id) == null);

        if (noVisitorTime > 0)
        {
            noVisitorTime -= 500;
        }

        if (building.getBuildingLevel() > 0 && externalCitizens.size() < 3 * building.getBuildingLevel() && noVisitorTime <= 0)
        {
            spawnVisitorInternal();
            noVisitorTime = colony.getWorld().getRandom().nextInt(3000)
                + (6000 / building.getBuildingLevel()) * colony.getCitizenManager().getCurrentCitizenCount() / colony.getCitizenManager().getMaxCitizens();
        }
    }

    @Override
    public void onUpgradeComplete(final int newlevel)
    {
        initTags = false;
    }

    /**
     * Spawns a visitor specifically for the tavern logic.
     */
    private void spawnVisitorInternal()
    {
        final IVisitorData visitorData = spawnVisitor();
        if (visitorData != null && !CustomVisitorListener.chanceCustomVisitors(visitorData))
        {
            visitorData.triggerInteraction(new RecruitmentInteraction(Component.translatable(
                "com.minecolonies.coremod.gui.chat.recruitstory" + (building.getColony().getWorld().random.nextInt(MAX_STORY) + 1), visitorData.getName().split(" ")[0]),
                ChatPriority.IMPORTANT));
        }
    }

    /**
     * Spawns a visitor citizen that can be recruited.
     */
    @Nullable
    public IVisitorData spawnVisitor()
    {
        final IVisitorData newCitizen = (IVisitorData) building.getColony().getVisitorManager().createAndRegisterCivilianData();
        newCitizen.setBedPos(building.getPosition());
        newCitizen.setHomeBuilding(building);
        newCitizen.getCitizenSkillHandler().init(8 + building.getBuildingLevel() * 2);

        BlockPos spawnPos;
        final BlockPos gatePos = building.getColony().getBuildingManager().getRandomBuilding(b -> b.getBuildingType() == ModBuildings.gateHouse.get());
        if (gatePos != null)
        {
            final IBuilding gateHouseBuilding = building.getColony().getBuildingManager().getBuilding(gatePos);
            if (gateHouseBuilding != null)
            {
                final List<BlockPos> gatePositions = gateHouseBuilding.getLocationsFromTag(TAG_GATE);
                if (gatePositions.isEmpty())
                {
                    spawnPos = BlockPosUtil.findSpawnPosAround(building.getColony().getWorld(), gatePos);
                }
                else
                {
                    spawnPos = BlockPosUtil.findSpawnPosAround(building.getColony().getWorld(), gatePositions.get(MathUtils.RANDOM.nextInt(gatePositions.size())));
                }
            }
            else
            {
                spawnPos = BlockPosUtil.findSpawnPosAround(building.getColony().getWorld(), gatePos);
            }
        }
        else
        {
            spawnPos = BlockPosUtil.findSpawnPosAround(building.getColony().getWorld(), building.getPosition());
        }

        if (spawnPos == null)
        {
            spawnPos = building.getPosition();
        }

        building.getColony().getVisitorManager().spawnOrCreateCivilian(newCitizen, building.getColony().getWorld(), spawnPos, true);
        building.getColony().getEventDescriptionManager().addEventDescription(new VisitorSpawnedEvent(spawnPos, newCitizen.getName()));

        StatsUtil.trackStat(building, NEW_VISITORS, 1);

        externalCitizens.add(newCitizen.getId());
        return newCitizen;
    }

    @Override
    public void serializeNBT(final CompoundTag nbt)
    {
        final ListTag visitorlist = new ListTag();
        for (final Integer id : externalCitizens)
        {
            CompoundTag visitorCompound = new CompoundTag();
            visitorCompound.putInt(TAG_VISITOR_ID, id);
            visitorlist.add(visitorCompound);
        }

        nbt.put(TAG_VISITORS, visitorlist);
        nbt.putInt(TAG_NOVISITTIME, noVisitorTime);
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final ListTag visitorlist = nbt.getList(TAG_VISITORS, TAG_COMPOUND);
        for (final Tag data : visitorlist)
        {
            final int id = ((CompoundTag) data).getInt(TAG_VISITOR_ID);
            final ICitizenData citizenData = building.getColony().getVisitorManager().getCivilian(id);
            if (citizenData != null)
            {
                externalCitizens.add(id);
                citizenData.setHomeBuilding(building);
            }
        }
        noVisitorTime = nbt.getInt(TAG_NOVISITTIME);
    }

    /**
     * Gets a free sitting position
     *
     * @return a blockpos to sit at
     */
    public BlockPos getFreeSitPosition()
    {
        final List<BlockPos> positions = new ArrayList<>(getSitPositions());

        if (positions.isEmpty())
        {
            return null;
        }

        for (final Integer id : externalCitizens)
        {
            final IVisitorData data = building.getColony().getVisitorManager().getVisitor(id);
            if (data != null)
            {
                positions.remove(data.getSittingPosition());
            }
        }

        if (!positions.isEmpty())
        {
            return positions.get(building.getColony().getWorld().random.nextInt(positions.size()));
        }

        return null;
    }

    @Override
    public void onDestroyed()
    {
        for (final Integer id : externalCitizens)
        {
            building.getColony().getVisitorManager().removeCivilian(building.getColony().getVisitorManager().getVisitor(id));
        }
    }

    /**
     * Gets the assigned visitor ids
     *
     * @return list of ids
     */
    public List<Integer> getExternalCitizens()
    {
        return externalCitizens;
    }

    /**
     * Get a random work position
     *
     * @return a random work pos
     */
    public BlockPos getWorkPos()
    {
        if (!getWorkPositions().isEmpty())
        {
            return workPositions.get(building.getColony().getWorld().random.nextInt(workPositions.size()));
        }
        return null;
    }

    /**
     * Get the list of sitting positions
     *
     * @return sit pos list
     */
    private List<BlockPos> getSitPositions()
    {
        initTagPositions();
        return sitPositions;
    }

    /**
     * Get the list of work positions
     *
     * @return work pos list
     */
    private List<BlockPos> getWorkPositions()
    {
        initTagPositions();
        return workPositions;
    }

    /**
     * Initializes the sitting and work position lists
     */
    public void initTagPositions()
    {
        if (initTags)
        {
            return;
        }

        final IBlueprintDataProviderBE te = building.getTileEntity();
        if (te != null)
        {
            initTags = true;
            for (final Map.Entry<BlockPos, List<String>> entry : te.getWorldTagPosMap().entrySet())
            {
                if (entry.getValue().contains(TAG_SITTING))
                {
                    sitPositions.add(entry.getKey());
                }
                if (entry.getValue().contains(TAG_SIT_IN))
                {
                    sitPositions.add(entry.getKey());
                }
                if (entry.getValue().contains(TAG_SIT_OUT))
                {
                    sitPositions.add(entry.getKey());
                }

                if (entry.getValue().contains(TAG_WORK))
                {
                    workPositions.add(entry.getKey());
                }
            }
        }
    }

    /**
     * Removes the given citizen id
     *
     * @param id to remove
     */
    public boolean removeCitizen(final Integer id)
    {
        externalCitizens.remove(id);
        return false;
    }

    /**
     * Sets the delay time before a new visitor spawns
     *
     * @param noVisitorTime time in ticks
     */
    public void setNoVisitorTime(final int noVisitorTime)
    {
        this.noVisitorTime = noVisitorTime;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends LivingBuildingView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowHutLiving(this);
        }
    }
}
