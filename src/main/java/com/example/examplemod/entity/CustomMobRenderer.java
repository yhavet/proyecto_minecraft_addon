package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CustomMobRenderer extends MobRenderer<CustomMob, CustomMobModel<CustomMob>> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(ExampleMod.micamod, "textures/entity/custom_mob.png");
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(ExampleMod.micamod, "custom_mob"), "main");

    public CustomMobRenderer(EntityRendererProvider.Context context) {
        super(context, new CustomMobModel<>(context.bakeLayer(LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(CustomMob entity) {
        return TEXTURE;
    }
}