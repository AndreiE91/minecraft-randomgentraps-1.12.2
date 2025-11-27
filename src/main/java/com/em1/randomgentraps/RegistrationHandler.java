package com.em1.randomgentraps;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = RandomGenTraps.MODID)
public class RegistrationHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        RandomGenTraps.volatileTNT = new BlockVolatileTNT()
                .setRegistryName(RandomGenTraps.MODID, "volatile_tnt")
                .setUnlocalizedName("volatile_tnt");
        event.getRegistry().register(RandomGenTraps.volatileTNT);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(
                new ItemBlock(RandomGenTraps.volatileTNT)
                        .setRegistryName(Objects.requireNonNull(RandomGenTraps.volatileTNT.getRegistryName()))
        );
    }
}
