package com.example.examplemod;

import com.example.examplemod.entity.CustomMob;
import com.example.examplemod.entity.CustomMobModel;
import com.example.examplemod.entity.CustomMobRenderer;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.client.renderer.entity.EntityRenderers;

@Mod(ExampleMod.micamod)
public class ExampleMod {
    public static final String micamod = "micamod";

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registramos la entidad y sus items
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        // Registramos nuestros eventos
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerSpawnPlacements);
        modEventBus.addListener(ExampleMod::onRegisterAttributes);

        MinecraftForge.EVENT_BUS.register(this);
    }

    // Este método agrega nuestro huevo a la pestaña de Huevos de Spawn
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.CUSTOM_MOB_SPAWN_EGG);
        }
    }

    // Este método define LAS REGLAS de cómo puede aparecer nuestro mob.
    public void registerSpawnPlacements(final SpawnPlacementRegisterEvent event) {
        event.register(
            ModEntities.CUSTOM_MOB.get(),
            SpawnPlacements.Type.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, // Regla: aparecer en la oscuridad como los zombis
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    @SubscribeEvent
    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CUSTOM_MOB.get(), CustomMob.createAttributes().build());
    }

    @Mod.EventBusSubscriber(modid = micamod, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.CUSTOM_MOB.get(), CustomMobRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(CustomMobRenderer.LAYER_LOCATION, CustomMobModel::createBodyLayer);
        }
    }
}