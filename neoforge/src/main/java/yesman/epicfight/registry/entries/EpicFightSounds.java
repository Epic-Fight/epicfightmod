package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightSounds {
	private EpicFightSounds() {}

	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, EpicFightMod.MODID);

	public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_HIT = registerVariableRangeSound("entity.hit.blade");
	public static final DeferredHolder<SoundEvent, SoundEvent> BLUNT_HIT = registerVariableRangeSound("entity.hit.blunt");
	public static final DeferredHolder<SoundEvent, SoundEvent> BLUNT_HIT_HARD = registerVariableRangeSound("entity.hit.blunt_hard");
	public static final DeferredHolder<SoundEvent, SoundEvent> CLASH = registerVariableRangeSound("entity.hit.clash");
	public static final DeferredHolder<SoundEvent, SoundEvent> EVISCERATE = registerVariableRangeSound("entity.hit.eviscerate");
	public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_RUSH_FINISHER = registerVariableRangeSound("entity.hit.blade_rush_last");
	public static final DeferredHolder<SoundEvent, SoundEvent> OLD_FALL = registerVariableRangeSound("entity.hit.old_fall");
	public static final DeferredHolder<SoundEvent, SoundEvent> SWORD_IN = registerVariableRangeSound("entity.weapon.sword_in");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH = registerVariableRangeSound("entity.weapon.whoosh");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_BIG = registerVariableRangeSound("entity.weapon.whoosh_hard");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_SMALL = registerVariableRangeSound("entity.weapon.whoosh_small");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_SHARP = registerVariableRangeSound("entity.weapon.whoosh_sharp");
	public static final DeferredHolder<SoundEvent, SoundEvent> WHOOSH_ROD = registerVariableRangeSound("entity.weapon.whoosh_rod");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENDER_DRAGON_BREATH = registerVariableRangeSound("entity.enderdragon.dragon_breath");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENDER_DRAGON_BREATH_FINALE = registerVariableRangeSound("entity.enderdragon.dragon_breath_finale");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENDER_DRAGON_CRYSTAL_LINK = registerVariableRangeSound("entity.enderdragon.dragon_crystal_link");
	public static final DeferredHolder<SoundEvent, SoundEvent> WITHER_SPELL_ARMOR = registerVariableRangeSound("entity.wither.wither_spell_armor");
	public static final DeferredHolder<SoundEvent, SoundEvent> NO_SOUND = registerVariableRangeSound("sfx.no_sound");
	public static final DeferredHolder<SoundEvent, SoundEvent> BUZZ = registerVariableRangeSound("sfx.buzz");
	public static final DeferredHolder<SoundEvent, SoundEvent> LASER_BLAST = registerVariableRangeSound("sfx.laser_blast");
	public static final DeferredHolder<SoundEvent, SoundEvent> SLAM_LIGHT = registerVariableRangeSound("sfx.slam_light");
	public static final DeferredHolder<SoundEvent, SoundEvent> SLAM_HEAVY = registerVariableRangeSound("sfx.slam_heavy");
	public static final DeferredHolder<SoundEvent, SoundEvent> NEUTRALIZE_BOSSES = registerVariableRangeSound("sfx.neutralize_bosses");
	public static final DeferredHolder<SoundEvent, SoundEvent> NEUTRALIZE_MOBS = registerVariableRangeSound("sfx.neutralize_mobs");
	public static final DeferredHolder<SoundEvent, SoundEvent> NETHER_STAR_GLITTER = registerVariableRangeSound("sfx.nether_star_glitter");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_MOVE = registerVariableRangeSound("sfx.entity_move");
	public static final DeferredHolder<SoundEvent, SoundEvent> BIG_ENTITY_MOVE = registerVariableRangeSound("sfx.big_entity_move");

	// Skill sounds
	public static final DeferredHolder<SoundEvent, SoundEvent> ADAPTIVE_SKIN_INCREASE = registerVariableRangeSound("skill.adaptive_skin_increase");
	public static final DeferredHolder<SoundEvent, SoundEvent> ADAPTIVE_SKIN_DECREASE = registerVariableRangeSound("skill.adaptive_skin_decrease");
	public static final DeferredHolder<SoundEvent, SoundEvent> ADRENALINE = registerVariableRangeSound("skill.adrenaline");
	public static final DeferredHolder<SoundEvent, SoundEvent> CATHARSIS = registerVariableRangeSound("skill.catharsis");
	public static final DeferredHolder<SoundEvent, SoundEvent> ENDURACNE = registerVariableRangeSound("skill.endurance");
	public static final DeferredHolder<SoundEvent, SoundEvent> EMERGENCY_ESCAPE = registerVariableRangeSound("skill.emergency_escape");
	public static final DeferredHolder<SoundEvent, SoundEvent> FORBIDDEN_STRENGTH = registerVariableRangeSound("skill.forbidden_strength");
	public static final DeferredHolder<SoundEvent, SoundEvent> HYPERVITALITY = registerVariableRangeSound("skill.hypervitality");
	public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_JUMP = registerVariableRangeSound("skill.rocket_jump");
	public static final DeferredHolder<SoundEvent, SoundEvent> ROLL = registerVariableRangeSound("skill.roll");
	public static final DeferredHolder<SoundEvent, SoundEvent> STAMINA_PILLAGER_DEATH = registerVariableRangeSound("skill.stamina_pillager_death");
	public static final DeferredHolder<SoundEvent, SoundEvent> SWORDMASTER_SWING = registerVariableRangeSound("skill.swordmaster");
	public static final DeferredHolder<SoundEvent, SoundEvent> TECHNICIAN = registerVariableRangeSound("skill.technician");
	public static final DeferredHolder<SoundEvent, SoundEvent> TUMBLE = registerVariableRangeSound("skill.tumble");
	public static final DeferredHolder<SoundEvent, SoundEvent> VENGEANCE = registerVariableRangeSound("skill.vengeance");

    // UI Sounds
    public static final DeferredHolder<SoundEvent, SoundEvent> HOVER_WIDGET = registerVariableRangeSound("ui.hover");

	public static DeferredHolder<SoundEvent, SoundEvent> registerVariableRangeSound(String name) {
        ResourceLocation res = EpicFightMod.identifier(name);
		
		return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(res));
	}

	public static DeferredHolder<SoundEvent, SoundEvent> registerFixedRangeSound(String name, float range) {
        ResourceLocation res = EpicFightMod.identifier(name);
		
		return REGISTRY.register(name, () -> SoundEvent.createFixedRangeEvent(res, range));
	}
}