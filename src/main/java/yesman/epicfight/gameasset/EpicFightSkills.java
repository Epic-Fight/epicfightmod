package yesman.epicfight.gameasset;

import java.util.Set;

import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.api.forgeevent.SkillBuildEvent.ModRegistryWorker;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.BasicAttack;
import yesman.epicfight.skill.BattojutsuPassive;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.Skill.ActivateType;
import yesman.epicfight.skill.Skill.Resource;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.dodge.*;
import yesman.epicfight.skill.guard.*;
import yesman.epicfight.skill.identity.*;
import yesman.epicfight.skill.mover.*;
import yesman.epicfight.skill.passive.*;
import yesman.epicfight.skill.weaponinnate.*;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.ExtraDamageInstance;
import yesman.epicfight.world.damagesource.StunType;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus=EventBusSubscriber.Bus.MOD)
public class EpicFightSkills {
	/** Default skills **/
	public static Skill BASIC_ATTACK;
    public static Skill KNOCKDOWN_WAKEUP;
	/** Dodging skills **/
	public static Skill ROLL;
	public static Skill STEP;
	/** Guard skills **/
	public static Skill GUARD;
	public static Skill PARRYING;
	public static Skill IMPACT_GUARD;
	/** Passive skills **/
	public static Skill ADAPTIVE_SKIN;
	public static Skill ADRENALINE_FIEND;
	public static Skill BERSERKER;
	public static Skill BONEBREAKER;
	public static Skill CATHARSIS;
	public static Skill DEATH_HARVEST;
	public static Skill EMERGENCY_ESCAPE;
	public static Skill ENDURANCE;
	public static Skill FORBIDDEN_STRENGTH;
	public static Skill HYPERVITALITY;
	public static Skill STAMINA_PILLAGER;
	public static Skill SWORD_MASTER;
	public static Skill TECHNICIAN;
	public static Skill VENGEANCE;
	/** Weapon innate skills**/
	public static Skill GUILLOTINE_AXE;
	public static Skill SWEEPING_EDGE;
	public static Skill DANCING_EDGE;
	public static Skill GRASPING_SPIRE;
	public static Skill HEARTPIERCER;
	public static Skill STEEL_WHIRLWIND;
	public static Skill BATTOJUTSU;
	public static Skill BATTOJUTSU_PASSIVE;
	public static Skill RUSHING_TEMPO;
	public static Skill RELENTLESS_COMBO;
	public static Skill SHARP_STAB;
	public static Skill LIECHTENAUER;
	public static Skill EVISCERATE;
	public static Skill BLADE_RUSH;
	public static Skill WRATHFUL_LIGHTING;
	public static Skill TSUNAMI;
	public static Skill EVERLASTING_ALLEGIANCE;
	
	/** Battle style skills **/
	public static Skill METEOR_STRIKE;
	public static Skill REVELATION;
	/** Mover skills **/
	public static Skill DEMOLITION_LEAP;
	public static Skill PHANTOM_ASCENT;
	
