package com.em1.randomgentraps;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityVolatileTNT extends EntityTNTPrimed {

    public EntityVolatileTNT(World world) {
        super(world);
    }

    public EntityVolatileTNT(World world, double x, double y, double z) {
        super(world, x, y, z, null);
    }

    @Override
    public void onUpdate() {
        if (this.getFuse() > 0) {
            this.setFuse(this.getFuse() - 1);
            this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY + 0.5, posZ, 0, 0, 0);
        } else {
            this.setDead();

            // Use configured explosion strength
            this.world.createExplosion(this,
                    this.posX,
                    this.posY,
                    this.posZ,
                    ConfigHandler.explosion.explosionStrength,
                    true
            );
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setShort("Fuse", (short) this.getFuse());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        this.setFuse(compound.getShort("Fuse"));
    }
}
