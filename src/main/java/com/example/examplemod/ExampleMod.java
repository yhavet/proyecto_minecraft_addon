package com.example.examplemod;

import com.example.examplemod.entity.CustomMob;
import com.example.examplemod.entity.CustomMobRenderer;
import com.example.examplemod.loot.GoldenBallLootModifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExampleMod.micamod)
public class ExampleMod {
    public static final String micamod = "micamod";

    public ExampleMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        // Registramos la entidad y sus items
        ModEntities.ENTITIES.register(modBusGroup);
        ModItems.ITEMS.register(modBusGroup);

        // Registramos nuestros eventos
        BuildCreativeModeTabContentsEvent.BUS.addListener(this::addCreative);
        SpawnPlacementRegisterEvent.BUS.addListener(this::registerSpawnPlacements);
        EntityAttributeCreationEvent.BUS.addListener(ExampleMod::onRegisterAttributes);
        RegisterEvent.getBus(modBusGroup).addListener(event -> event.register(
                ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                Identifier.fromNamespaceAndPath(micamod, "add_golden_ball"),
                () -> GoldenBallLootModifier.CODEC
        ));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            EntityRenderersEvent.RegisterRenderers.BUS.addListener(ClientModEvents::registerRenderers);
        }
    }

    // Este método agrega nuestro huevo a la pestaña de Huevos de Spawn
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.CUSTOM_MOB_SPAWN_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GOLDEN_BALL);
        }
    }

    // Este método define LAS REGLAS de cómo puede aparecer nuestro mob.
    public void registerSpawnPlacements(final SpawnPlacementRegisterEvent event) {
        event.register(
            ModEntities.CUSTOM_MOB.get(),
            SpawnPlacementTypes.ON_GROUND,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            Monster::checkMonsterSpawnRules, // Regla: aparecer en la oscuridad como los zombis
            SpawnPlacementRegisterEvent.Operation.REPLACE
        );
    }

    public static void onRegisterAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.CUSTOM_MOB.get(), CustomMob.createAttributes().build());
    }

    public static class ClientModEvents {
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.CUSTOM_MOB.get(), CustomMobRenderer::new);
        }
    }
}