	@SubscribeEvent
	public static void buildSkillEvent(SkillBuildEvent build) {
		ModRegistryWorker modRegistry = build.createRegistryWorker(EpicFightMod.MODID);
		
		BASIC_ATTACK = modRegistry.build("basic_attack", BasicAttack::new, BasicAttack.createBasicAttackBuilder().setResource(Resource.STAMINA));
		ROLL = modRegistry.build("roll", DodgeSkill::new, DodgeSkill.createDodgeBuilder().setAnimations(Animations.BIPED_ROLL_FORWARD, Animations.BIPED_ROLL_BACKWARD));
		STEP = modRegistry.build("step", DodgeSkill::new, DodgeSkill.createDodgeBuilder().setAnimations(Animations.BIPED_STEP_FORWARD, Animations.BIPED_STEP_BACKWARD, Animations.BIPED_STEP_LEFT, Animations.BIPED_STEP_RIGHT));
		KNOCKDOWN_WAKEUP = modRegistry.build("knockdown_wakeup", KnockdownWakeupSkill::new, DodgeSkill.createDodgeBuilder().setAnimations(Animations.BIPED_KNOCKDOWN_WAKEUP_LEFT, Animations.BIPED_KNOCKDOWN_WAKEUP_RIGHT).setCategory(SkillCategories.KNOCKDOWN_WAKEUP));
		
		GUARD = modRegistry.build("guard", GuardSkill::new, GuardSkill.createGuardBuilder());
		PARRYING = modRegistry.build("parrying", ParryingSkill::new, ParryingSkill.createActiveGuardBuilder());
		IMPACT_GUARD = modRegistry.build("impact_guard", ImpactGuardSkill::new, ImpactGuardSkill.createEnergizingGuardBuilder());
		
		ADAPTIVE_SKIN = modRegistry.build("adaptive_skin", AdaptiveSkinSkill::new, AdaptiveSkinSkill.createAdaptiveSkinBuilder());
		ADRENALINE_FIEND = modRegistry.build("adrenaline_fiend", AdrenalineFiendSkill::new, PassiveSkill.createPassiveBuilder());
		BERSERKER = modRegistry.build("berserker", BerserkerSkill::new, PassiveSkill.createPassiveBuilder());
		BONEBREAKER = modRegistry.build("bonebreaker", BonebreakerSkill::new, PassiveSkill.createPassiveBuilder());
		CATHARSIS = modRegistry.build("catharsis", CatharsisSkill::new, PassiveSkill.createPassiveBuilder());
		DEATH_HARVEST = modRegistry.build("death_harvest", DeathHarvestSkill::new, PassiveSkill.createPassiveBuilder());
		EMERGENCY_ESCAPE = modRegistry.build("emergency_escape", EmergencyEscapeSkill::new, EmergencyEscapeSkill.createEmergencyEscapeBuilder().addAvailableWeaponCategory(WeaponCategories.FIST, WeaponCategories.SWORD, WeaponCategories.UCHIGATANA, WeaponCategories.DAGGER));
		ENDURANCE = modRegistry.build("endurance", EnduranceSkill::new, PassiveSkill.createPassiveBuilder().setResource(Resource.COOLDOWN).setActivateType(ActivateType.DURATION));
		FORBIDDEN_STRENGTH = modRegistry.build("forbidden_strength", ForbiddenStrengthSkill::new, PassiveSkill.createPassiveBuilder());
		HYPERVITALITY = modRegistry.build("hypervitality", HyperVitalitySkill::new, PassiveSkill.createPassiveBuilder().setResource(Resource.COOLDOWN).setActivateType(ActivateType.TOGGLE));
		STAMINA_PILLAGER = modRegistry.build("stamina_pillager", StaminaPillagerSkill::new, PassiveSkill.createPassiveBuilder());
		SWORD_MASTER = modRegistry.build("swordmaster", SwordmasterSkill::new, SwordmasterSkill.createSwordMasterBuilder());
		TECHNICIAN = modRegistry.build("technician", TechnicianSkill::new, PassiveSkill.createPassiveBuilder());
		VENGEANCE = modRegistry.build("vengeance", VengeanceSkill::new, PassiveSkill.createPassiveBuilder().setActivateType(ActivateType.DURATION));
		
		METEOR_STRIKE = modRegistry.build("meteor_slam", MeteorSlamSkill::new, MeteorSlamSkill.createMeteorSlamBuilder());
		REVELATION = modRegistry.build("revelation", RevelationSkill::new, RevelationSkill.createRevelationSkillBuilder());
		
		DEMOLITION_LEAP = modRegistry.build("demolition_leap", DemolitionLeapSkill::new, Skill.createMoverBuilder().setActivateType(ActivateType.HELD));
		PHANTOM_ASCENT = modRegistry.build("phantom_ascent", PhantomAscentSkill::new, Skill.createMoverBuilder().setResource(Resource.COOLDOWN));
		
		WeaponInnateSkill sweepingEdge = modRegistry.build("sweeping_edge", SimpleWeaponInnateSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.SWEEPING_EDGE));
		sweepingEdge.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20.0F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.6F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		SWEEPING_EDGE = sweepingEdge;
		
