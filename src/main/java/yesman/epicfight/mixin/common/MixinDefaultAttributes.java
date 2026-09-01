package yesman.epicfight.mixin.common;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.platform.fabric.event.EntityAttributeCreationEvent;
import yesman.epicfight.platform.fabric.event.EntityAttributeModificationEvent;

import java.util.Map;

/// Mixin to inject Epic Fight's custom attributes into the vanilla DefaultAttributes system.
/// On NeoForge, this was handled by EntityAttributeCreationEvent and EntityAttributeModificationEvent.
/// On Fabric, we intercept getSupplier() to add our custom attributes to the returned supplier.
@Mixin(value = DefaultAttributes.class)
public abstract class MixinDefaultAttributes {

    @Inject(method = "getSupplier", at = @At("HEAD"), cancellable = true)
    private static void epicfight$getSupplier(EntityType<? extends LivingEntity> entityType, CallbackInfoReturnable<AttributeSupplier> cir) {
        // Check for custom entity attribute suppliers (creation event)
        if (EntityAttributeCreationEvent.hasSupplier(entityType)) {
            AttributeSupplier customSupplier = EntityAttributeCreationEvent.getSupplier(entityType);
            // Apply modifications on top of the custom supplier
            AttributeSupplier modified = applyModifications(entityType, customSupplier);
            cir.setReturnValue(modified);
        }
    }

    @Inject(method = "getSupplier", at = @At("RETURN"), cancellable = true)
    private static void epicfight$getSupplierReturn(EntityType<? extends LivingEntity> entityType, CallbackInfoReturnable<AttributeSupplier> cir) {
        // Don't interfere with custom entities (already handled in HEAD inject)
        if (EntityAttributeCreationEvent.hasSupplier(entityType)) {
            return;
        }

        // For existing entities, apply modifications to the vanilla supplier
        if (EntityAttributeModificationEvent.hasModifications(entityType)) {
            AttributeSupplier vanillaSupplier = cir.getReturnValue();
            AttributeSupplier modified = applyModifications(entityType, vanillaSupplier);
            cir.setReturnValue(modified);
        }
    }

    /// Apply pending attribute modifications to a supplier.
    /// Uses AttributeSupplier.builder() to create a new supplier that combines the base
    /// attributes with our custom Epic Fight attributes.
    private static AttributeSupplier applyModifications(EntityType<?> entityType, AttributeSupplier base) {
        Map<Holder<Attribute>, Double> mods = EntityAttributeModificationEvent.getPendingModifications().get(entityType);
        if (mods == null) {
            return base;
        }

        AttributeSupplier.Builder builder = AttributeSupplier.builder();

        // Copy existing attribute base values from the base supplier
        for (Map.Entry<Holder<Attribute>, AttributeInstance> entry : base.instances.entrySet()) {
            builder.add(entry.getKey(), entry.getValue().getBaseValue());
        }

        // Add our custom Epic Fight attributes
        for (Map.Entry<Holder<Attribute>, Double> entry : mods.entrySet()) {
            if (!base.hasAttribute(entry.getKey())) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }

        return builder.build();
    }
}
