package com.em1.randomgentraps;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;

import com.em1.randomgentraps.EntityVolatileTNT;

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

    public static Block volatileTNT;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "volatile_tnt"),
                EntityVolatileTNT.class,
                "volatile_tnt",
                1,
                this,
                64,
                1,
                true
        );
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new TNTVeinGenerator(), 0);
        MinecraftForge.EVENT_BUS.register(new TNTProximityHandler());
        registerRecipes();
        logger.info("Random TNT Traps generator registered!");
    }

    private void registerRecipes() {
        // 4 TNT + 1 Redstone Block = 2 Volatile TNT
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MODID, "volatile_tnt"),
                null,
                new ItemStack(volatileTNT, 1),
                " T ",
                "TRT",
                " T ",
                'T', new ItemStack(Blocks.TNT),
                'R', new ItemStack(Blocks.REDSTONE_BLOCK)
        );
    }

    public static class TNTVeinGenerator implements IWorldGenerator {

        public void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
            // Only generate in overworld (dimension 0)
            if (world.provider.getDimension() != 0) {
                return;
            }

            // Generate multiple veins at different heights with depth-based probability
            int attempts = ConfigHandler.worldGen.attemptsPerChunkOverworld + random.nextInt(ConfigHandler.worldGen.attemptsPerChunkVarianceOverworld);
            for (int i = 0; i < attempts; i++) {
                int y = ConfigHandler.worldGen.minHeight + random.nextInt(ConfigHandler.worldGen.maxHeight - ConfigHandler.worldGen.minHeight);

                // Calculate chance based on depth (higher chance deeper underground)
                double depthFactor = 1.0 - Math.max(0, Math.min(1, (double)(y - ConfigHandler.worldGen.minHeight) / (ConfigHandler.worldGen.surfaceLevel - ConfigHandler.worldGen.minHeight)));
                double spawnChance = ConfigHandler.worldGen.surfaceChance + (ConfigHandler.worldGen.deepChance - ConfigHandler.worldGen.surfaceChance) * depthFactor;

                if (random.nextDouble() <= spawnChance) {
                    generateTNTVein(world, random, chunkX * 16, chunkZ * 16, y);
                }
            }
        }

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world,
                             IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {

            int dim = world.provider.getDimension();

            // 0 = Overworld (use full logic)
            if (dim == 0) {
                generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
                return;
            }

            // Nether (-1) or End (1) — use simpler random generation
            int attempts = ConfigHandler.worldGen.attemptsPerChunkOtherDims + random.nextInt(ConfigHandler.worldGen.attemptsPerChunkVarianceOtherDims);
            if (dim == -1 || dim == 1) {
                for (int i = 0; i < attempts; i++) {
                    int y = 10 + random.nextInt(100);
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
            int depthBonus = Math.max(0, (ConfigHandler.worldGen.surfaceLevel - targetY) / 10);
            int veinSize = ConfigHandler.worldGen.minVeinSize + random.nextInt(ConfigHandler.worldGen.maxVeinSize - ConfigHandler.worldGen.minVeinSize + 1) + depthBonus;

            // Generate vein using random walk pattern
            BlockPos currentPos = centerPos;
            for (int i = 0; i < veinSize; i++) {
                // Replace solid blocks with TNT
                if (world.getBlockState(currentPos).getMaterial().isSolid()) {
                    world.setBlockState(currentPos, RandomGenTraps.volatileTNT.getDefaultState(), 2);
                }

                // Random walk to next position
                int dx = random.nextInt(3) - 1; // -1, 0, or 1
                int dy = random.nextInt(3) - 1;
                int dz = random.nextInt(3) - 1;
                
                currentPos = currentPos.add(dx, dy, dz);

                // Ensure we stay within reasonable bounds
                if (currentPos.getY() < ConfigHandler.worldGen.minHeight || currentPos.getY() > ConfigHandler.worldGen.maxHeight) {
                    break;
                }
            }

            logger.info("Generated volatile TNT vein at chunk ({}, {}) - Center: {} (Y: {}, Vein Size: {})", chunkX / 16, chunkZ / 16, centerPos, targetY, veinSize);
        }
    }

    public static class TNTProximityHandler {

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
                return;
            }

            EntityPlayer player = event.player;
            World world = player.world;
            BlockPos playerPos = player.getPosition();

            // Check blocks in a radius around the player
            int radius = (int) Math.ceil(ConfigHandler.proximityTrigger.activationRadius);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);
                        
                        // Check if block is TNT and within activation radius
                        if (world.getBlockState(checkPos).getBlock() == RandomGenTraps.volatileTNT) {
                            double distance = player.getDistance(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5);
                            
                            // Only activate if within range, not already activated, AND exposed to air
                            if (distance <= ConfigHandler.proximityTrigger.activationRadius && !activatedTNT.contains(checkPos) && isExposedToAir(world, checkPos)) {
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
            world.setBlockToAir(pos);

            EntityVolatileTNT tnt = new EntityVolatileTNT(world,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5);

            int fuseTicks = ConfigHandler.proximityTrigger.minFuseTicks + world.rand.nextInt(ConfigHandler.proximityTrigger.maxFuseTicks - ConfigHandler.proximityTrigger.minFuseTicks + 1);
            tnt.setFuse(fuseTicks);

            world.spawnEntity(tnt);

            logger.info("Activated volatile TNT at {} with fuse {} ticks", pos, fuseTicks);
        }

    }
}