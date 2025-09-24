package no.monopixel.slimcolonies.api.entity;

import no.monopixel.slimcolonies.api.entity.citizen.AbstractEntityCitizen;
import no.monopixel.slimcolonies.api.entity.other.MinecoloniesMinecart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.*;

public class ModEntities
{
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    public static EntityType<? extends AbstractEntityCitizen> VISITOR;

    public static EntityType<? extends Projectile> FISHHOOK;



    public static EntityType<? extends Entity> SITTINGENTITY;


    public static EntityType<MinecoloniesMinecart> MINECART;

    public static EntityType<? extends Arrow> MC_NORMAL_ARROW;



}
