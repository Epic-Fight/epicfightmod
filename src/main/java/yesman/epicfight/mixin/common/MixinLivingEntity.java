package yesman.epicfight.mixin.common;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.entity.EntityRemovedEvent;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPAbsorption;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.HurtableEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

@Mixin(value = LivingEntity.class)
public abstract class MixinLivingEntity {
	@Shadow
	protected abstract void hurtArmor(DamageSource damageSource, float amount);

	@Inject(at = @At(value = "TAIL"), method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V")
	private void epicfight$constructor(EntityType<?> entityType, Level level, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, HurtableEntityPatch.class).ifPresent(entitypatch -> {
			AttributeSupplier.Builder builder = AttributeSupplier.builder();
			
			self.getAttributes().supplier.instances.forEach((k, v) -> {
				builder.add(k, v.getBaseValue());
			});
			
			if (!builder.hasAttribute(Attributes.ATTACK_DAMAGE)) builder.add(Attributes.ATTACK_DAMAGE);
            if (!builder.hasAttribute(EpicFightAttributes.WEIGHT)) builder.add(EpicFightAttributes.WEIGHT);
            if (!builder.hasAttribute(EpicFightAttributes.IMPACT)) builder.add(EpicFightAttributes.IMPACT);
            if (!builder.hasAttribute(EpicFightAttributes.ARMOR_NEGATION)) builder.add(EpicFightAttributes.ARMOR_NEGATION);
            if (!builder.hasAttribute(EpicFightAttributes.MAX_STRIKES)) builder.add(EpicFightAttributes.MAX_STRIKES);
            if (!builder.hasAttribute(EpicFightAttributes.STUN_ARMOR)) builder.add(EpicFightAttributes.STUN_ARMOR);
            if (!builder.hasAttribute(EpicFightAttributes.ASSASSINATION_RESISTANCE)) builder.add(EpicFightAttributes.ASSASSINATION_RESISTANCE);
            if (!builder.hasAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION)) builder.add(EpicFightAttributes.OFFHAND_ARMOR_NEGATION);
            if (!builder.hasAttribute(EpicFightAttributes.OFFHAND_IMPACT)) builder.add(EpicFightAttributes.OFFHAND_IMPACT);
            if (!builder.hasAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES)) builder.add(EpicFightAttributes.OFFHAND_MAX_STRIKES);
            if (!builder.hasAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED)) builder.add(EpicFightAttributes.OFFHAND_ATTACK_SPEED);
			
			self.getAttributes().supplier = builder.build();
		});
	}
	
	@Inject(at = @At(value = "TAIL"), method = "blockUsingShield(Lnet/minecraft/world/entity/LivingEntity;)V", cancellable = true)
	private void epicfight$blockUsingShield(LivingEntity p_21200_, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> opponentEntitypatch = EpicFightCapabilities.getEntityPatch(p_21200_, LivingEntityPatch.class);
		LivingEntityPatch<?> selfEntitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (opponentEntitypatch != null) {
			opponentEntitypatch.setLastAttackResult(AttackResult.blocked(0.0F));
			
			if (selfEntitypatch != null && opponentEntitypatch.getEpicFightDamageSource() != null) {
				opponentEntitypatch.onAttackBlocked(opponentEntitypatch.getEpicFightDamageSource(), selfEntitypatch);
			}
		}
	}
	
	@Inject(at = @At(value = "RETURN"), method = "hurt", cancellable = true)
	private void epicfight$hurt(DamageSource damagesource, float amount, CallbackInfoReturnable<Boolean> info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(damagesource.getEntity(), LivingEntityPatch.class);
		
		if (entitypatch != null) {
			if (info.getReturnValue()) {
				entitypatch.setLastAttackEntity(self);
			}
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "push(Lnet/minecraft/world/entity/Entity;)V", cancellable = true)
	private void epicfight$push(Entity p_20293_, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(self, LivingEntityPatch.class);
		
		if (entitypatch != null && !entitypatch.canPush(p_20293_)) {
			info.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", cancellable = true)
	private void epicfight$getDamageAfterArmorAbsorb(DamageSource source, float amount, CallbackInfoReturnable<Float> info) {
		if (source instanceof EpicFightDamageSource epicFightDamageSource && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
			this.hurtArmor(source, amount);
			float armorNegationAmount = amount * Math.min(epicFightDamageSource.calculateArmorNegation() * 0.01F , 1.0F);
			float amountElse = amount - armorNegationAmount;
			LivingEntity self = (LivingEntity)((Object)this);
			amountElse = CombatRules.getDamageAfterAbsorb(self, amountElse, source, (float)self.getArmorValue(), (float)self.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
			info.setReturnValue(armorNegationAmount + amountElse);
			info.cancel();
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "setAbsorptionAmount(F)V")
	private void epicfight$setAbsorptionAmount(float absorptionAmount, CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		
		if (!self.level().isClientSide()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPAbsorption(self.getId(), absorptionAmount), self);
		}
	}
	
	@Inject(at = @At(value = "TAIL"), method = "makePoofParticles()V")
	private void epicfight$makePoofParticles(CallbackInfo info) {
		LivingEntity self = (LivingEntity)((Object)this);
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(self, LivingEntityPatch.class).ifPresent(entitypatch -> {
            // We needed a hook when an entity is removed by death, makePoofParticles provided a perfect place to achieve it
            EpicFightEventHooks.Entity.ON_REMOVED.postWithListener(new EntityRemovedEvent(Entity.RemovalReason.KILLED, entitypatch), entitypatch.getEventListener());
		});
	}

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
        ),
        method = "jumpFromGround()V"
    )
    private float epicfight$jumpFromGround(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player && player.isLocalPlayer()) {
            EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
            return cameraApi.isTPSMode() ? cameraApi.getCameraYRot() : livingEntity.getYRot();
        }

        return livingEntity.getYRot();
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F",
            ordinal = 0
        ),
        method = "tick()V"
    )
    private float epicfight$tick(LivingEntity livingEntity) {
        // returns the basis y rotation as camera in TPS mode
        if (livingEntity instanceof Player player && player.isLocalPlayer()) {
            return EpicFightCameraAPI.getInstance().getYRotForHead(player);
        }

        return livingEntity.getYRot();
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"
        ),
        method = "tickHeadTurn(FF)F"
    )
    protected float epicfight$tickHeadTurn(LivingEntity livingEntity) {
        // returns the basis y rotation as camera in TPS mode
        if (livingEntity instanceof Player player && player.isLocalPlayer()) {
            return EpicFightCameraAPI.getInstance().getYRotForHead(player);
        }

        return livingEntity.getYRot();
    }
}