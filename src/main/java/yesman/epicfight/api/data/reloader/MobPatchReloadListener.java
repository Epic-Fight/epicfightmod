package yesman.epicfight.api.data.reloader;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.netty.util.internal.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.entity.HasCustomTag;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightConditions;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.*;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MobPatchReloadListener extends SimpleJsonResourceReloadListener {
	public static final String DIRECTORY = "epicfight_mobpatch";
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<EntityType<?>, CompoundTag> TAGMAP = Maps.newHashMap();
	private static final Map<EntityType<?>, AbstractMobPatchProvider> MOB_PATCH_PROVIDERS = Maps.newHashMap();
	
	public MobPatchReloadListener() {
		super(GSON, DIRECTORY);
	}
	
	@Override
	protected Map<ResourceLocation, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profileIn) {
		MOB_PATCH_PROVIDERS.clear();
		TAGMAP.clear();
		return super.prepare(resourceManager, profileIn);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profilerIn) {
		for (Map.Entry<ResourceLocation, JsonElement> entry : objectIn.entrySet()) {
			ResourceLocation rl = entry.getKey();
			String pathString = rl.getPath();
			ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), pathString);
			
			if (!BuiltInRegistries.ENTITY_TYPE.containsKey(registryName)) {
                EpicFight.LOGGER.warn("Mob Patch Exception: No Entity named {}", registryName);
				continue;
			}
			
			EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(registryName);
			CompoundTag tag = null;
			
			try {
				tag = TagParser.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
                EpicFight.LOGGER.warn("Error while deserializing datapack for {} : {}", registryName, e.getLocalizedMessage());
				continue;
			}
			
			AbstractMobPatchProvider abstractMobpatchProvider = null;
			
			try {
				abstractMobpatchProvider = deserialize(entityType, tag, false, resourceManager);
			} catch (Exception e) {
                EpicFight.LOGGER.warn("Can't deserialize mob capability: {}: {}", registryName, e.getLocalizedMessage());
				continue;
			}
			
			MOB_PATCH_PROVIDERS.put(entityType, abstractMobpatchProvider);
			EpicFightCapabilities.ENTITY_PATCH_PROVIDER.putCustomEntityPatch(entityType, (entity) -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
			TAGMAP.put(entityType, filterClientData(tag));
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
				RenderEngine.getInstance().registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
			}
		}
	}
	
	public static abstract class AbstractMobPatchProvider {
		public abstract EntityPatch<?> get(Entity entity);
	}
	
	public static class NullPatchProvider extends AbstractMobPatchProvider {
		@Override
		public EntityPatch<?> get(Entity entity) {
			return null;
		}
	}
	
	public static class BranchProvider extends AbstractMobPatchProvider {
		protected List<Pair<HasCustomTag, AbstractMobPatchProvider>> providers = Lists.newArrayList();
		protected AbstractMobPatchProvider defaultProvider;
		
		@Override
		public EntityPatch<?> get(Entity entity) {
			for (Pair<HasCustomTag, AbstractMobPatchProvider> provider : this.providers) {
				if (provider.getFirst().predicate(entity)) {
					return provider.getSecond().get(entity);
				}
			}
			
			return this.defaultProvider.get(entity);
		}
	}
	
	public static class MobPatchPresetProvider extends AbstractMobPatchProvider {
		protected final Function<Entity, EntityPatch<?>> presetProvider;
		
		public MobPatchPresetProvider(Function<Entity, EntityPatch<?>> presetProvider) {
			this.presetProvider = presetProvider;
		}
		
		@Override
		public EntityPatch<?> get(Entity entity) {
			return this.presetProvider.apply(entity);
		}
	}
	
	public static class CustomHumanoidMobPatchProvider extends CustomMobPatchProvider {
		protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> humanoidCombatBehaviors;
		protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>>>> humanoidWeaponMotions;
		
		@Override
		public EntityPatch<?> get(Entity entity) {
			if (this.humanoidCombatBehaviors == null && !entity.level().isClientSide()) {
                EpicFight.LOGGER.warn("Custom humanoid mob capability undefined combat behaviors");
				return null;
			}
			
			if (this.humanoidWeaponMotions == null && !entity.level().isClientSide()) {
                EpicFight.LOGGER.warn("Custom humanoid mob capability undefined weapon motions");
				return null;
			}
			
			if (!(entity instanceof PathfinderMob pathfinderMob)) {
                EpicFight.LOGGER.warn(entity.getClass().getSimpleName() + " is not a subtype of Pathfinder Mob");
				return null;
			}
			
			return new CustomHumanoidMobPatch<> (pathfinderMob, this.faction, this);
		}
		
		public Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>>>> getHumanoidWeaponMotions() {
			return this.humanoidWeaponMotions;
		}
		
		public Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> getHumanoidCombatBehaviors() {
			return this.humanoidCombatBehaviors;
		}
	}
	
	public static class CustomMobPatchProvider extends AbstractMobPatchProvider {
		protected CombatBehaviors.Builder<?> combatBehaviorsBuilder;
		protected List<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> defaultAnimations;
		protected Map<StunType, AnimationAccessor<? extends StaticAnimation>> stunAnimations;
		protected Object2DoubleMap<Holder<Attribute>> attributeValues;
		protected Faction faction;
		protected double chasingSpeed = 1.0D;
		protected float scale;
		protected SoundEvent swingSound = EpicFightSounds.WHOOSH.get();
		protected SoundEvent hitSound = EpicFightSounds.BLUNT_HIT.get();
		protected HitParticleType hitParticle = EpicFightParticles.HIT_BLUNT.get();
		
		@Override
		public EntityPatch<?> get(Entity entity) {
			if (this.combatBehaviorsBuilder == null && !entity.level().isClientSide()) {
                EpicFight.LOGGER.warn("Combat behavior undefined for mob capability of " + entity.getClass());
				return null;
			}
			
			if (!(entity instanceof PathfinderMob pathfinderMob)) {
                EpicFight.LOGGER.warn(entity.getClass().getSimpleName() + " is not a subtype of Pathfinder Mob");
				return null;
			}
			
			return new CustomMobPatch<> (pathfinderMob, this.faction, this);
		}

		public CombatBehaviors.Builder<?> getCombatBehaviorsBuilder() {
			return this.combatBehaviorsBuilder;
		}
		
		public List<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> getDefaultAnimations() {
			return this.defaultAnimations;
		}

		public Map<StunType, AnimationAccessor<? extends StaticAnimation>> getStunAnimations() {
			return this.stunAnimations;
		}

		public Object2DoubleMap<Holder<Attribute>> getAttributeValues() {
			return this.attributeValues;
		}
		
		public double getChasingSpeed() {
			return this.chasingSpeed;
		}
		
		public float getScale() {
			return this.scale;
		}
		
		public SoundEvent getSwingSound() {
			return this.swingSound;
		}
		
		public SoundEvent getHitSound() {
			return this.hitSound;
		}
		
		public HitParticleType getHitParticle() {
			return this.hitParticle;
		}
	}
	
	public static AbstractMobPatchProvider deserialize(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager) {
		AbstractMobPatchProvider provider = null;
		int i = 0;
		boolean hasBranch = tag.contains(String.format("branch_%d", i));
		
		if (hasBranch) {
			provider = new BranchProvider();
			((BranchProvider)provider).defaultProvider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
		} else {
			provider = deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager);
		}
		
		while (hasBranch) {
			CompoundTag branchTag = tag.getCompound(String.format("branch_%d", i));
			((BranchProvider)provider).providers.add(Pair.of(deserializeBranchPredicate(branchTag.getCompound("condition")), deserialize(entityType, branchTag, clientSide, resourceManager)));
			hasBranch = tag.contains(String.format("branch_%d", ++i));
		}
		
		return provider;
	}
	
	public static HasCustomTag deserializeBranchPredicate(CompoundTag tag) {
		String predicateType = tag.getString("predicate");
		HasCustomTag predicate = null;
		
		if ("has_tags".equals(predicateType)) {
			if (!tag.contains("tags", 9)) {
                EpicFight.LOGGER.info("Mob capability deserializing exception: Can't find a proper argument for %s. [identifier: %s, type: %s]".formatted("has_tags", "tags", "string list"));
			}
			
			predicate = new HasCustomTag(tag.getList("tags", 8));
		}
		
		if (predicate == null) {
			throw new IllegalArgumentException("Mob capability deserializing exception: No predicate type: " + predicateType);
		}
		
		return predicate;
	}

    public static AbstractMobPatchProvider deserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager) {
        return deserializeMobPatchProvider(entityType, tag, clientSide, resourceManager, null);
    }

    /// @deprecated Use Non-datapack sensitive version. [#deserialize(EntityType, CompoundTag, boolean, ResourceManager)]
    /// @param extraEntryProvider Returns extra-entry created in runtime. (Datapack editor) Exists to access armatures and meshes
    public static AbstractMobPatchProvider deserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager, @Nullable ExtraEntryProvider extraEntryProvider) {
		boolean disabled = tag.contains("disabled") && tag.getBoolean("disabled");
		
		if (disabled) {
			return new NullPatchProvider();
		} else if (tag.contains("preset")) {
			String presetName = tag.getString("preset");
			Function<Entity, EntityPatch<?>> preset = EpicFightCapabilities.ENTITY_PATCH_PROVIDER.get(presetName);
            if (extraEntryProvider == null) Armatures.registerEntityTypeArmatureByPreset(entityType, presetName); // Register armature when it's not loaded from datapack
            return new MobPatchPresetProvider(preset);
		} else {
			boolean humanoid = tag.getBoolean("isHumanoid");
			CustomMobPatchProvider provider = humanoid ? new CustomHumanoidMobPatchProvider() : new CustomMobPatchProvider();
			provider.attributeValues = deserializeAttributes(tag.getCompound("attributes"));
			ResourceLocation modelLocation = ResourceLocation.parse(tag.getString("model"));
			ResourceLocation armatureId = ResourceLocation.parse(tag.getString("armature"));
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
                if (extraEntryProvider == null) Meshes.getOrCreate(modelLocation, (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(humanoid ? SkinnedMesh::new : HumanoidMesh::new)); // Register mesh when it's not loaded from datapack
			}

            if (extraEntryProvider == null) Armatures.registerEntityTypeArmature(entityType, Armatures.getOrCreate(armatureId, Armature::new)); // Register armature when it's not loaded from datapack
			
			provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
			provider.faction = Faction.ENUM_MANAGER.getOrThrow(tag.getString("faction"));
			
			provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;

            if (tag.contains("swing_sound")) {
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(tag.getString("swing_sound")));

                if (soundEvent == null) {
                    EpicFight.LOGGER.warn("Can't find a swing sound {} for the next mot patch: {}", tag.getString("swing_sound"), entityType.toString());
                } else {
                    provider.swingSound = soundEvent;
                }
            }

            if (tag.contains("hit_sound")) {
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(tag.getString("hit_sound")));

                if (soundEvent == null) {
                    EpicFight.LOGGER.warn("Can't find a hit sound {} for the next mot patch: {}", tag.getString("hit_sound"), entityType.toString());
                } else {
                    provider.hitSound = soundEvent;
                }
            }

            if (tag.contains("hit_particle")) {
                HitParticleType hitParticle = (HitParticleType)BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(tag.getString("hit_particle")));

                if (hitParticle == null) {
                    EpicFight.LOGGER.warn("Can't find a hit particle type {} for the next mot patch: {}", tag.getString("hit_particle"), entityType.toString());
                } else {
                    provider.hitParticle = hitParticle;
                }
            }
			
			if (!clientSide) {
				provider.stunAnimations = deserializeStunAnimations(tag.getCompound("stun_animations"));
				
				if (tag.getCompound("attributes").contains("chasing_speed")) {
					provider.chasingSpeed = tag.getCompound("attributes").getDouble("chasing_speed");
				}
				
				if (humanoid) {
					CustomHumanoidMobPatchProvider humanoidProvider = (CustomHumanoidMobPatchProvider)provider;
					humanoidProvider.humanoidCombatBehaviors = deserializeHumanoidCombatBehaviors(tag.getList("combat_behavior", 10));
					humanoidProvider.humanoidWeaponMotions = deserializeHumanoidWeaponMotions(tag.getList("humanoid_weapon_motions", 10));
				} else {
					provider.combatBehaviorsBuilder = deserializeCombatBehaviorsBuilder(tag.getList("combat_behavior", 10));
				}
			}
			
			return provider;
		}
	}
	
	public static Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> deserializeHumanoidCombatBehaviors(ListTag tag) {
		Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> combatBehaviorsMapBuilder = Maps.newHashMap();
		
		for (int i = 0; i < tag.size(); i++) {
			CompoundTag combatBehavior = tag.getCompound(i);
			ListTag categories = combatBehavior.getList("weapon_categories", 8);
			Style style = Style.ENUM_MANAGER.getOrThrow(combatBehavior.getString("style"));
			CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = deserializeCombatBehaviorsBuilder(combatBehavior.getList("behavior_series", 10));
			
			for (int j = 0; j < categories.size(); j++) {
				WeaponCategory category = WeaponCategory.ENUM_MANAGER.getOrThrow(categories.getString(j));
				combatBehaviorsMapBuilder.computeIfAbsent(category, (key) -> Maps.newHashMap());
				combatBehaviorsMapBuilder.get(category).put(style, builder);
			}
		}
		
		return combatBehaviorsMapBuilder;
	}
	
	public static List<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> deserializeDefaultAnimations(CompoundTag defaultLivingmotions) {
		List<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> defaultAnimations = Lists.newArrayList();
		
		for (String key : defaultLivingmotions.getAllKeys()) {
			String animation = defaultLivingmotions.getString(key);
			defaultAnimations.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.byKey(animation)));
		}
		
		return defaultAnimations;
	}
	
	public static Map<StunType, AnimationAccessor<? extends StaticAnimation>> deserializeStunAnimations(CompoundTag tag) {
		Map<StunType, AnimationAccessor<? extends StaticAnimation>> stunAnimations = Maps.newHashMap();
		
		for (StunType stunType : StunType.values()) {
			String lowerCaseName = tag.getString(stunType.name().toLowerCase(Locale.ROOT));
			
			if (!StringUtil.isNullOrEmpty(lowerCaseName)) {
				stunAnimations.put(stunType, AnimationManager.byKey(lowerCaseName));
			}
		}
		
		return stunAnimations;
	}
	
	public static Object2DoubleMap<Holder<Attribute>> deserializeAttributes(CompoundTag tag) {
		Object2DoubleMap<Holder<Attribute>> attributes = new Object2DoubleOpenHashMap<>();
		attributes.put(EpicFightAttributes.IMPACT, tag.contains("impact", Tag.TAG_DOUBLE) ? tag.getDouble("impact") : 0.5D);
		attributes.put(EpicFightAttributes.ARMOR_NEGATION, tag.contains("armor_negation", Tag.TAG_DOUBLE) ? tag.getDouble("armor_negation") : 0.0D);
		attributes.put(EpicFightAttributes.MAX_STRIKES, (double)(tag.contains("max_strikes", Tag.TAG_INT) ? tag.getInt("max_strikes") : 1));
		attributes.put(EpicFightAttributes.STUN_ARMOR, (double)(tag.contains("stun_armor", Tag.TAG_DOUBLE) ? tag.getDouble("stun_armor") : 0.0D));
		
		if (tag.contains("attack_damage", Tag.TAG_DOUBLE)) {
			attributes.put(Attributes.ATTACK_DAMAGE, tag.getDouble("attack_damage"));
		}
		
		return attributes;
	}
	
	public static Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>>>> deserializeHumanoidWeaponMotions(ListTag tag) {
		Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>>>> map = Maps.newHashMap();
		
		for (int i = 0; i < tag.size(); i++) {
			ImmutableSet.Builder<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>> motions = ImmutableSet.builder();
			CompoundTag weaponMotionTag = tag.getCompound(i);
			Style style = Style.ENUM_MANAGER.getOrThrow(weaponMotionTag.getString("style"));
			CompoundTag motionsTag = weaponMotionTag.getCompound("livingmotions");
			
			for (String key : motionsTag.getAllKeys()) {
				motions.add(Pair.of(LivingMotion.ENUM_MANAGER.getOrThrow(key), AnimationManager.byKey(motionsTag.getString(key))));
			}
			
			Tag weponTypeTag = weaponMotionTag.get("weapon_categories");
			
			if (weponTypeTag instanceof StringTag) {
				WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypeTag.getAsString());
				if (!map.containsKey(weaponCategory)) {
					map.put(weaponCategory, Maps.newHashMap());
				}
				map.get(weaponCategory).put(style, motions.build());
				
			} else if (weponTypeTag instanceof ListTag weponTypesTag) {

				for (int j = 0; j < weponTypesTag.size(); j++) {
					WeaponCategory weaponCategory = WeaponCategory.ENUM_MANAGER.getOrThrow(weponTypesTag.getString(j));
					if (!map.containsKey(weaponCategory)) {
						map.put(weaponCategory, Maps.newHashMap());
					}
					map.get(weaponCategory).put(style, motions.build());
				}
			}
		}
		
		return map;
	}
	
	public static <T extends MobPatch<?>> CombatBehaviors.Builder<T> deserializeCombatBehaviorsBuilder(ListTag tag) {
		CombatBehaviors.Builder<T> builder = CombatBehaviors.builder();
		
		for (int i = 0; i < tag.size(); i++) {
			CompoundTag behaviorSeries = tag.getCompound(i);
			float weight = (float)behaviorSeries.getDouble("weight");
			int cooldown = behaviorSeries.contains("cooldown") ? behaviorSeries.getInt("cooldown") : 0;
			boolean canBeInterrupted = behaviorSeries.contains("canBeInterrupted") && behaviorSeries.getBoolean("canBeInterrupted");
			boolean looping = behaviorSeries.contains("looping") && behaviorSeries.getBoolean("looping");
			ListTag behaviorList = behaviorSeries.getList("behaviors", 10);
			BehaviorSeries.Builder<T> behaviorSeriesBuilder = BehaviorSeries.builder();
			behaviorSeriesBuilder.weight(weight).cooldown(cooldown).canBeInterrupted(canBeInterrupted).looping(looping);
			
			for (int j = 0; j < behaviorList.size(); j++) {
				Behavior.Builder<T> behaviorBuilder = Behavior.builder();
				CompoundTag behavior = behaviorList.getCompound(j);
				String animationName = behavior.getString("animation");
				AnimationAccessor<? extends StaticAnimation> animation = AnimationManager.byKey(animationName);
				
				if (animation == null) {
					throw new NoSuchElementException("No animation named " + animationName);
				}
				
				ListTag conditionList = behavior.getList("conditions", 10);
				behaviorBuilder.animationBehavior(animation);
				
				for (int k = 0; k < conditionList.size(); k++) {
					CompoundTag condition = conditionList.getCompound(k);
					Condition<T> predicate = deserializeBehaviorPredicate(condition.getString("predicate"), condition);
					behaviorBuilder.predicate(predicate);
				}
				
				behaviorSeriesBuilder.nextBehavior(behaviorBuilder);
			}
			
			builder.newBehaviorSeries(behaviorSeriesBuilder);
		}
		
		return builder;
	}
	
	public static <T extends MobPatch<?>> Condition<T> deserializeBehaviorPredicate(String type, CompoundTag args) {
		ResourceLocation rl;
		
		if (type.contains(":")) {
			rl = ResourceLocation.parse(type);
		} else {
			rl = EpicFight.identifier(type);
		}
		
		Supplier<Condition<T>> predicateProvider = EpicFightConditions.getConditionOrNull(rl);
		Condition<T> condition = predicateProvider.get();
		condition.read(args);
		
		return condition;
	}
	
	public static CompoundTag filterClientData(CompoundTag tag) {
		CompoundTag clientTag = new CompoundTag();
		int i = 0;
		boolean hasBranch = tag.contains(String.format("branch_%d", i));
		
		while (hasBranch) {
			CompoundTag branchTag = tag.getCompound(String.format("branch_%d", i));
			CompoundTag copiedTag = new CompoundTag();
			extractBranch(copiedTag, branchTag);
			clientTag.put(String.format("branch_%d", i), copiedTag);
			hasBranch = tag.contains(String.format("branch_%d", ++i));
		}
		
		extractBranch(clientTag, tag);
		
		return clientTag;
	}
	
	public static CompoundTag extractBranch(CompoundTag extract, CompoundTag original) {
		if (original.contains("disabled") && original.getBoolean("disabled")) {
			extract.put("disabled", original.get("disabled"));
		} else if (original.contains("preset")) {
			extract.put("preset", original.get("preset"));
		} else {
			extract.put("model", original.get("model"));
			extract.put("armature", original.get("armature"));
			extract.putBoolean("isHumanoid", original.contains("isHumanoid") ? original.getBoolean("isHumanoid") : false);
			extract.put("renderer", original.get("renderer"));
			extract.put("faction", original.get("faction"));
			extract.put("default_livingmotions", original.get("default_livingmotions"));
			if (original.contains("attributes", Tag.TAG_COMPOUND)) extract.put("attributes", original.get("attributes"));
		}
		
		return extract;
	}
	
	public static Stream<CompoundTag> getDataStream() {
		Stream<CompoundTag> tagStream = TAGMAP.entrySet().stream().map((entry) -> {
			entry.getValue().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entry.getKey()).toString());
			return entry.getValue();
		});
		
		return tagStream;
	}
	
	public static void processServerPacket(SPDatapackSync packet) {
		for (CompoundTag tag : packet.tags()) {
			boolean disabled = false;
			
			if (tag.contains("disabled")) {
				disabled = tag.getBoolean("disabled");
			}
			
			EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(tag.getString("id")));
			MOB_PATCH_PROVIDERS.put(entityType, deserialize(entityType, tag, true, Minecraft.getInstance().getResourceManager()));
			EpicFightCapabilities.ENTITY_PATCH_PROVIDER.putCustomEntityPatch(entityType, (entity) -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
			
			if (!disabled) {
				if (tag.contains("preset")) {
					Armatures.registerEntityTypeArmatureByPreset(entityType, tag.getString("preset"));
				} else {
					ResourceLocation armatureLocation = ResourceLocation.parse(tag.getString("armature"));
					boolean humanoid = tag.getBoolean("isHumanoid");
					AssetAccessor<? extends Armature> armature = Armatures.getOrCreate(armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
					Armatures.registerEntityTypeArmature(entityType, armature);
				}
				
				RenderEngine.getInstance().registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
			}
		}
	}
}