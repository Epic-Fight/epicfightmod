package yesman.epicfight.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightParticles {
	public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, EpicFightMod.MODID);
	
	public static final RegistryObject<SimpleParticleType> ASH_DIRECTIONAL = PARTICLES.register("ash_directional", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BLOOD = PARTICLES.register("blood", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> CATHARSIS = PARTICLES.register("catharsis", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> CUT = PARTICLES.register("cut", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> DUST_EXPANSIVE = PARTICLES.register("dust_expansive", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> DUST_CONTRACTIVE = PARTICLES.register("dust_contractive", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> NORMAL_DUST = PARTICLES.register("dust_normal", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> ENDERMAN_DEATH_EMIT = PARTICLES.register("enderman_death_emit", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> GROUND_SLAM = PARTICLES.register("ground_slam", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BREATH_FLAME = PARTICLES.register("breath_flame", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> FORCE_FIELD = PARTICLES.register("force_field", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> FORCE_FIELD_END = PARTICLES.register("force_field_end", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> ADRENALINE_PLAYER_BEATING = PARTICLES.register("adrenaline_player_beating", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> WHITE_AFTERIMAGE = PARTICLES.register("white_afterimage", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> LASER = PARTICLES.register("laser", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> NEUTRALIZE = PARTICLES.register("neutralize", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> BOSS_CASTING = PARTICLES.register("boss_casting", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> TSUNAMI_SPLASH = PARTICLES.register("tsunami_splash", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> FEATHER = PARTICLES.register("feather", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> SWING_TRAIL = PARTICLES.register("swing_trail", () -> new SimpleParticleType(true));
	public static final RegistryObject<SimpleParticleType> PROJECTILE_TRAIL = PARTICLES.register("projectile_trail", () -> new SimpleParticleType(true));
	
	public static final RegistryObject<HitParticleType> AIR_BURST = PARTICLES.register("air_burst", () -> new HitParticleType(true, HitParticleType.MIDDLE_OF_ENTITIES, HitParticleType.ATTACKER_Y_ROTATION));
	public static final RegistryObject<HitParticleType> HIT_BLUNT = PARTICLES.register("hit_blunt", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.ZERO));
	public static final RegistryObject<HitParticleType> HIT_BLADE = PARTICLES.register("hit_blade", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.ZERO));
	public static final RegistryObject<HitParticleType> EVISCERATE = PARTICLES.register("eviscerate", () -> new HitParticleType(true, HitParticleType.CENTER_OF_TARGET, HitParticleType.ATTACKER_XY_ROTATION));
	public static final RegistryObject<HitParticleType> BLADE_RUSH_SKILL = PARTICLES.register("blade_rush", () -> new HitParticleType(true, HitParticleType.RANDOM_WITHIN_BOUNDING_BOX, HitParticleType.ZERO));
}