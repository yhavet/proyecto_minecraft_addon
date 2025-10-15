package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.micamod);

    public static final RegistryObject<Item> CUSTOM_MOB_SPAWN_EGG =
            ITEMS.register("custom_mob_spawn_egg",
                    () -> new ForgeSpawnEggItem(ModEntities.CUSTOM_MOB, 0x72B6FB, 0xFFFFFF, new Item.Properties())); 
}