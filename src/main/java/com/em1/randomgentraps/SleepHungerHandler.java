package com.em1.randomgentraps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SleepHungerHandler {

    // Block sleeping if hunger isn’t full and notify the player
    @SubscribeEvent
    public void onPlayerAttemptSleep(PlayerSleepInBedEvent event) {
        if (ConfigHandler.sleep.disable) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return;
        if (player.isCreative() || player.isSpectator()) return;

        int currentFood = player.getFoodStats().getFoodLevel();
        if (currentFood < 20) {
            event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
            player.sendMessage(new TextComponentString("You are too hungry to fall asleep, find something to eat first."));
        }
    }

    // After successful sleep, drain hunger by configured amount
    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (ConfigHandler.sleep.disable) return;

        EntityPlayer player = event.getEntityPlayer();
        if (player == null || player.world.isRemote) return;
        if (player.isCreative() || player.isSpectator()) return;

        int drain = Math.max(0, Math.min(20, ConfigHandler.sleep.hungerDrain));
        int newFood = Math.max(0, player.getFoodStats().getFoodLevel() - drain);
        player.getFoodStats().setFoodLevel(newFood);
        player.getFoodStats().setFoodSaturationLevel(0.0F);
    }
}