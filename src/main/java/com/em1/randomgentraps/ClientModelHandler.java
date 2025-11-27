package com.em1.randomgentraps;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = RandomGenTraps.MODID, value = Side.CLIENT)
public final class ClientModelHandler {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        // Bind the ItemBlock to the item model
        Item item = Item.getItemFromBlock(RandomGenTraps.volatileTNT);
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(RandomGenTraps.MODID + ":volatile_tnt", "inventory")
        );
    }
}