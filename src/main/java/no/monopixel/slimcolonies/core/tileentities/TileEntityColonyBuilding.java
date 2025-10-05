package no.monopixel.slimcolonies.core.tileentities;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.RotationMirror;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import no.monopixel.slimcolonies.api.blocks.AbstractBlockHut;
import no.monopixel.slimcolonies.api.blocks.AbstractColonyBlock;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.colony.buildings.IBuilding;
import no.monopixel.slimcolonies.api.colony.buildings.IBuildingContainer;
import no.monopixel.slimcolonies.api.colony.buildings.views.IBuildingView;
import no.monopixel.slimcolonies.api.colony.permissions.Action;
import no.monopixel.slimcolonies.api.compatibility.newstruct.BlueprintMapping;
import no.monopixel.slimcolonies.api.inventory.api.CombinedItemHandler;
import no.monopixel.slimcolonies.api.inventory.container.ContainerBuildingInventory;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import no.monopixel.slimcolonies.api.tileentities.AbstractTileEntityRack;
import no.monopixel.slimcolonies.api.tileentities.ITickable;
import no.monopixel.slimcolonies.api.tileentities.SlimColoniesTileEntities;
import no.monopixel.slimcolonies.api.util.BlockPosUtil;
import no.monopixel.slimcolonies.api.util.ItemStackUtils;
import no.monopixel.slimcolonies.api.util.Log;
import no.monopixel.slimcolonies.api.util.WorldUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static no.monopixel.slimcolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_BUILDING_TYPE;
import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.TAG_NAME;
import static no.monopixel.slimcolonies.api.util.constant.SchematicTagConstants.BUILDING_SIGN;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TileEntityColonyBuilding extends AbstractTileEntityColonyBuilding implements ITickable
{
    /**
     * NBTTag to store the colony id.
     */
    private static final String TAG_COLONY = "colony";
    private static final String TAG_MIRROR = "mirror";
    private static final String TAG_STYLE  = "style";
    private static final String TAG_PACK   = "pack";
    private static final String TAG_PATH   = "path";

    /**
     * The colony id.
     */
    private int colonyId = 0;

    /**
     * The colony.
     */
    private IColony colony;

    /**
     * The building the tileEntity belongs to.
     */
    private IBuilding building;

    /**
     * Check if the building has a mirror.
     */
    private boolean mirror;

    /**
     * The style of the building.
     */
    private String packMeta = "";

    /**
     * Path of the blueprint.
     */
    private String path = "";

    /**
     * The name of the building location.
     */
    public ResourceLocation registryName;

    /**
     * Create the combined inv wrapper for the building.
     */
    private LazyOptional<CombinedItemHandler> combinedInv;

    /**
     * Pending blueprint future.
     */
    private Future<Blueprint> pendingBlueprintFuture = null;

    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityColonyBuilding(final BlockPos pos, final BlockState state)
    {
        this(SlimColoniesTileEntities.BUILDING.get(), pos, state);
    }

    /**
     * Alternative overriden constructor.
     *
     * @param type the entity type.
     */
    public TileEntityColonyBuilding(final BlockEntityType<? extends AbstractTileEntityColonyBuilding> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    @Override
    public IColony getColony()
    {
        if (colony == null)
        {
            updateColonyReferences();
        }
        return colony;
    }

    /**
     * Synchronises colony references from the tile entity.
     */
    private void updateColonyReferences()
    {
        if (colony == null && getLevel() != null)
        {
            if (colonyId == 0)
            {
                colony = IColonyManager.getInstance().getColonyByPosFromWorld(getLevel(), this.getBlockPos());
            }
            else if (level.isClientSide)
            {
                colony = IColonyManager.getInstance().getColonyView(colonyId, getLevel().dimension());
            }
            else
            {
                colony = IColonyManager.getInstance().getColonyByWorld(colonyId, getLevel());
            }

            // It's most probably previewed building, please don't spam it here.
            if (colony == null && !getLevel().isClientSide)
            {
                //log on the server
                //Log.getLogger().info(String.format("TileEntityColonyBuilding at %s:[%d,%d,%d] had colony.",getWorld().getWorldInfo().getWorldName(), pos.getX(), pos.getY(), pos.getZ()));
            }
        }

        if (building == null && colony != null && !getLevel().isClientSide)
        {
            building = colony.getBuildingManager().getBuilding(getPosition());
            if (building != null)
            {
                registryName = building.getBuildingType().getRegistryName();
                building.setTileEntity(this);
            }
        }
    }

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    @Override
    public BlockPos getPosition()
    {
        return worldPosition;
    }

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Override
    @Nullable
    public BlockPos getPositionOfChestWithItemStack(@NotNull final Predicate<ItemStack> itemStackSelectionPredicate)
    {
        final Predicate<ItemStack> notEmptyPredicate = itemStackSelectionPredicate.and(ItemStackUtils.NOT_EMPTY_PREDICATE);
        @Nullable final IBuildingContainer theBuilding = getBuilding();

        if (theBuilding != null)
        {
            for (final BlockPos pos : theBuilding.getContainers())
            {
                if (WorldUtil.isBlockLoaded(level, pos))
                {
                    final BlockEntity entity = getLevel().getBlockEntity(pos);
                    if (entity instanceof AbstractTileEntityRack)
                    {
                        if (((AbstractTileEntityRack) entity).hasItemStack(notEmptyPredicate))
                        {
                            return pos;
                        }
                    }
                    else if (isInTileEntity(entity, notEmptyPredicate))
                    {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    @Override
    public void setColony(final IColony c)
    {
        colony = c;
        colonyId = c.getID();
        setChanged();
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if (building != null)
        {
            building.markDirty();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return saveWithId();
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag)
    {
        this.load(tag);
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        colonyId = compound.getInt(TAG_COLONY);
        super.onDataPacket(net, packet);
    }

    @Override
    public void onLoad()
    {
        if (building != null)
        {
            building.setTileEntity(null);
        }
    }

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuildingContainer} associated with the tile entity.
     */
    @Override
    public IBuilding getBuilding()
    {
        if (building == null)
        {
            updateColonyReferences();
        }
        return building;
    }

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link IBuilding} to associate with the tile entity.
     */
    @Override
    public void setBuilding(final IBuilding b)
    {
        building = b;
    }

    @NotNull
    @Override
    public Component getDisplayName()
    {
        return getBlockState().getBlock().getName();
    }

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link IBuildingView} the tile entity is associated with.
     */
    @Override
    public IBuildingView getBuildingView()
    {
        final IColonyView c = IColonyManager.getInstance().getColonyView(colonyId, level.dimension());
        return c == null ? null : c.getBuilding(getPosition());
    }

    @Override
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);
        if (compound.contains(TAG_COLONY))
        {
            colonyId = compound.getInt(TAG_COLONY);
        }
        mirror = compound.getBoolean(TAG_MIRROR);

        String packName;
        String path;
        if (compound.contains(TAG_STYLE) && !compound.getString(TAG_STYLE).isEmpty())
        {
            packName = BlueprintMapping.getStyleMapping(compound.getString(TAG_STYLE));

            if (this.getSchematicName().isEmpty())
            {
                path = null;
            }
            else
            {
                final String level = this.getSchematicName().substring(this.getSchematicName().length() - 1);
                path = BlueprintMapping.getPathMapping(compound.getString(TAG_STYLE), this.getSchematicName().substring(0, this.getSchematicName().length() - 1)) + level
                    + ".blueprint";
            }
        }
        else
        {
            packName = compound.getString(TAG_PACK);
            path = compound.getString(TAG_PATH);
        }

        if (getBlockState().getBlock() instanceof AbstractBlockHut<?>)
        {
            if (packName == null || packName.isEmpty())
            {
                final List<String> tags = new ArrayList<>(getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()));
                if (!tags.isEmpty())
                {
                    tags.remove(DEACTIVATED);
                    if (!tags.isEmpty())
                    {
                        packName = BlueprintMapping.getStyleMapping(tags.get(0));
                        if (path == null || path.isEmpty())
                        {
                            path = BlueprintMapping.getPathMapping(tags.get(0), ((AbstractBlockHut<?>) getBlockState().getBlock()).getBlueprintName()) + "1.blueprint";
                        }
                    }
                }
                else if (StructurePacks.selectedPack != null)
                {
                    packName = StructurePacks.selectedPack.getName();
                }
            }

            if (path == null || path.isEmpty() || path.contains("null"))
            {
                path = BlueprintMapping.getPathMapping("", ((AbstractBlockHut<?>) getBlockState().getBlock()).getBlueprintName()) + "1.blueprint";
            }

            if (!path.endsWith(".blueprint"))
            {
                path += ".blueprint";
            }
        }

        this.packMeta = packName;
        this.path = path;

        if (compound.contains(TAG_BUILDING_TYPE))
        {
            registryName = new ResourceLocation(compound.getString(TAG_BUILDING_TYPE));
        }
        buildingPos = worldPosition;
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt(TAG_COLONY, colonyId);
        compound.putBoolean(TAG_MIRROR, mirror);
        compound.putString(TAG_PACK, packMeta == null ? "" : packMeta);
        compound.putString(TAG_PATH, path == null ? "" : path);
        if (registryName != null)
        {
            compound.putString(TAG_BUILDING_TYPE, registryName.toString());
        }
    }

    @Override
    public void tick()
    {
        if (combinedInv != null)
        {
            combinedInv.invalidate();
            combinedInv = null;
        }
        if (!getLevel().isClientSide && colonyId == 0)
        {
            final IColony tempColony = IColonyManager.getInstance().getColonyByPosFromWorld(getLevel(), this.getPosition());
            if (tempColony != null)
            {
                colonyId = tempColony.getID();
            }
        }
        else
        {
            if (colony instanceof IColonyView && level.getGameTime() % 20 == 0)
            {
                final IBuildingView buildingView = ((IColonyView) colony).getBuilding(buildingPos);
                if (buildingView != null)
                {
                    for (final BlockPos buildingSignPos : getWorldTagNamePosMap().getOrDefault(BUILDING_SIGN, Collections.emptySet()))
                    {
                        if (WorldUtil.isBlockLoaded(colony.getWorld(), buildingSignPos))
                        {
                            final BlockEntity blockEntity = colony.getWorld().getBlockEntity(buildingSignPos);
                            if (blockEntity instanceof SignBlockEntity signBlockEntity)
                            {
                                SignText signText = new SignText();
                                final String nameText = Component.translatable(buildingView.getBuildingDisplayName()).getString();

                                final List<FormattedText> lines = Minecraft.getInstance().font.getSplitter().splitLines(nameText, 60, Style.EMPTY);
                                int i;
                                for (i = 0; i < Math.min(lines.size(), 3); i++)
                                {
                                    signText = signText.setMessage(i, Component.literal(lines.get(i).getString()));
                                }

                                signText = signText.setMessage(i, Component.literal(buildingView.getBuildingLevel() + ""));
                                signBlockEntity.setText(signText, true);
                                signBlockEntity.setText(signText, false);
                            }
                        }
                    }
                }
            }
        }

        if (colonyId != 0 && colony == null)
        {
            updateColonyReferences();
        }

        if (pendingBlueprintFuture != null && pendingBlueprintFuture.isDone())
        {
            try
            {
                processBlueprint(pendingBlueprintFuture.get());
                pendingBlueprintFuture = null;
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isUsableByPlayer(@NotNull final Player player)
    {
        return this.hasAccessPermission(player);
    }

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    @Override
    public boolean hasAccessPermission(final Player player)
    {
        // TODO This is called every tick the GUI is open. Is that bad?
        return building == null || building.getColony().getPermissions().hasPermission(player, Action.ACCESS_HUTS);
    }

    /**
     * Set if the entity is mirrored.
     *
     * @param mirror true if so.
     */
    @Override
    public void setMirror(final boolean mirror)
    {
        this.mirror = mirror;
    }

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    @Override
    public boolean isMirrored()
    {
        return mirror;
    }

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    @Override
    public StructurePackMeta getStructurePack()
    {
        return StructurePacks.getStructurePack(this.packMeta);
    }

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    public void setStructurePack(final StructurePackMeta style)
    {
        this.packMeta = style.getName();
    }

    @Override
    public void setBlueprintPath(final String path)
    {
        this.path = path;
    }

    @Override
    public void setPackName(final String packName)
    {
        this.packMeta = packName;
    }

    @Override
    public String getPackName()
    {
        return packMeta;
    }

    @Override
    public String getBlueprintPath()
    {
        return path;
    }

    @Override
    public ResourceLocation getBuildingName()
    {
        if (registryName != null && !registryName.getPath().isEmpty())
        {
            return registryName;
        }
        return getBlockState().getBlock() instanceof AbstractColonyBlock<?> ? ((AbstractColonyBlock<?>) getBlockState().getBlock()).getBuildingEntry().getRegistryName() : null;
    }

    @Override
    public void updateBlockState()
    {
        // Do nothing
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull final Capability<T> capability, @Nullable final Direction side)
    {
        if (!remove && capability == ForgeCapabilities.ITEM_HANDLER && getBuilding() != null)
        {
            if (combinedInv == null)
            {
                //Add additional containers
                final Set<IItemHandlerModifiable> handlers = new LinkedHashSet<>();
                final Level world = colony.getWorld();
                if (world != null)
                {
                    for (final BlockPos pos : building.getContainers())
                    {
                        if (WorldUtil.isBlockLoaded(world, pos) && !pos.equals(this.worldPosition))
                        {
                            final BlockEntity te = world.getBlockEntity(pos);
                            if (te != null)
                            {
                                if (te instanceof AbstractTileEntityRack)
                                {
                                    handlers.add(((AbstractTileEntityRack) te).getInventory());
                                    ((AbstractTileEntityRack) te).setBuildingPos(this.getBlockPos());
                                }
                                else
                                {
                                    building.removeContainerPosition(pos);
                                }
                            }
                        }
                    }
                }
                handlers.add(this.getInventory());

                combinedInv = LazyOptional.of(() -> new CombinedItemHandler(building.getSchematicName(), handlers.toArray(new IItemHandlerModifiable[0])));
            }
            return (LazyOptional<T>) combinedInv;
        }
        return super.getCapability(capability, side);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int id, @NotNull final Inventory inv, @NotNull final Player player)
    {
        return new ContainerBuildingInventory(id, inv, colonyId, getBlockPos());
    }

    /**
     * Reactivate the hut of this tileEntity.
     * Load the schematic data and set the style correctly.
     */
    public void reactivate()
    {
        final List<String> tags = new ArrayList<>(this.getPositionedTags().get(BlockPos.ZERO));
        tags.remove(DEACTIVATED);
        if (tags.isEmpty())
        {
            this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(this.packMeta, this.path);
            return;
        }

        // First tag on those buildings always has to be the stylename.
        String tagName = tags.get(0);
        final String blueprintPath;
        final String packName;
        if (tagName.contains("/"))
        {
            final String[] split = tagName.split("/");
            packName = split[0];
            blueprintPath = tagName.replace(packName, "");
        }
        else
        {
            final String level = this.getSchematicName().substring(this.getSchematicName().length() - 1);
            packName = BlueprintMapping.getStyleMapping(tagName);
            blueprintPath = BlueprintMapping.getPathMapping(tagName, this.getSchematicName().substring(0, this.getSchematicName().length() - 1)) + level + ".blueprint";
        }

        if (!StructurePacks.hasPack(packName))
        {
            this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(this.packMeta, this.path);
            return;
        }

        this.setStructurePack(StructurePacks.getStructurePack(packName));
        this.pendingBlueprintFuture = StructurePacks.getBlueprintFuture(packName, blueprintPath);
    }

    /**
     * Process the blueprint to read relevant data.
     *
     * @param blueprint the queried blueprint.
     */
    private void processBlueprint(final Blueprint blueprint)
    {
        if (blueprint == null)
        {
            Log.getLogger().error("Invalid building details for reactivation");
            return;
        }

        final BlockState structureState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
        if (structureState != null)
        {
            if (!(structureState.getBlock() instanceof AbstractBlockHut) || !(level.getBlockState(this.getPosition()).getBlock() instanceof AbstractBlockHut))
            {
                Log.getLogger().error(String.format("Schematic %s doesn't have a correct Primary Offset", blueprint.getName()));
                return;
            }
            final int structureRotation = structureState.getValue(AbstractBlockHut.FACING).get2DDataValue();
            final int worldRotation = level.getBlockState(this.getPosition()).getValue(AbstractBlockHut.FACING).get2DDataValue();

            final int rotation;
            if (structureRotation <= worldRotation)
            {
                rotation = worldRotation - structureRotation;
            }
            else
            {
                rotation = 4 + worldRotation - structureRotation;
            }

            blueprint.setRotationMirror(RotationMirror.of(BlockPosUtil.getRotationFromRotations(rotation), this.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE), level);
            final BlockInfo info = blueprint.getBlockInfoAsMap().getOrDefault(blueprint.getPrimaryBlockOffset(), null);

            if (info.getTileEntityData() != null)
            {
                final CompoundTag teCompound = info.getTileEntityData().copy();
                final CompoundTag tagData = teCompound.getCompound(TAG_BLUEPRINTDATA);

                tagData.putString(TAG_PACK, blueprint.getPackName());
                final String location = StructurePacks.getStructurePack(blueprint.getPackName()).getSubPath(blueprint.getFilePath().resolve(blueprint.getFileName()));
                tagData.putString(TAG_NAME, location);
                this.readSchematicDataFromNBT(teCompound);
            }
        }
    }
}
