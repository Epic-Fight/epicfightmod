package yesman.epicfight.skill;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.utils.PacketBufferCodec;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.skill.guard.ImpactGuardSkill;
import yesman.epicfight.skill.guard.ParryingSkill;
import yesman.epicfight.skill.identity.MeteorSlamSkill;
import yesman.epicfight.skill.identity.RevelationSkill;
import yesman.epicfight.skill.mover.DemolitionLeapSkill;
import yesman.epicfight.skill.mover.PhantomAscentSkill;
import yesman.epicfight.skill.weaponinnate.BattojutsuSkill;
import yesman.epicfight.skill.weaponinnate.BladeRushSkill;
import yesman.epicfight.skill.weaponinnate.EverlastingAllegiance;
import yesman.epicfight.skill.weaponinnate.GraspingSpireSkill;
import yesman.epicfight.skill.weaponinnate.SteelWhirlwindSkill;

public class SkillDataKeys {
	private static final Supplier<RegistryBuilder<SkillDataKey<?>>> BUILDER = () -> new RegistryBuilder<SkillDataKey<?>>().addCallback(SkillDataKey.getRegistryCallback());
	
	public static final DeferredRegister<SkillDataKey<?>> DATA_KEYS = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "skill_data_keys"), EpicFightMod.MODID);
	public static final Supplier<IForgeRegistry<SkillDataKey<?>>> REGISTRY = DATA_KEYS.makeRegistry(BUILDER);
	
	/**
	 * Check COMBO_COUNTER > 0 to judge if basic attack is activated
	 */
	@Deprecated(forRemoval = true, since = "1.21.1")
	public static final RegistryObject<SkillDataKey<Boolean>> BASIC_ATTACK_ACTIVATE = DATA_KEYS.register("basic_attack_active", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, BasicAttack.class));
	public static final RegistryObject<SkillDataKey<Integer>> COMBO_COUNTER = DATA_KEYS.register("combo_counter", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, BasicAttack.class, BladeRushSkill.class));
	public static final RegistryObject<SkillDataKey<Boolean>> SHEATH = DATA_KEYS.register("sheath", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, BattojutsuPassive.class, BattojutsuSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> PENALTY_RESTORE_COUNTER = DATA_KEYS.register("penalty_restore_counter", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, GuardSkill.class));
	public static final RegistryObject<SkillDataKey<Float>> PENALTY = DATA_KEYS.register("penalty", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.FLOAT, 0.0F, GuardSkill.class, ImpactGuardSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> LAST_ACTIVE = DATA_KEYS.register("last_active", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, ParryingSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> PARRY_MOTION_COUNTER = DATA_KEYS.register("parry_motion_counter", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, ParryingSkill.class));
	public static final RegistryObject<SkillDataKey<Float>> FALL_DISTANCE = DATA_KEYS.register("fall_distance", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.FLOAT, 0.0F, MeteorSlamSkill.class));
	public static final RegistryObject<SkillDataKey<Boolean>> PROTECT_NEXT_FALL = DATA_KEYS.register("slam_protect_next_fall", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, MeteorSlamSkill.class, DemolitionLeapSkill.class, PhantomAscentSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> STACKS = DATA_KEYS.register("stacks", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, RevelationSkill.class));
	public static final RegistryObject<SkillDataKey<Boolean>> JUMP_KEY_PRESSED_LAST_TICK = DATA_KEYS.register("jump_key_pressed_last_tick", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.BOOLEAN, false, PhantomAscentSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> JUMP_COUNT = DATA_KEYS.register("jump_count", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, PhantomAscentSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> THROWN_TRIDENT_ENTITY_ID = DATA_KEYS.register("thrown_trident_entity_id", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, -1, EverlastingAllegiance.class));
	public static final RegistryObject<SkillDataKey<Integer>> LAST_HIT_COUNT = DATA_KEYS.register("last_hit_count", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, GraspingSpireSkill.class));
	public static final RegistryObject<SkillDataKey<Integer>> CHARGING_POWER = DATA_KEYS.register("charging_power", () -> SkillDataKey.createSkillDataKey(PacketBufferCodec.INTEGER, 0, SteelWhirlwindSkill.class));
}