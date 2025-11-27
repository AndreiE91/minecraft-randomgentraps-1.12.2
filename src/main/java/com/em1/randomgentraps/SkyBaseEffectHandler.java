package com.em1.randomgentraps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.potion.Potion;

public class SkyBaseEffectHandler {

    private static final int REFRESH_TICKS = 60; // 3 seconds

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (ConfigHandler.skyBase.disable) return;

        EntityPlayer player = event.player;
        if (player == null || player.world.isRemote) return;
        if (player.isCreative() || player.isSpectator()) return;

        if (hasBeaconInfluence(player)) return;

        double y = player.posY;
        boolean isNether = player.world.provider.getDimension() == -1;

        // Nether roof: apply all max effects at once
        if (isNether && y > ConfigHandler.skyBase.netherRoofY) {
            applyEffect(player, MobEffects.HUNGER, 3);
            applyEffect(player, MobEffects.SLOWNESS, 3);
            applyEffect(player, MobEffects.POISON, 3);
            applyEffect(player, MobEffects.WITHER, 0); // Wither I
            return;
        }

        // Below first threshold: nothing
        if (y <= ConfigHandler.skyBase.hungerY) return;

        // Hunger tiers (0-3)
        int hungerAmplifier =
                y >= ConfigHandler.skyBase.slowness4Y ? 3 :
                y >= ConfigHandler.skyBase.slowness3Y ? 2 :
                y >= ConfigHandler.skyBase.slowness2Y ? 1 :
                0;
        applyEffect(player, MobEffects.HUNGER, hungerAmplifier);

        // Slowness tiers start at slowness2Y
        if (y >= ConfigHandler.skyBase.slowness2Y) {
            int slownessAmplifier =
                    y >= ConfigHandler.skyBase.slowness4Y ? 3 :
                    y >= ConfigHandler.skyBase.slowness3Y ? 2 :
                    1;
            applyEffect(player, MobEffects.SLOWNESS, slownessAmplifier);
        }

        // Poison tiers start at poison1Y
        if (y >= ConfigHandler.skyBase.poison1Y) {
            int poisonAmplifier =
                    y >= ConfigHandler.skyBase.poison4Y ? 3 :
                    y >= ConfigHandler.skyBase.poison3Y ? 2 :
                    y >= ConfigHandler.skyBase.poison2Y ? 1 :
                    0;
            applyEffect(player, MobEffects.POISON, poisonAmplifier);
        }

        // Wither starts at witherY
        if (y >= ConfigHandler.skyBase.witherY) {
            applyEffect(player, MobEffects.WITHER, 0); // Wither I
        }
    }

    private void applyEffect(EntityPlayer player, Potion potion, int amplifier) {
        player.addPotionEffect(new PotionEffect(potion, REFRESH_TICKS, amplifier, true, true));
    }

    private boolean hasBeaconInfluence(EntityPlayer player) {
        return isAmbientActive(player, MobEffects.SPEED)
            || isAmbientActive(player, MobEffects.HASTE)
            || isAmbientActive(player, MobEffects.RESISTANCE)
            || isAmbientActive(player, MobEffects.JUMP_BOOST)
            || isAmbientActive(player, MobEffects.STRENGTH)
            || isAmbientActive(player, MobEffects.REGENERATION);
    }

    private boolean isAmbientActive(EntityPlayer player, Potion potion) {
        PotionEffect eff = player.getActivePotionEffect(potion);
        return eff != null && eff.getIsAmbient();
    }
}