package com.example.examplemod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;

// Es un Monstruo y puede atacar a distancia (RangedAttackMob)
public class CustomMob extends Monster implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> DATA_IS_DEFENDING =
            SynchedEntityData.defineId(CustomMob.class, EntityDataSerializers.BOOLEAN);

    private int defenseCooldown = 0;

    public CustomMob(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_DEFENDING, false);
    }

    public boolean isDefending() {
        return this.entityData.get(DATA_IS_DEFENDING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - this.getY(0.5D);
        double dz = target.getZ() - this.getZ();
        
        SmallFireball fireball = new SmallFireball(this.level(), this, dx, dy, dz);
        fireball.setPos(this.getX(), this.getY(0.5D), this.getZ());
        this.level().addFreshEntity(fireball);
        // La línea del sonido de disparo ha sido eliminada.
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (this.isDefending()) {
                this.defenseCooldown--;
                if (this.defenseCooldown <= 0) {
                    this.entityData.set(DATA_IS_DEFENDING, false);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isDefending()) {
            // La línea del sonido de bloqueo ha sido eliminada.
            return super.hurt(source, amount * 0.2f);
        }
        if (source.getEntity() instanceof LivingEntity) {
            this.entityData.set(DATA_IS_DEFENDING, true);
            this.defenseCooldown = 100;
            return super.hurt(source, amount);
        }
        return super.hurt(source, amount);
    }
}