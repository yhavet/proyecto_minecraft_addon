package com.example.examplemod.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));

        // ¡IA DE OBJETIVO MODIFICADA!
        // Ataca al Warden sin condiciones.
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        // Ataca al jugador SOLO SI el jugador lo está mirando.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, this::isPlayerLookingAtMe));
    }

    // --- LÓGICA DE "ME ESTÁS MIRANDO?" ---
    private boolean isPlayerLookingAtMe(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        // Si el jugador tiene una calabaza en la cabeza, no lo atacamos.
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (itemstack.is(Items.CARVED_PUMPKIN)) {
            return false;
        }

        // Lógica de vectores para ver si el jugador nos mira.
        Vec3 viewVector = player.getViewVector(1.0F).normalize();
        Vec3 directionToMob = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double distance = directionToMob.length();
        directionToMob = directionToMob.normalize();
        double dotProduct = viewVector.dot(directionToMob);

        // Si el ángulo de visión es muy directo y estamos cerca, nos está mirando.
        return dotProduct > 1.0D - 0.025D / distance && player.hasLineOfSight(this);
    }


    // --- LÓGICA DE ATAQUE DUAL ---
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // Si el objetivo es un Warden, lanzamos una calavera de Wither.
        if (target instanceof Warden) {
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            
            WitherSkull skull = new WitherSkull(this.level(), this, dx, dy, dz);
            skull.setPos(this.getX(), this.getEyeY(), this.getZ());
            this.level().addFreshEntity(skull);
        }
        // Si el objetivo es cualquier otra cosa (como un jugador), lanzamos una bola de fuego.
        else {
            double dx = target.getX() - this.getX();
            double dy = target.getY(0.3333333333333333D) - this.getY(0.5D);
            double dz = target.getZ() - this.getZ();
            
            SmallFireball fireball = new SmallFireball(this.level(), this, dx, dy, dz);
            fireball.setPos(this.getX(), this.getY(0.5D), this.getZ());
            this.level().addFreshEntity(fireball);
        }
    }

    // --- MÉTODOS ANTERIORES (SIN CAMBIOS) ---
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