package com.example.examplemod.entity;

import com.example.examplemod.ModItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CustomMob extends TamableAnimal implements RangedAttackMob {

    private static final EntityDataAccessor<Boolean> DATA_IS_DEFENDING =
            SynchedEntityData.defineId(CustomMob.class, EntityDataSerializers.BOOLEAN);

    private int defenseCooldown = 0;

    public CustomMob(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_IS_DEFENDING, false);
    }

    public boolean isDefending() {
        return this.entityData.get(DATA_IS_DEFENDING);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 600.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        // ¡IA DE OBJETIVO MODIFICADA!
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Warden.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(
                this, Player.class, true, (player, level) -> !this.isTame() && this.isPlayerLookingAtMe(player)));
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

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !this.isTame()
                && player.getItemInHand(hand).is(ModItems.GOLDEN_BALL.get())) {
            if (!this.level().isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    player.getItemInHand(hand).shrink(1);
                }
                this.tame(player);
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }


    // --- LÓGICA DE ATAQUE DUAL ---
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        // Si el objetivo es un Warden, lanzamos una calavera de Wither.
        if (target instanceof Warden) {
            double dx = target.getX() - this.getX();
            double dy = target.getEyeY() - this.getEyeY();
            double dz = target.getZ() - this.getZ();
            
            WitherSkull skull = new WitherSkull(this.level(), this, new Vec3(dx, dy, dz));
            skull.setPos(this.getX(), this.getEyeY(), this.getZ());
            this.level().addFreshEntity(skull);
        }
        // Si el objetivo es cualquier otra cosa (como un jugador), lanzamos una bola de fuego.
        else {
            double dx = target.getX() - this.getX();
            double dy = target.getY(0.3333333333333333D) - this.getY(0.5D);
            double dz = target.getZ() - this.getZ();
            
            SmallFireball fireball = new SmallFireball(this.level(), this, new Vec3(dx, dy, dz));
            fireball.setPos(this.getX(), this.getY(0.5D), this.getZ());
            this.level().addFreshEntity(fireball);
        }
    }

    // --- MÉTODOS ANTERIORES (SIN CAMBIOS) ---
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.isDefending()) {
                this.defenseCooldown--;
                if (this.defenseCooldown <= 0) {
                    this.entityData.set(DATA_IS_DEFENDING, false);
                }
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isDefending()) {
            return super.hurtServer(level, source, amount * 0.2f);
        }
        if (source.getEntity() instanceof LivingEntity) {
            this.entityData.set(DATA_IS_DEFENDING, true);
            this.defenseCooldown = 100;
            return super.hurtServer(level, source, amount);
        }
        return super.hurtServer(level, source, amount);
    }
}