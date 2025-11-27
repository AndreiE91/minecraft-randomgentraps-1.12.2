package com.em1.randomgentraps;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = RandomGenTraps.MODID)
public class ConfigHandler {

    @Config.Comment("World Generation Settings")
    @Config.Name("World Generation")
    public static WorldGen worldGen = new WorldGen();

    @Config.Comment("TNT Proximity Trigger Settings")
    @Config.Name("Proximity Trigger")
    public static ProximityTrigger proximityTrigger = new ProximityTrigger();

    @Config.Comment("Explosion Settings")
    @Config.Name("Explosion")
    public static Explosion explosion = new Explosion();

    public static class WorldGen {
        @Config.Comment("Minimum vein size")
        @Config.RangeInt(min = 1, max = 50)
        public int minVeinSize = 3;

        @Config.Comment("Maximum vein size")
        @Config.RangeInt(min = 1, max = 50)
        public int maxVeinSize = 9;

        @Config.Comment("Minimum spawn height")
        @Config.RangeInt(min = 1, max = 255)
        public int minHeight = 5;

        @Config.Comment("Maximum spawn height")
        @Config.RangeInt(min = 1, max = 255)
        public int maxHeight = 250;

        @Config.Comment("Base attempts per chunk in Overworld")
        @Config.RangeInt(min = 0, max = 100)
        public int attemptsPerChunkOverworld = 2;

        @Config.Comment("Random variance for attempts per chunk in Overworld")
        @Config.RangeInt(min = 0, max = 100)
        public int attemptsPerChunkVarianceOverworld = 4;

        @Config.Comment("Base attempts per chunk in other dimensions")
        @Config.RangeInt(min = 0, max = 100)
        public int attemptsPerChunkOtherDims = 1;

        @Config.Comment("Random variance for attempts per chunk in other dimensions")
        @Config.RangeInt(min = 0, max = 100)
        public int attemptsPerChunkVarianceOtherDims = 2;

        @Config.Comment("Surface level (sea level) for depth calculations")
        @Config.RangeInt(min = 1, max = 255)
        public int surfaceLevel = 63;

        @Config.Comment("Spawn chance at surface (0.0 = 0%, 1.0 = 100%)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double surfaceChance = 0.0;

        @Config.Comment("Spawn chance at deep underground (0.0 = 0%, 1.0 = 100%)")
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double deepChance = 1.0;
    }

    public static class ProximityTrigger {
        @Config.Comment("Activation radius in blocks")
        @Config.RangeDouble(min = 0.0, max = 50.0)
        public double activationRadius = 5.0;

        @Config.Comment("Minimum fuse time in ticks (20 ticks = 1 second)")
        @Config.RangeInt(min = 1, max = 200)
        public int minFuseTicks = 1;

        @Config.Comment("Maximum fuse time in ticks (20 ticks = 1 second)")
        @Config.RangeInt(min = 1, max = 200)
        public int maxFuseTicks = 4;
    }

    public static class Explosion {
        @Config.Comment("Explosion strength (vanilla TNT is 4.0)")
        @Config.RangeDouble(min = 0.0, max = 100.0)
        public float explosionStrength = 8.0F;
    }

    @Mod.EventBusSubscriber(modid = RandomGenTraps.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(RandomGenTraps.MODID)) {
                ConfigManager.sync(RandomGenTraps.MODID, Config.Type.INSTANCE);
            }
        }
    }
}