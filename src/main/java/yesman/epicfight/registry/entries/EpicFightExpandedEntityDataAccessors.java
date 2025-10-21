package yesman.epicfight.registry.entries;

import com.mojang.serialization.Codec;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.world.entity.data.ExpandedEntityDataAccessor;

public final class EpicFightExpandedEntityDataAccessors {
	private EpicFightExpandedEntityDataAccessors() {}
	
	public static final DeferredRegister<ExpandedEntityDataAccessor<?>> REGISTRY = DeferredRegister.create(EpicFightRegistries.Keys.EXPANDED_ENTITY_DATA_ACCESSOR, EpicFightMod.MODID);
	
	// LivingEntityPatch
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Float>> STUN_SHIELD = REGISTRY.register("stun_shield", () -> 
		ExpandedEntityDataAccessor.<Float>builder().persistent(Codec.FLOAT.fieldOf("stun_shield")).dataSerializer(ByteBufCodecs.FLOAT).defaultValue(0.0F).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Float>> MAX_STUN_SHIELD = REGISTRY.register("max_stun_shield", () -> 
		ExpandedEntityDataAccessor.<Float>builder().persistent(null).dataSerializer(ByteBufCodecs.FLOAT).defaultValue(0.0F).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Integer>> ASSASSINATION_RESISTANCE = REGISTRY.register("assassination_resistance", () -> 
		ExpandedEntityDataAccessor.<Integer>builder().dataSerializer(ByteBufCodecs.INT).defaultValue(0).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Boolean>> AIRBORNE = REGISTRY.register("airborne", () -> 
		ExpandedEntityDataAccessor.<Boolean>builder().dataSerializer(ByteBufCodecs.BOOL).defaultValue(false).build()
	);
	// PlayerPatch
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Float>> STAMINA = REGISTRY.register("stamina", () -> 
		ExpandedEntityDataAccessor.<Float>builder().persistent(Codec.FLOAT.fieldOf("stamina")).dataSerializer(ByteBufCodecs.FLOAT).defaultValue(0.0F).build()
	);
	// WitherPatch
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Boolean>> WITHER_ARMOR_ACTIVATED = REGISTRY.register("wither_armor_activated", () -> 
		ExpandedEntityDataAccessor.<Boolean>builder().dataSerializer(ByteBufCodecs.BOOL).defaultValue(false).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Boolean>> WITHER_GHOST_MODE = REGISTRY.register("wither_ghost_mode", () -> 
		ExpandedEntityDataAccessor.<Boolean>builder().dataSerializer(ByteBufCodecs.BOOL).defaultValue(false).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Integer>> WITHER_TRANSPARENCY = REGISTRY.register("wither_transparency", () -> 
		ExpandedEntityDataAccessor.<Integer>builder().dataSerializer(ByteBufCodecs.INT).defaultValue(0).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Vec3>> WITHER_HEAD_LEFT_TARGET_LOCATION = REGISTRY.register("wither_head_left_target_location", () -> 
		ExpandedEntityDataAccessor.<Vec3>builder().dataSerializer(ByteBufCodecsExtends.VEC3).defaultValue(new Vec3(Double.NaN, Double.NaN, Double.NaN)).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Vec3>> WITHER_HEAD_CENTER_TARGET_LOCATION = REGISTRY.register("wither_head_center_target_location", () -> 
		ExpandedEntityDataAccessor.<Vec3>builder().dataSerializer(ByteBufCodecsExtends.VEC3).defaultValue(new Vec3(Double.NaN, Double.NaN, Double.NaN)).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Vec3>> WITHER_HEAD_RIGHT_TARGET_LOCATION = REGISTRY.register("wither_head_right_target_location", () -> 
		ExpandedEntityDataAccessor.<Vec3>builder().dataSerializer(ByteBufCodecsExtends.VEC3).defaultValue(new Vec3(Double.NaN, Double.NaN, Double.NaN)).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Integer>> WITHER_HEAD_LEFT_TARGET_ENTITY_ID = REGISTRY.register("wither_head_left_target_entity_id", () -> 
		ExpandedEntityDataAccessor.<Integer>builder().dataSerializer(ByteBufCodecs.INT).defaultValue(-1).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Integer>> WITHER_HEAD_CENTER_TARGET_ENTITY_ID = REGISTRY.register("wither_head_center_target_entity_id", () -> 
		ExpandedEntityDataAccessor.<Integer>builder().dataSerializer(ByteBufCodecs.INT).defaultValue(-1).build()
	);
	public static final DeferredHolder<ExpandedEntityDataAccessor<?>, ExpandedEntityDataAccessor<Integer>> WITHER_HEAD_RIGHT_TARGET_ENTITY_ID = REGISTRY.register("wither_head_right_target_entity_id", () -> 
		ExpandedEntityDataAccessor.<Integer>builder().dataSerializer(ByteBufCodecs.INT).defaultValue(-1).build()
	);
}
