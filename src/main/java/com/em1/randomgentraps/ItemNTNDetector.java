package com.em1.randomgentraps;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

/**
 * NTN Detector item that changes state (and model) based on proximity
 * to Volatile TNT blocks: green (>10), yellow (<=10), red (<=6).
 */
public class ItemNTNDetector extends Item {

    private static class Cache {
        long tick;
        float value;
    }

    private static final java.util.WeakHashMap<net.minecraft.entity.EntityLivingBase, Cache> CACHE = new java.util.WeakHashMap<>();

    public static final ResourceLocation STATE_PROP = new ResourceLocation(RandomGenTraps.MODID, "state");

    public ItemNTNDetector() {
        setRegistryName(new ResourceLocation(RandomGenTraps.MODID, "ntn_detector"));
        setUnlocalizedName(RandomGenTraps.MODID + ".ntn_detector");
        setCreativeTab(CreativeTabs.REDSTONE);
        setMaxStackSize(1);

        addPropertyOverride(STATE_PROP, (stack, worldIn, entityIn) -> {
            net.minecraft.entity.EntityLivingBase sourceEntity = entityIn;
            net.minecraft.world.World sourceWorld = worldIn;

            // For inventory/hotbar rendering, entity/world can be null; fallback to client player
            if (sourceEntity == null || sourceWorld == null) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
                if (mc != null && mc.player != null) {
                    sourceEntity = mc.player;
                    sourceWorld = mc.world;
                } else {
                    return 0.0F; // default green if no context
                }
            }

            long now = sourceWorld.getTotalWorldTime();
            Cache c = CACHE.get(sourceEntity);
            if (c != null && (now - c.tick) < 5) { // throttle: recompute at most every 5 ticks
                return c.value;
            }
            // Only scan when the item is present in player's hotbar (slots 0-8)
            if (!(sourceEntity instanceof net.minecraft.entity.player.EntityPlayer)) {
                return c != null ? c.value : 0.0F;
            }
            net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) sourceEntity;
            boolean inHotbar = false;
            for (int i = 0; i < 9; i++) {
                net.minecraft.item.ItemStack s = player.inventory.getStackInSlot(i);
                if (!s.isEmpty() && net.minecraft.item.ItemStack.areItemStackTagsEqual(s, stack) && s.getItem() == this) {
                    inHotbar = true;
                    break;
                }
                // Fallback: match by item type only if tag compare fails (e.g., during render copies)
                if (!s.isEmpty() && s.getItem() == this && s.getItemDamage() == stack.getItemDamage()) {
                    inHotbar = true;
                    break;
                }
            }
            if (!inHotbar) {
                float val = c != null ? c.value : 0.0F;
                // Cache the default to avoid rechecking until next window
                if (c == null) {
                    c = new Cache();
                    CACHE.put(sourceEntity, c);
                }
                c.tick = now;
                c.value = val;
                return val;
            }
            BlockPos playerPos = sourceEntity.getPosition();

            int maxRadius = 10;
            double closest = Double.POSITIVE_INFINITY;

            for (int x = -maxRadius; x <= maxRadius; x++) {
                for (int y = -maxRadius; y <= maxRadius; y++) {
                    for (int z = -maxRadius; z <= maxRadius; z++) {
                        BlockPos checkPos = playerPos.add(x, y, z);
                        if (sourceWorld.getBlockState(checkPos).getBlock() == RandomGenTraps.volatileTNT) {
                            double d = sourceEntity.getDistance(checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5);
                            if (d < closest) closest = d;
                            if (closest <= 6.0) {
                                return 2.0F; // red
                            }
                        }
                    }
                }
            }

            float result = 0.0F; // green
            if (closest <= 10.0) result = 1.0F; // yellow
            if (closest <= 6.0) result = 2.0F; // red

            if (c == null) {
                c = new Cache();
                CACHE.put(sourceEntity, c);
            }
            c.tick = now;
            c.value = result;
            return result;
        });
    }
}
