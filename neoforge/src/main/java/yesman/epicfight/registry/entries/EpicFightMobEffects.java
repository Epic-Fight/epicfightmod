package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.EpicFight;
import yesman.epicfight.main.EpicFightNeoForge;
import yesman.epicfight.world.effect.VisibleMobEffect;

public final class EpicFightMobEffects {
	private EpicFightMobEffects() {}
	
	public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, EpicFight.MODID);
	
	public static final DeferredHolder<MobEffect, VisibleMobEffect> STUN_IMMUNITY = REGISTRY.register("stun_immunity", () -> 
		new VisibleMobEffect(
				MobEffectCategory.BENEFICIAL
				, 16758016
				, EpicFight.identifier("textures/mob_effect/stun_immunity.png")
		)
	);
	
	//public static final RegistryObject<MobEffect> BLOOMING = EFFECTS.register("blooming", () -> 
	//	new VisibleMobEffect(MobEffectCategory.BENEFICIAL, 16735744, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/blooming.png")));
	
	public static final DeferredHolder<MobEffect, VisibleMobEffect> INSTABILITY = REGISTRY.register("instability", () -> 
		new VisibleMobEffect(
				MobEffectCategory.HARMFUL
				, 0
				, (effectInstance) ->
				Math.min(effectInstance.getAmplifier(), 2)
				, EpicFight.identifier("textures/mob_effect/instability1.png")
				, EpicFight.identifier("textures/mob_effect/instability2.png")
				, EpicFight.identifier("textures/mob_effect/instability3.png")
		)
	);
	
	public static void addOffhandModifier() {
		MobEffects.DIG_SPEED.value().addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED, EpicFight.identifier("offhand_dig_modifier"), 0.1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		MobEffects.DIG_SLOWDOWN.value().addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED, EpicFight.identifier("offhand_dig_modifier"), -0.1D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}