package yesman.epicfight.gameasset;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightSounds {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "epicfight");
	
	// Common sounds
	public static final RegistryObject<SoundEvent> BLADE_HIT = registerSound("entity.hit.blade");
	public static final RegistryObject<SoundEvent> BLUNT_HIT = registerSound("entity.hit.blunt");
	public static final RegistryObject<SoundEvent> BLUNT_HIT_HARD = registerSound("entity.hit.blunt_hard");
	public static final RegistryObject<SoundEvent> CLASH = registerSound("entity.hit.clash");
	public static final RegistryObject<SoundEvent> EVISCERATE = registerSound("entity.hit.eviscerate");
	public static final RegistryObject<SoundEvent> BLADE_RUSH_FINISHER = registerSound("entity.hit.blade_rush_last");
	public static final RegistryObject<SoundEvent> OLD_FALL = registerSound("entity.hit.old_fall");
	public static final RegistryObject<SoundEvent> SWORD_IN = registerSound("entity.weapon.sword_in");
	public static final RegistryObject<SoundEvent> WHOOSH = registerSound("entity.weapon.whoosh");
	public static final RegistryObject<SoundEvent> WHOOSH_BIG = registerSound("entity.weapon.whoosh_hard");
	public static final RegistryObject<SoundEvent> WHOOSH_SMALL = registerSound("entity.weapon.whoosh_small");
	public static final RegistryObject<SoundEvent> WHOOSH_SHARP = registerSound("entity.weapon.whoosh_sharp");
	public static final RegistryObject<SoundEvent> WHOOSH_ROD = registerSound("entity.weapon.whoosh_rod");
	public static final RegistryObject<SoundEvent> ENDER_DRAGON_BREATH = registerSound("entity.enderdragon.dragon_breath");
	public static final RegistryObject<SoundEvent> ENDER_DRAGON_BREATH_FINALE = registerSound("entity.enderdragon.dragon_breath_finale");
	public static final RegistryObject<SoundEvent> ENDER_DRAGON_CRYSTAL_LINK = registerSound("entity.enderdragon.dragon_crystal_link");
	public static final RegistryObject<SoundEvent> WITHER_SPELL_ARMOR = registerSound("entity.wither.wither_spell_armor");
	public static final RegistryObject<SoundEvent> BUZZ = registerSound("sfx.buzz");
	public static final RegistryObject<SoundEvent> LASER_BLAST = registerSound("sfx.laser_blast");
	public static final RegistryObject<SoundEvent> SLAM_LIGHT = registerSound("sfx.slam_light");
	public static final RegistryObject<SoundEvent> SLAM_HEAVY = registerSound("sfx.slam_heavy");
	public static final RegistryObject<SoundEvent> NEUTRALIZE_BOSSES = registerSound("sfx.neutralize_bosses");
	public static final RegistryObject<SoundEvent> NEUTRALIZE_MOBS = registerSound("sfx.neutralize_mobs");
	public static final RegistryObject<SoundEvent> NETHER_STAR_GLITTER = registerSound("sfx.nether_star_glitter");
	public static final RegistryObject<SoundEvent> ENTITY_MOVE = registerSound("sfx.entity_move");
	public static final RegistryObject<SoundEvent> BIG_ENTITY_MOVE = registerSound("sfx.big_entity_move");
	
	// Skill sounds
	public static final RegistryObject<SoundEvent> ADAPTIVE_SKIN_INCREASE = registerSound("skill.adaptive_skin_increase");
	public static final RegistryObject<SoundEvent> ADAPTIVE_SKIN_DECREASE = registerSound("skill.adaptive_skin_decrease");
	public static final RegistryObject<SoundEvent> ADRENALINE = registerSound("skill.adrenaline");
	public static final RegistryObject<SoundEvent> CATHARSIS = registerSound("skill.catharsis");
	public static final RegistryObject<SoundEvent> ENDURACNE = registerSound("skill.endurance");
	public static final RegistryObject<SoundEvent> EMERGENCY_ESCAPE = registerSound("skill.emergency_escape");
	public static final RegistryObject<SoundEvent> FORBIDDEN_STRENGTH = registerSound("skill.forbidden_strength");
	public static final RegistryObject<SoundEvent> HYPERVITALITY = registerSound("skill.hypervitality");
	public static final RegistryObject<SoundEvent> ROCKET_JUMP = registerSound("skill.rocket_jump");
	public static final RegistryObject<SoundEvent> ROLL = registerSound("skill.roll");
	public static final RegistryObject<SoundEvent> STAMINA_PILLAGER_DEATH = registerSound("skill.stamina_pillager_death");
	public static final RegistryObject<SoundEvent> SWORDMASTER_SWING = registerSound("skill.swordmaster");
	public static final RegistryObject<SoundEvent> TECHNICIAN = registerSound("skill.technician");
	public static final RegistryObject<SoundEvent> TUMBLE = registerSound("skill.tumble");
	public static final RegistryObject<SoundEvent> VENGEANCE = registerSound("skill.vengeance");
	
	private static RegistryObject<SoundEvent> registerSound(String name) {
        ResourceLocation res = EpicFightMod.identifier(name);
		return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(res));
	}
}