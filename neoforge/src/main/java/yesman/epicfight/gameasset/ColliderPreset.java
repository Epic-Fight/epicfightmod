package yesman.epicfight.gameasset;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.main.EpicFightMod;

public class ColliderPreset implements PreparableReloadListener {
	private static final BiMap<ResourceLocation, Collider> PRESETS = HashBiMap.create();
	
	public static Collider registerCollider(ResourceLocation rl, Collider collider) {
		if (PRESETS.containsKey(rl)) {
			throw new IllegalStateException("Collider named " + rl + " already registered.");
		}
		
		PRESETS.put(rl, collider);
		
		return collider;
	}
	
	public static Set<Map.Entry<ResourceLocation, Collider>> entries() {
		return Collections.unmodifiableSet(PRESETS.entrySet());
	}
	
	public static ResourceLocation getKey(Collider collider) {
		return PRESETS.inverse().get(collider);
	}
	
	public static Collider get(ResourceLocation rl) {
		return PRESETS.get(rl);
	}
	
	public static final Collider DAGGER = registerCollider(EpicFightMod.identifier("dagger"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.6D, 0.0D, 0.0D, -0.1D));
	public static final Collider DUAL_DAGGER_DASH = registerCollider(EpicFightMod.identifier("dual_dagger_dash"), new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D));
	public static final Collider BIPED_BODY_COLLIDER = registerCollider(EpicFightMod.identifier("biped_body_collider"), new MultiOBBCollider(
            new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D),
            new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 1.0D, -0.6D)
    ));
	public static final Collider DRAGON_BODY = registerCollider(EpicFightMod.identifier("dragon_body"), new OBBCollider(2.0D, 1.5D, 4.0D, 0.0D, 1.5D, -0.5D));
	public static final Collider DRAGON_LEG = registerCollider(EpicFightMod.identifier("dragon_leg"), new MultiOBBCollider(3, 0.8D, 1.6D, 0.8D, 0.0D, -0.6D, 0.7D));
	public static final Collider DUAL_SWORD = registerCollider(EpicFightMod.identifier("dual_sword"), new OBBCollider(0.8D, 0.5D, 1.0D, 0.0D, 0.5D, -1.0D));
	public static final Collider DUAL_SWORD_DASH = registerCollider(EpicFightMod.identifier("dual_sword_dash"), new OBBCollider(0.8D, 0.5D, 1.0D, 0D, 1.0D, -1.0D));
	public static final Collider BATTOJUTSU = registerCollider(EpicFightMod.identifier("battojutsu"), new OBBCollider(3.0D, 0.4D, 1.5D, 0.0D, 1.2D, -1.0D));
	public static final Collider BATTOJUTSU_DASH = registerCollider(EpicFightMod.identifier("battojutsu_dash"), new MultiOBBCollider(
            new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
            new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
            new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
            new OBBCollider(0.7D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D),
            new OBBCollider(1.5D, 0.7D, 1.0D, 0.0D, 1.0D, -1.0D)
    ));
	public static final Collider FIST = registerCollider(EpicFightMod.identifier("fist"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.4D, 0D, 0D, 0D));
	public static final Collider GREATSWORD = registerCollider(EpicFightMod.identifier("greatsword"), new MultiOBBCollider(3, 0.5D, 0.8D, 1.0D, 0D, 0D, -1.0D));
	public static final Collider HEAD = registerCollider(EpicFightMod.identifier("head"), new OBBCollider(0.4D, 0.4D, 0.4D, 0D, 0D, -0.3D));
	public static final Collider HEADBUTT_RAVAGER = registerCollider(EpicFightMod.identifier("headbutt_ravager"), new OBBCollider(0.8D, 0.8D, 0.8D, 0D, 0D, -0.3D));
	public static final Collider UCHIGATANA = registerCollider(EpicFightMod.identifier("uchigatana"), new MultiOBBCollider(5, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.7D));
	public static final Collider TACHI = registerCollider(EpicFightMod.identifier("tachi"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.95D, 0D, 0D, -0.95D));
	public static final Collider SWORD = registerCollider(EpicFightMod.identifier("sword"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.7D, 0D, 0D, -0.35D));
	public static final Collider LONGSWORD = registerCollider(EpicFightMod.identifier("longsword"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.8D, 0D, 0D, -0.75D));
	public static final Collider SPEAR = registerCollider(EpicFightMod.identifier("spear"), new MultiOBBCollider(3, 0.6D, 0.6D, 1.0D, 0D, 0D, -1.0D));
	public static final Collider SPIDER = registerCollider(EpicFightMod.identifier("spider"), new OBBCollider(0.8D, 0.8D, 0.8D, 0D, 0D, -0.4D));
	
	public static final Collider STEEL_WHIRLWIND = registerCollider(EpicFightMod.identifier("steel_whirlwind"), new MultiOBBCollider(
            new OBBCollider(1.8D, 0.6D, 1.5D, 0.0D, 1.0D, -0.5D),
            new OBBCollider(1.8D, 0.6D, 1.5D, 0.0D, 1.0D, -0.5D),
            new OBBCollider(1.8D, 0.6D, 1.5D, 0.0D, 1.0D, -0.5D),
            new OBBCollider(1.8D, 0.6D, 1.5D, 0.0D, 1.0D, -0.5D)
    ));
	
	public static final Collider TOOLS = registerCollider(EpicFightMod.identifier("tools"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.55D, 0D, 0.0D, -0.25D));
	public static final Collider ENDERMAN_LIMB = registerCollider(EpicFightMod.identifier("enderman_limb"), new OBBCollider(0.4D, 0.8D, 0.4D, 0D, 0D, 0D));
	public static final Collider GOLEM_SMASHDOWN = registerCollider(EpicFightMod.identifier("golem_smashdown"), new MultiOBBCollider(3, 0.75D, 0.5D, 0.5D, 0.6D, 0.5D, 0D));
	public static final Collider GOLEM_SWING_ARM = registerCollider(EpicFightMod.identifier("golem_swing_arm"), new MultiOBBCollider(2, 0.6D, 0.9D, 0.6D, 0D, 0D, 0D));
	public static final Collider FIST_FIXED = registerCollider(EpicFightMod.identifier("fist_fixed"), new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.25D, -0.85D));
	public static final Collider DUAL_SWORD_AIR_SLASH = registerCollider(EpicFightMod.identifier("dual_sword_air_slash"), new OBBCollider(0.8D, 0.4D, 1.0D, 0D, 0.5D, -0.5D));
	public static final Collider DUAL_DAGGER_AIR_SLASH = registerCollider(EpicFightMod.identifier("dual_dagger_air_slash"), new OBBCollider(0.8D, 0.4D, 0.75D, 0D, 0.5D, -0.5D));
	public static final Collider WITHER_CHARGE = registerCollider(EpicFightMod.identifier("wither_charge"), new MultiOBBCollider(5, 0.7D, 0.9D, 0.7D, 0D, 1.0D, -0.35D));
	public static final Collider VEX_CHARGE = registerCollider(EpicFightMod.identifier("vex_charge"), new MultiOBBCollider(3, 0.4D, 0.4D, 0.95D, 0D, 0.2D, -0.85D));
	
	public static Collider deserializeSimpleCollider(CompoundTag tag) throws IllegalArgumentException {
		int number = tag.getInt("number");
		
		if (number < 1) {
			throw new IllegalArgumentException("Datapack deserialization error: the number of colliders must bigger than 0!");
		}
		
		ListTag sizeVector = tag.getList("size", 6);
		ListTag centerVector = tag.getList("center", 6);
		
		if (sizeVector.size() != 3) {
			throw new IllegalArgumentException("The size list tag must consist of three double elements.");
		}
		
		if (centerVector.size() != 3) {
			throw new IllegalArgumentException("The center list tag must consist of three double elements.");
		}
		
		double sizeX = sizeVector.getDouble(0);
		double sizeY = sizeVector.getDouble(1);
		double sizeZ = sizeVector.getDouble(2);
		
		double centerX = centerVector.getDouble(0);
		double centerY = centerVector.getDouble(1);
		double centerZ = centerVector.getDouble(2);
		
		if (sizeX < 0.0D || sizeY < 0.0D || sizeZ < 0.0D || (sizeX == 0.0D && sizeY == 0.0D && sizeZ == 0.0D)) {
			throw new IllegalArgumentException("Datapack deserialization error: the size of the collider must be non-negative value!");
		}
		
		if (number == 1) {
			return new OBBCollider(sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		} else {
			return new MultiOBBCollider(number, sizeX, sizeY, sizeZ, centerX, centerY, centerZ);
		}
	}
	
	@Override
	public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.runAsync(() -> {
			//Collider newCOllider = new OBBCollider(0.4D, 0.4D, 0.5D, 0D, 1.25D, -0.85D)
			//((AttackAnimation)Animations.FATAL_DRAW_DASH).changeCollider(newCOllider, 0);
		}, gameExecutor).thenCompose(stage::wait);
	}
}