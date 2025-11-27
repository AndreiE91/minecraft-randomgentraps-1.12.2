package com.em1.randomgentraps;

import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod(modid = RandomGenTraps.MODID, name = RandomGenTraps.NAME, version = RandomGenTraps.VERSION)
public class RandomGenTraps {
    public static final String MODID = "randomgentraps";
    public static final String NAME = "Random Gen Traps";
    public static final String VERSION = "1.0";

    private static Logger logger;
    private static final Set<BlockPos> activatedTNT = new HashSet<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new TNTVeinGenerator(), 0);
        MinecraftForge.EVENT_BUS.register(new TNTProximityHandler());
        logger.info("Random TNT Traps generator registered!");
    }

    public static class TNTVeinGenerator implements IWorldGenerator {
        // Configuration - adjust these values to your preference
        private static final int MIN_VEIN_SIZE = 5;
        private static final int MAX_VEIN_SIZE = 15;
        private static final int MIN_HEIGHT = 5;
        private static final int MAX_HEIGHT = 250;
        
        // Depth-based spawn chances
        private static final int SURFACE_LEVEL = 126; // Sea level
        private static final double SURFACE_CHANCE = 0; // 0% at surface
        private static final double DEEP_CHANCE = 1; // 100% at bedrock

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            // Only generate in overworld (dimension 0)
            if (world.provider.getDimension() != 0) {
                return;
            }

            // Generate multiple veins at different heights with depth-based probability
            int attempts = 5 + random.nextInt(5); // 5-9 attempts per chunk
            for (int i = 0; i < attempts; i++) {
                int y = MIN_HEIGHT + random.nextInt(MAX_HEIGHT - MIN_HEIGHT);
                
                // Calculate chance based on depth (higher chance deeper underground)
                double depthFactor = 1.0 - Math.max(0, Math.min(1, (double)(y - MIN_HEIGHT) / (SURFACE_LEVEL - MIN_HEIGHT)));
                double spawnChance = SURFACE_CHANCE + (DEEP_CHANCE - SURFACE_CHANCE) * depthFactor;
                
                if (random.nextDouble() <= spawnChance) {
                    generateTNTVein(world, random, chunkX * 16, chunkZ * 16, y);
                }
            }
        }

        private void generateTNTVein(World world, Random random, int chunkX, int chunkZ, int targetY) {
            // Random position within chunk
            int x = chunkX + random.nextInt(16);
            int z = chunkZ + random.nextInt(16);

            BlockPos centerPos = new BlockPos(x, targetY, z);

            // Only start generation if there's a solid block at center
            if (!world.getBlockState(centerPos).getMaterial().isSolid()) {
                return;
            }

            // Random vein size (larger veins deeper underground)
            int depthBonus = Math.max(0, (SURFACE_LEVEL - targetY) / 10);
            int veinSize = MIN_VEIN_SIZE + random.nextInt(MAX_VEIN_SIZE - MIN_VEIN_SIZE + 1) + depthBonus;

            // Generate vein using random walk pattern
            BlockPos currentPos = centerPos;
            for (int i = 0; i < veinSize; i++) {
                // Replace solid blocks with TNT
                if (world.getBlockState(currentPos).getMaterial().isSolid()) {
                    world.setBlockState(currentPos, Blocks.TNT.getDefaultState(), 2);
                }

                // Random walk to next position
                int dx = random.nextInt(3) - 1; // -1, 0, or 1
                int dy = random.nextInt(3) - 1;
                int dz = random.nextInt(3) - 1;
                
                currentPos = currentPos.add(dx, dy, dz);

                // Ensure we stay within reasonable bounds
                if (currentPos.getY() < MIN_HEIGHT || currentPos.getY() > MAX_HEIGHT) {
                    break;
                }
            }

            logger.info("Generated TNT vein at chunk ({}, {}) - Center: {} (Y: {}, Vein Size: {})", chunkX / 16, chunkZ / 16, centerPos, targetY, veinSize);
        }
    }

    public static class TNTProximityHandler {
        private static final double ACTIVATION_RADIUS = 5.0;
        private static final int MIN_FUSE_TICKS = 1; // 0.05 seconds (1 tick)
        private static final int MAX_FUSE_TICKS = 4; // 0.2 seconds (4 ticks)

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
                return;
            }

            EntityPlayer player = event.player;
            World world = player.world;
            BlockPos playerPos = player.getPosition();

            // Check blocks in a radius around the player
            int radius = (int) Math.ceil(ACTIVATION_RADIUS);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);
                        
                        // Check if block is TNT and within activation radius
                        if (world.getBlockState(checkPos).getBlock() == Blocks.TNT) {
                            double distance = player.getDistance(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5);
                            
                            // Only activate if within range, not already activated, AND exposed to air
                            if (distance <= ACTIVATION_RADIUS && !activatedTNT.contains(checkPos) && isExposedToAir(world, checkPos)) {
                                activateTNT(world, checkPos);
                                activatedTNT.add(checkPos);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Check if a block position has at least one air block adjacent to it
         */
        private boolean isExposedToAir(World world, BlockPos pos) {
            // Check all 6 adjacent faces
            return world.isAirBlock(pos.up()) ||
                   world.isAirBlock(pos.down()) ||
                   world.isAirBlock(pos.north()) ||
                   world.isAirBlock(pos.south()) ||
                   world.isAirBlock(pos.east()) ||
                   world.isAirBlock(pos.west());
        }

        private void activateTNT(World world, BlockPos pos) {
            // Remove TNT block
            world.setBlockToAir(pos);
            
            // Create primed TNT entity with short fuse
            EntityTNTPrimed entityTNT = new EntityTNTPrimed(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, null);
            
            // Set random fuse between 0.05-0.2 seconds (1-4 ticks)
            int fuseTicks = MIN_FUSE_TICKS + world.rand.nextInt(MAX_FUSE_TICKS - MIN_FUSE_TICKS + 1);
            entityTNT.setFuse(fuseTicks);
            
            world.spawnEntity(entityTNT);
            
            logger.info("Activated TNT at {} with fuse: {} ticks", pos, fuseTicks);
        }
    }
}