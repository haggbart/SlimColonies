package no.monopixel.slimcolonies.core.entity.ai.combat;

import no.monopixel.slimcolonies.api.colony.ICitizenData;
import no.monopixel.slimcolonies.api.entity.ModEntities;
import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

/**
 * Utility class of combat functions
 */
public class CombatUtils
{
    /**
     * Shooting constants
     */
    private static final double AIM_HEIGHT                     = 2.0D;
    private static final double ARROW_SPEED                    = 1.4D;
    private static final double AIM_SLIGHTLY_HIGHER_MULTIPLIER = 0.18;
    private static final double SPEED_FOR_DIST                 = 35;

    /**
     * Get an arrow entity for the given shooter
     *
     * @param shooter entity
     * @return arrow entity
     */
    public static AbstractArrow createArrowForShooter(final LivingEntity shooter)
    {
        AbstractArrow arrowEntity = ModEntities.MC_NORMAL_ARROW.create(shooter.level);

        final ItemStack rangedWeapon = shooter.getItemInHand(InteractionHand.MAIN_HAND);
        final Item rangedWeaponItem = rangedWeapon.getItem();

        // Mod compat, some mods expect it to be set before using their Bows to create a custom arrow *looks at TF*
        arrowEntity.setOwner(shooter);
        if (rangedWeaponItem instanceof BowItem)
        {
            arrowEntity = ((BowItem) rangedWeaponItem).customArrow(arrowEntity);
        }
        else if (rangedWeaponItem instanceof TridentItem)
        {
            arrowEntity = EntityType.TRIDENT.create(shooter.level);
        }

        arrowEntity.setOwner(shooter);
        arrowEntity.setPos(shooter.getX(), shooter.getY() + 1, shooter.getZ());
        return arrowEntity;
    }

    /**
     * Shoots a given arrow at the given target with a hit chance
     *
     * @param arrow     the arrow entity to be shot
     * @param target    the target to be shot at
     * @param hitChance the chance the target will be hit
     */
    public static void shootArrow(final AbstractArrow arrow, final LivingEntity target, final float hitChance)
    {
        final double xVector = target.getX() - arrow.getX();
        final double yVector = target.getBoundingBox().minY + target.getBbHeight() / AIM_HEIGHT - arrow.getY();
        final double zVector = target.getZ() - arrow.getZ();
        final double distance = Mth.sqrt((float) (xVector * xVector + zVector * zVector));
        final double dist3d = Mth.sqrt((float) (yVector * yVector + xVector * xVector + zVector * zVector));
        arrow.shoot(xVector, yVector + distance * AIM_SLIGHTLY_HIGHER_MULTIPLIER, zVector, (float) (ARROW_SPEED * 1 + (dist3d / SPEED_FOR_DIST)), (float) hitChance);
        target.level.addFreshEntity(arrow);
    }

    /**
     * Actions on changing to a new target entity
     */
    public static void notifyGuardsOfTarget(final AbstractEntityCitizen user, final LivingEntity target, final int callRange)
    {
        for (final ICitizenData citizen : user.getCitizenData().getWorkBuilding().getAllAssignedCitizen())
        {
            if (citizen.getEntity().isPresent() && citizen.getEntity().get().getLastHurtByMob() == null)
            {
                ((EntityCitizen) citizen.getEntity().get()).getThreatTable().addThreat(target, 0);
            }
        }

    }
}
