package no.monopixel.slimcolonies.core.network.messages.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import no.monopixel.slimcolonies.api.network.IMessage;
import org.jetbrains.annotations.Nullable;

/**
 * Asks the client to play a specific music
 */
public class PlayAudioMessage implements IMessage
{
    /**
     * The sound event to play.
     */
    private ResourceLocation soundEvent;
    private SoundSource      category;

    /**
     * Default constructor.
     */
    public PlayAudioMessage()
    {
        super();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeVarInt(category.ordinal());
        buf.writeResourceLocation(soundEvent);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        this.category = SoundSource.values()[buf.readVarInt()];
        this.soundEvent = buf.readResourceLocation();
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final Player player = Minecraft.getInstance().player;

        if (player == null)
        {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
            soundEvent, category,
            1.0F, 1.0F, RandomSource.create(), false, 0, SoundInstance.Attenuation.NONE, player.getX(), player.getY(), player.getZ(), true));
    }
}
