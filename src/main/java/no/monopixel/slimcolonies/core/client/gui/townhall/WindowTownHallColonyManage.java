package no.monopixel.slimcolonies.core.client.gui.townhall;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.structurize.storage.StructurePacks;
import no.monopixel.slimcolonies.core.tileentities.TileEntityColonyBuilding;
import no.monopixel.slimcolonies.core.Network;
import no.monopixel.slimcolonies.core.client.gui.AbstractWindowSkeleton;
import no.monopixel.slimcolonies.core.network.messages.server.CreateColonyMessage;
import no.monopixel.slimcolonies.core.network.messages.client.VanillaParticleMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;

import static no.monopixel.slimcolonies.api.util.constant.Constants.MOD_ID;
import static no.monopixel.slimcolonies.api.util.constant.Constants.TICKS_SECOND;
import static no.monopixel.slimcolonies.api.util.constant.TranslationConstants.*;
import static no.monopixel.slimcolonies.api.util.constant.WindowConstants.*;

/**
 * UI to found a new colony.
 */
public class WindowTownHallColonyManage extends AbstractWindowSkeleton
{
    private static final String BUTTON_CREATE = "create";

    /**
     * Townhall position
     */
    private final BlockPos pos;

    /**
     * If it is a reactivated colony.
     */
    private final boolean reactivate;

    public WindowTownHallColonyManage(final BlockPos pos, final String closestName, final int closestDistance, final String preName, final boolean reactivate)
    {
        super(MOD_ID + TOWNHALL_COLONY_MANAGEMENT_GUI);
        this.pos = pos;
        this.reactivate = reactivate;
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));

        registerButton(BUTTON_CANCEL, this::close);
        registerButton(BUTTON_CREATE, this::onCreate);

        this.findPaneOfTypeByID("colonyname", TextField.class).setText(preName.isEmpty() ? Component.translatable(DEFAULT_COLONY_NAME, mc.player.getName()).getString() : preName);
        this.findPaneOfTypeByID("text1", Text.class)
            .setText(Component.translatable("no.monopixel.slimcolonies.core.settlementcovenant1",
                Math.max(13, Minecraft.getInstance().level.getGameTime() / TICKS_SECOND / 60 / 100)));
        if (closestDistance < 1000)
        {
            this.findPaneOfTypeByID("text3", Text.class)
                .setText(Component.translatable("no.monopixel.slimcolonies.core.settlementcovenant3.hasclose",
                    Component.literal(closestName).withStyle(ChatFormatting.RED),
                    Component.literal(String.valueOf(closestDistance)).withStyle(ChatFormatting.RED)));
        }
        else
        {
            this.findPaneOfTypeByID("text3", Text.class).setText(Component.translatable("no.monopixel.slimcolonies.core.settlementcovenant3.noclose"));
        }
    }

    /**
     * On create button
     */
    public void onCreate()
    {
        final String colonyName = this.findPaneOfTypeByID("colonyname", TextField.class).getText();

        new VanillaParticleMessage(pos.getX(), pos.getY(), pos.getZ(), ParticleTypes.DRAGON_BREATH).onExecute(null, false);
        Minecraft.getInstance().level.playSound(Minecraft.getInstance().player, Minecraft.getInstance().player.blockPosition(),
            SoundEvents.CAMPFIRE_CRACKLE, SoundSource.AMBIENT, 2.5f, 0.8f);
        final BlockEntity entity = Minecraft.getInstance().level.getBlockEntity(pos);

        Network.getNetwork()
            .sendToServer(new CreateColonyMessage(pos,
                reactivate,
                colonyName,
                reactivate ? "" : StructurePacks.selectedPack.getName(),
                ((TileEntityColonyBuilding) entity).getBlueprintPath()));

        close();
    }
}
