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

    @Config.Comment("Sleep Settings")
    @Config.Name("Sleep")
    public static Sleep sleep = new Sleep();

    @Config.Comment("Sky Base Nerfs")
    @Config.Name("Sky Base")
    public static SkyBase skyBase = new SkyBase();

    @Config.Comment("Ender Dragon TNT Settings")
    @Config.Name("Dragon")
    public static Dragon dragon = new Dragon();

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

    public static class Sleep {
        @Config.Comment("Disable sleep hunger feature entirely")
        public boolean disable = false;

        @Config.Comment("Hunger drain per sleep session (points, 0-20)")
        @Config.RangeInt(min = 0, max = 20)
        public int hungerDrain = 20;
    }

    public static class SkyBase {
        @Config.Comment("Disable sky base effects entirely")
        public boolean disable = false;

        @Config.Comment("Y level for Hunger I baseline")
        @Config.RangeInt(min = 0, max = 255)
        public int hungerY = 130;

        @Config.Comment("Y level for Hunger II + Slowness II")
        @Config.RangeInt(min = 0, max = 255)
        public int slowness2Y = 140;

        @Config.Comment("Y level for Hunger III + Slowness III")
        @Config.RangeInt(min = 0, max = 255)
        public int slowness3Y = 150;

        @Config.Comment("Y level for Hunger IV + Slowness IV")
        @Config.RangeInt(min = 0, max = 255)
        public int slowness4Y = 160;

        @Config.Comment("Y level for Poison I")
        @Config.RangeInt(min = 0, max = 255)
        public int poison1Y = 170;

        @Config.Comment("Y level for Poison II")
        @Config.RangeInt(min = 0, max = 255)
        public int poison2Y = 180;

        @Config.Comment("Y level for Poison III")
        @Config.RangeInt(min = 0, max = 255)
        public int poison3Y = 190;

        @Config.Comment("Y level for Poison IV")
        @Config.RangeInt(min = 0, max = 255)
        public int poison4Y = 200;

        @Config.Comment("Y level for Wither I")
        @Config.RangeInt(min = 0, max = 255)
        public int witherY = 210;

        @Config.Comment("Nether roof trigger Y (apply all at once)")
        @Config.RangeInt(min = 0, max = 255)
        public int netherRoofY = 128;
    }

    public static class Dragon {
        @Config.Comment("Min interval between TNT volleys (ticks)")
        @Config.RangeInt(min = 1, max = 20000)
        public int minIntervalTicks = 60;

        @Config.Comment("Max interval between TNT volleys (ticks)")
        @Config.RangeInt(min = 1, max = 20000)
        public int maxIntervalTicks = 180;

        @Config.Comment("Min bursts per burst mode")
        @Config.RangeInt(min = 1, max = 1000)
        public int minBurstCount = 3;

        @Config.Comment("Max bursts per burst mode")
        @Config.RangeInt(min = 1, max = 1000)
        public int maxBurstCount = 6;

        @Config.Comment("Min pause between burst modes (ticks)")
        @Config.RangeInt(min = 1, max = 200000)
        public int burstPauseMin = 600;

        @Config.Comment("Max pause between burst modes (ticks)")
        @Config.RangeInt(min = 1, max = 200000)
        public int burstPauseMax = 1200;

        @Config.Comment("Min TNT grid size")
        @Config.RangeInt(min = 1, max = 1000)
        public int minGridSize = 10;

        @Config.Comment("Max TNT grid size")
        @Config.RangeInt(min = 1, max = 1000)
        public int maxGridSize = 20;

        @Config.Comment("Min spacing between TNT entities")
        @Config.RangeDouble(min = 0.1, max = 1000.0)
        public double minSpacing = 6.0;

        @Config.Comment("Max spacing between TNT entities")
        @Config.RangeDouble(min = 0.1, max = 1000.0)
        public double maxSpacing = 14.0;

        @Config.Comment("Max speed multiplier at 0% health (1.0 = no scaling)")
        @Config.RangeDouble(min = 1.0, max = 100.0)
        public float enrageSpeedMultiplier = 10.0F;
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