		WeaponInnateSkill dancingEdge = modRegistry.build("dancing_edge", SimpleWeaponInnateSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.DANCING_EDGE));
		dancingEdge.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(1))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		DANCING_EDGE = dancingEdge;
		
		WeaponInnateSkill theGuillotine = modRegistry.build("the_guillotine", GuillotineAxeSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.THE_GUILLOTINE));
		theGuillotine.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.5F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(20.0F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		GUILLOTINE_AXE = theGuillotine;
		
		WeaponInnateSkill graspingSpire = modRegistry.build("grasping_spire", GraspingSpireSkill::new, WeaponInnateSkill.createWeaponInnateBuilder());
		graspingSpire.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(3))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(4.0F))
					.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(4))
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.25F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.2F))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		GRASPING_SPIRE = graspingSpire;
		
		WeaponInnateSkill heartpiercer = modRegistry.build("heartpiercer", SimpleWeaponInnateSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.HEARTPIERCER));
		heartpiercer.newProperty()
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(10.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		HEARTPIERCER = heartpiercer;
		
		WeaponInnateSkill steelWhirlwind = modRegistry.build("steel_whirlwind", SteelWhirlwindSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(ActivateType.HELD));
		steelWhirlwind.newProperty()
					  .addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(1.4F))
					  .addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					  .addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		STEEL_WHIRLWIND = steelWhirlwind;
		
		BATTOJUTSU_PASSIVE = modRegistry.build("battojutsu_passive", BattojutsuPassive::new, Skill.createBuilder().setCategory(SkillCategories.WEAPON_PASSIVE).setActivateType(ActivateType.ONE_SHOT).setResource(Resource.COOLDOWN));
		
		WeaponInnateSkill battojutsu = modRegistry.build("battojutsu", BattojutsuSkill::new, ConditionalWeaponInnateSkill.createConditionalWeaponInnateBuilder().setSelector((executor) -> executor.getOriginal().isSprinting() ? 1 : 0).setAnimations(Animations.BATTOJUTSU, Animations.BATTOJUTSU_DASH));
		battojutsu.newProperty()
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(2.0F))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(50.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(6))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		BATTOJUTSU = battojutsu;
		
		WeaponInnateSkill rushingTempo = modRegistry.build("rushing_tempo", RushingTempoSkill::new, WeaponInnateSkill.createWeaponInnateBuilder());
		rushingTempo.newProperty()
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(50.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(2))
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.7F))
					.addProperty(AttackPhaseProperty.SWING_SOUND, EpicFightSounds.WHOOSH_SHARP.get())
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD);
		RUSHING_TEMPO = rushingTempo;
		
		WeaponInnateSkill relentlessCombo = modRegistry.build("relentless_combo", SimpleWeaponInnateSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.RELENTLESS_COMBO));
		relentlessCombo.newProperty()
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.6F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
					.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		RELENTLESS_COMBO = relentlessCombo;
		
		WeaponInnateSkill sharpStab = modRegistry.build("sharp_stab", SimpleWeaponInnateSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.SHARP_STAB));
		sharpStab.newProperty()
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.4F))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(0.5F))
					.addProperty(AttackPhaseProperty.PARTICLE, EpicFightParticles.HIT_BLUNT)
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE, EpicFightDamageTypeTags.GUARD_PUNCTURE));
		SHARP_STAB = sharpStab;
		
		LIECHTENAUER = modRegistry.build("liechtenauer", LiechtenauerSkill::new, WeaponInnateSkill.createWeaponInnateBuilder().setActivateType(ActivateType.DURATION));
		
		WeaponInnateSkill eviscerate = modRegistry.build("eviscerate", EviscerateSkill::new, WeaponInnateSkill.createWeaponInnateBuilder());
		eviscerate.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.addProperty(AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.setter(2.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create(), ExtraDamageInstance.EVISCERATE_LOST_HEALTH.create(0.1F)))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(50.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.LONG);
		EVISCERATE = eviscerate;
		
		WeaponInnateSkill bladeRush = modRegistry.build("blade_rush", BladeRushSkill::new, BladeRushSkill.createBladeRushBuilder());
		bladeRush.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.newProperty()
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.EXECUTION, EpicFightDamageTypeTags.WEAPON_INNATE, DamageTypeTags.BYPASSES_ARMOR))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.NONE)
					.addProperty(AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLADE_RUSH_FINISHER.get());
		BLADE_RUSH = bladeRush;
		
		WeaponInnateSkill wrathfulLighting = modRegistry.build("wrathful_lighting", WrathfulLightingSkill::new, SimpleWeaponInnateSkill.createSimpleWeaponInnateBuilder().setAnimations(Animations.WRATHFUL_LIGHTING));
		wrathfulLighting.newProperty()
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(1))
					.newProperty()
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.setter(8.0F))
					.addProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.setter(3))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(100.0F))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE));
		WRATHFUL_LIGHTING = wrathfulLighting;
		
		WeaponInnateSkill tsunami = modRegistry.build("tsunami", ConditionalWeaponInnateSkill::new, ConditionalWeaponInnateSkill.createConditionalWeaponInnateBuilder().setSelector((executor) -> executor.getOriginal().isInWaterOrRain() ? 1 : 0).setAnimations(Animations.TSUNAMI, Animations.TSUNAMI_REINFORCED));
		tsunami.newProperty()
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(100.0F))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN);
		TSUNAMI = tsunami;
		
		WeaponInnateSkill everlastAllegiance = modRegistry.build("everlasting_allegiance", EverlastingAllegiance::new, WeaponInnateSkill.createWeaponInnateBuilder());
		everlastAllegiance.newProperty()
					.addProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(30.0F))
					.addProperty(AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.4F))
					.addProperty(AttackPhaseProperty.EXTRA_DAMAGE, Set.of(ExtraDamageInstance.SWEEPING_EDGE_ENCHANTMENT.create()))
					.addProperty(AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE))
					.addProperty(AttackPhaseProperty.STUN_TYPE, StunType.HOLD);
		EVERLASTING_ALLEGIANCE = everlastAllegiance;
	}
	
	private EpicFightSkills() {}
}