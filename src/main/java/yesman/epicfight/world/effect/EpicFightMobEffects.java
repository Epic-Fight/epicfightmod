package yesman.epicfight.world.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

public class EpicFightMobEffects {
	public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, EpicFightMod.MODID);
	
	public static final RegistryObject<MobEffect> STUN_IMMUNITY = EFFECTS.register("stun_immunity", () -> 
		new VisibleMobEffect(MobEffectCategory.BENEFICIAL, 16758016, EpicFightMod.identifier("textures/mob_effect/stun_immunity.png")));
	
	//public static final RegistryObject<MobEffect> BLOOMING = EFFECTS.register("blooming", () -> 
	//	new VisibleMobEffect(MobEffectCategory.BENEFICIAL, 16735744, new ResourceLocation(EpicFightMod.MODID, "textures/mob_effect/blooming.png")));
	
	public static final RegistryObject<MobEffect> INSTABILITY = EFFECTS.register("instability", () -> 
		new VisibleMobEffect(MobEffectCategory.HARMFUL, 0, (effectInstance) -> Math.min(effectInstance.getAmplifier(), 2)
                , EpicFightMod.identifier("textures/mob_effect/instability1.png")
                , EpicFightMod.identifier("textures/mob_effect/instability2.png")
                , EpicFightMod.identifier("textures/mob_effect/instability3.png")));
	
	public static void addOffhandModifier() {
		MobEffects.DIG_SPEED.addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get(), "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
		MobEffects.DIG_SLOWDOWN.addAttributeModifier(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get(), "55FCED67-E92A-486E-9800-B47F202C4386", -0.1D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}