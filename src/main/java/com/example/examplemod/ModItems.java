package com.example.examplemod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.micamod);

    public static final RegistryObject<Item> CUSTOM_MOB_SPAWN_EGG =
            ITEMS.register("custom_mob_spawn_egg",
                    () -> new SpawnEggItem(new Item.Properties()
                            .setId(ITEMS.key("custom_mob_spawn_egg"))
                            .spawnEgg(ModEntities.CUSTOM_MOB.get())));

    public static final RegistryObject<Item> GOLDEN_BALL =
            ITEMS.register("golden_ball",
                    () -> new Item(new Item.Properties()
                            .setId(ITEMS.key("golden_ball"))
                            .rarity(Rarity.RARE)
                            .stacksTo(1)));
}