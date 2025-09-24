package no.monopixel.slimcolonies.core.tileentities;

import no.monopixel.slimcolonies.api.blocks.ModBlocks;
import no.monopixel.slimcolonies.api.colony.IColony;
import no.monopixel.slimcolonies.api.colony.IColonyManager;
import no.monopixel.slimcolonies.api.colony.IColonyView;
import no.monopixel.slimcolonies.api.items.ModItems;
import no.monopixel.slimcolonies.api.tileentities.MinecoloniesTileEntities;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

import static no.monopixel.slimcolonies.api.util.constant.NbtTagConstants.*;

public class TileEntityColonyFlag extends BlockEntity
{
    /** A list of the default banner patterns, for colonies that have not chosen a flag */
    private ListTag patterns = new ListTag();

    /** The colony of the player that placed this banner */
    public int colonyId = -1;

    public TileEntityColonyFlag(final BlockPos pos, final BlockState state) { super(MinecoloniesTileEntities.COLONY_FLAG.get(), pos, state); }

    @Override
    public void saveAdditional(CompoundTag compound)
    {
        super.saveAdditional(compound);

        compound.put(TAG_BANNER_PATTERNS, this.patterns);

        compound.putInt(TAG_COLONY_ID, colonyId);
    }

    @Override
    public void load(CompoundTag compound)
    {
        super.load(compound);

        this.patterns = compound.getList(TAG_BANNER_PATTERNS, 10);
        this.colonyId = compound.getInt(TAG_COLONY_ID);

        if(this.colonyId == -1 && this.hasLevel())
        {
            IColony colony = IColonyManager.getInstance().getIColony(this.getLevel(), worldPosition);
            if (colony != null)
            {
                this.colonyId = colony.getID();
                this.setChanged();
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() { return this.saveWithId(); }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        this.load(compound);
    }

    /**
     * Retrieves the patterns, similar to {@link BannerBlockEntity#getPatterns()}
     * @return the list of pattern-color pairs
     */
    public List<Pair<Holder<BannerPattern>, DyeColor>> getPatterns()
    {
        // Structurize will cause the second condition to be false
        if (level != null && level.dimension() != null)
        {
            IColonyView colony = IColonyManager.getInstance().getColonyView(this.colonyId, level.dimension());
            if (colony != null && this.patterns != colony.getColonyFlag())
            {
                this.patterns = colony.getColonyFlag();
                setChanged();
            }
        }

        List<Pair<Holder<BannerPattern>, DyeColor>> pattern = BannerBlockEntity.createPatterns(
                DyeColor.WHITE,
                this.patterns
        );
	//remove the first base layer
	pattern.remove(0);
    	return pattern;
    }

    /**
     * Builds a mutable ItemStack from the information within the tile entity
     * @return the ItemStack representing this banner
     */
    @OnlyIn(Dist.CLIENT)
    public ItemStack getItemClient()
    {
        ItemStack itemstack = new ItemStack(ModBlocks.blockColonyBanner);
        List<Pair<Holder<BannerPattern>, DyeColor>> list = getPatterns();
        ListTag nbt = new ListTag();

        for (Pair<Holder<BannerPattern>, DyeColor> pair : list)
        {
            CompoundTag pairNBT = new CompoundTag();
            pairNBT.putString(TAG_SINGLE_PATTERN, pair.getFirst().get().getHashname());
            pairNBT.putInt(TAG_PATTERN_COLOR, pair.getSecond().getId());
            nbt.add(pairNBT);
        }

        if (!nbt.isEmpty())
            itemstack.getOrCreateTagElement("BlockEntityTag").put(TAG_BANNER_PATTERNS, nbt);

        return itemstack;
    }

    /**
     * Serverside version of the getItem method.
     * @return the classic stack.
     */
    public ItemStack getItemServer()
    {
        return new ItemStack(ModItems.flagBanner);
    }
}
