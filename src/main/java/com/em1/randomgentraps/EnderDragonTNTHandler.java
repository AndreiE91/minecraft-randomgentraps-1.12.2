package com.em1.randomgentraps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.block.state.IBlockState;

public class EnderDragonTNTHandler {

    // Remove hardcoded capitals; use config values
    private final Map<UUID, DragonTNTState> dragonStates = new HashMap<>();
    private final Random random = new Random();

    private static class DragonTNTState {
        int timer = 0;
        int nextInterval;
        int burstsRemaining;
        boolean inBurstMode = false;

        DragonTNTState(int initialInterval, int burstCount) {
            this.nextInterval = initialInterval;
            this.burstsRemaining = burstCount;
            this.inBurstMode = true;
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        // Check if dragon features are disabled
        if (ConfigHandler.dragon.disable) return;

        if (event.phase != TickEvent.Phase.END) return;
        if (event.world.isRemote) return;
        if (event.world.provider.getDimension() != 1) return; // Only in The End

        World world = event.world;
        List<EntityDragon> dragons = world.getEntities(EntityDragon.class, dragon -> !dragon.isDead);

        for (EntityDragon dragon : dragons) {
            // Check health threshold before bombing
            float healthPercent = dragon.getHealth() / dragon.getMaxHealth();
            if (healthPercent > ConfigHandler.dragon.healthThreshold) {
                continue; // Skip this dragon if above threshold
            }

            UUID dragonId = dragon.getUniqueID();
            DragonTNTState state = dragonStates.computeIfAbsent(dragonId, 
                id -> new DragonTNTState(
                    getRandomInterval(dragon),
                    random.nextInt(ConfigHandler.dragon.maxBurstCount - ConfigHandler.dragon.minBurstCount + 1) + ConfigHandler.dragon.minBurstCount
                ));

            state.timer++;

            if (state.timer >= state.nextInterval) {
                launchTNTVolley(world, dragon);
                state.timer = 0;

                if (state.inBurstMode) {
                    state.burstsRemaining--;
                    
                    if (state.burstsRemaining <= 0) {
                        // End burst mode, start long pause
                        state.inBurstMode = false;
                        state.nextInterval = getScaledPauseInterval(dragon);
                    } else {
                        // Continue burst with short interval
                        state.nextInterval = getRandomInterval(dragon);
                    }
                } else {
                    // End pause, start new burst
                    state.inBurstMode = true;
                    state.burstsRemaining = random.nextInt(ConfigHandler.dragon.maxBurstCount - ConfigHandler.dragon.minBurstCount + 1) + ConfigHandler.dragon.minBurstCount;
                    state.nextInterval = getRandomInterval(dragon);
                }
            }
        }

        // Apply effects ONLY if at least one dragon alive AND airborne effects not disabled
        if (!dragons.isEmpty() && ConfigHandler.dragon.airborneEffectsMode > 0) {
            List<EntityPlayer> players = world.getEntities(EntityPlayer.class, p -> !p.isDead);
            for (EntityPlayer player : players) {
                double groundTopY = getTopSolidYBelow(world, player);
                double heightAboveGround = player.posY - groundTopY;

                if (heightAboveGround > 2.0D) {
                    // Apply effects based on mode
                    if (ConfigHandler.dragon.airborneEffectsMode >= 1) {
                        // Mode 1 or 2: Blindness + Nausea
                        player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 40, 1, true, false));
                        player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 40, 0, true, false));
                    }
                    if (ConfigHandler.dragon.airborneEffectsMode >= 2) {
                        // Mode 2: Add Wither + Poison
                        player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 40, 0, true, false));
                        player.addPotionEffect(new PotionEffect(MobEffects.POISON, 40, 0, true, false));
                    }
                } else {
                    // Grounded: remove all airborne effects
                    player.removePotionEffect(MobEffects.NAUSEA);
                    player.removePotionEffect(MobEffects.BLINDNESS);
                    player.removePotionEffect(MobEffects.WITHER);
                    player.removePotionEffect(MobEffects.POISON);
                }
            }
        }

        // Clean up states for dead/despawned dragons
        dragonStates.keySet().removeIf(id -> 
            dragons.stream().noneMatch(dragon -> dragon.getUniqueID().equals(id))
        );
    }

    // Returns the Y coordinate of the top surface (block top + 1) of the highest solid block below the player
    private double getTopSolidYBelow(World world, EntityPlayer player) {
        int startY = (int)Math.floor(player.posY);
        int x = (int)Math.floor(player.posX);
        int z = (int)Math.floor(player.posZ);

        for (int y = startY; y >= 0; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            // Consider solid/full blocks as ground
            if (state.isFullBlock() || state.getMaterial().isSolid()) {
                return y + 1.0D; // top surface Y
            }
        }
        // No solid found below; treat void as infinitely far
        return Double.NEGATIVE_INFINITY;
    }

    private float getSpeedMultiplier(EntityDragon dragon) {
        float healthPercent = Math.max(0F, Math.min(1F, dragon.getHealth() / dragon.getMaxHealth()));
        return 1.0F + (ConfigHandler.dragon.enrageSpeedMultiplier - 1.0F) * (1.0F - healthPercent);
    }

    private int getRandomInterval(EntityDragon dragon) {
        int baseInterval = random.nextInt(ConfigHandler.dragon.maxIntervalTicks - ConfigHandler.dragon.minIntervalTicks + 1) + ConfigHandler.dragon.minIntervalTicks;
        float speed = getSpeedMultiplier(dragon);
        return Math.max(1, (int)(baseInterval / speed));
    }

    private int getScaledPauseInterval(EntityDragon dragon) {
        int basePause = random.nextInt(ConfigHandler.dragon.burstPauseMax - ConfigHandler.dragon.burstPauseMin + 1) + ConfigHandler.dragon.burstPauseMin;
        float speed = getSpeedMultiplier(dragon);
        return Math.max(1, (int)(basePause / speed));
    }

    private void launchTNTVolley(World world, EntityDragon dragon) {
        BlockPos dragonPos = dragon.getPosition();
        double centerX = dragonPos.getX();
        double centerY = dragonPos.getY() + 20;
        double centerZ = dragonPos.getZ();

        int gridSize = random.nextInt(ConfigHandler.dragon.maxGridSize - ConfigHandler.dragon.minGridSize + 1) + ConfigHandler.dragon.minGridSize;
        double spacing = ConfigHandler.dragon.minSpacing + (ConfigHandler.dragon.maxSpacing - ConfigHandler.dragon.minSpacing) * random.nextDouble();

        double gridOffset = (gridSize - 1) * spacing / 2.0;

        for (int x = 0; x < gridSize; x++) {
            for (int z = 0; z < gridSize; z++) {
                double spawnX = centerX - gridOffset + (x * spacing);
                double spawnY = centerY;
                double spawnZ = centerZ - gridOffset + (z * spacing);

                // Random fuse: 0.2s (4 ticks) up to 8.0s (160 ticks)
                int fuseTicks = 4 + random.nextInt(160 - 4 + 1);
                EntityDragonTNT tnt = new EntityDragonTNT(world, spawnX, spawnY, spawnZ, fuseTicks);
                world.spawnEntity(tnt);
            }
        }
    }

    /**
     * Custom TNT entity that only damages players, not blocks or other entities
     */
    public static class EntityDragonTNT extends EntityTNTPrimed {

        private int ageTicks = 0;
        private int levitationTrigger; // tick when gravity stops
        private boolean levitating = false;

        public EntityDragonTNT(World worldIn) {
            super(worldIn);
        }

        public EntityDragonTNT(World worldIn, double x, double y, double z, int fuseTicks) {
            super(worldIn, x, y, z, null);
            this.setFuse(fuseTicks);
            this.levitationTrigger = worldIn.rand.nextInt(fuseTicks + 1); // 0 .. fuseTicks inclusive
        }

        @Override
        public boolean hasNoGravity() {
            return levitating || super.hasNoGravity();
        }

        @Override
        public void onUpdate() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            ageTicks++;

            // Trigger levitation
            if (!levitating && ageTicks >= levitationTrigger) {
                levitating = true;
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
            }

            if (!levitating) {
                if (!this.hasNoGravity()) {
                    this.motionY -= 0.03999999910593033D;
                }
            } else {
                // Keep frozen in air
                this.motionY = 0;
            }

            this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= levitating ? 0.90D : 0.98D;
            this.motionY *= levitating ? 0.90D : 0.98D;
            this.motionZ *= levitating ? 0.90D : 0.98D;

            if (!levitating && this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
                this.motionY *= -0.5D;
            }

            int fuse = this.getFuse() - 1;
            this.setFuse(fuse);

            if (fuse <= 0) {
                this.setDead();
                if (!this.world.isRemote) {
                    customPlayerOnlyExplosion();
                }
            } else {
                this.handleWaterMovement();
            }
        }

        private void customPlayerOnlyExplosion() {
            // Play explosion sound
            this.world.playSound(null, this.posX, this.posY, this.posZ, 
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 
                4.0F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
            
            // Spawn explosion particles
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);
            
            // Manually damage only players in range
            double explosionRange = 8.0D * 2.0D; // power * 2 for radius
            AxisAlignedBB boundingBox = new AxisAlignedBB(
                this.posX - explosionRange, this.posY - explosionRange, this.posZ - explosionRange,
                this.posX + explosionRange, this.posY + explosionRange, this.posZ + explosionRange
            );

            List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, boundingBox);
            
            for (Entity entity : entities) {
                // Only damage players
                if (!(entity instanceof EntityPlayer)) continue;
                
                double distance = entity.getDistance(this.posX, this.posY, this.posZ);
                if (distance > explosionRange) continue;
                
                // Calculate damage based on distance (similar to vanilla TNT)
                double distanceFactor = 1.0D - (distance / explosionRange);
                if (distanceFactor > 0) {
                    float damage = (float)((int)((distanceFactor * distanceFactor + distanceFactor) / 2.0D * 7.0D * (double)8.0F + 1.0D));
                    entity.attackEntityFrom(DamageSource.causeExplosionDamage(this.getTntPlacedBy()), damage);
                    
                    // Apply knockback
                    double knockbackX = entity.posX - this.posX;
                    double knockbackY = entity.posY - this.posY;
                    double knockbackZ = entity.posZ - this.posZ;
                    double knockbackDistance = Math.sqrt(knockbackX * knockbackX + knockbackY * knockbackY + knockbackZ * knockbackZ);
                    
                    if (knockbackDistance != 0.0D) {
                        knockbackX /= knockbackDistance;
                        knockbackY /= knockbackDistance;
                        knockbackZ /= knockbackDistance;
                        double knockbackStrength = distanceFactor;
                        entity.motionX += knockbackX * knockbackStrength;
                        entity.motionY += knockbackY * knockbackStrength;
                        entity.motionZ += knockbackZ * knockbackStrength;
                    }
                }
            }
        }
    }
}