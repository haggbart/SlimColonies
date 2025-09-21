package com.minecolonies.api.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.other.MinecoloniesMinecart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.projectile.*;

import java.util.List;

public class ModEntities
{
    public static EntityType<? extends AbstractEntityCitizen> CITIZEN;

    public static EntityType<? extends AbstractEntityCitizen> VISITOR;

    public static EntityType<? extends Projectile> FISHHOOK;

    public static EntityType<? extends PathfinderMob> MERCENARY;


    public static EntityType<? extends Entity> SITTINGENTITY;


    public static EntityType<MinecoloniesMinecart> MINECART;

    public static EntityType<? extends AbstractArrow> FIREARROW;

    public static EntityType<? extends Arrow> MC_NORMAL_ARROW;

    public static EntityType<? extends ThrownPotion> DRUID_POTION;

    public static EntityType<? extends ThrownTrident> SPEAR;


}
