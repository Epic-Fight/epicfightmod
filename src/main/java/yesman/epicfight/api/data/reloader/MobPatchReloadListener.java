package yesman.epicfight.api.data.reloader;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.data.conditions.entity.HasCustomTag;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.CustomHumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.CustomMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.Behavior;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors.BehaviorSeries;

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
			
			if (!ForgeRegistries.ENTITY_TYPES.containsKey(registryName)) {
				EpicFightMod.LOGGER.warn("Mob Patch Exception: No Entity named " + registryName);
				continue;
			}
			
			EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(registryName);
			CompoundTag tag = null;
			
			try {
				tag = TagParser.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
				EpicFightMod.LOGGER.warn("Error while deserializing datapack for " + registryName + ": " + e.getLocalizedMessage());
				continue;
			}
			
			AbstractMobPatchProvider abstractMobpatchProvider = null;
			
			try {
				abstractMobpatchProvider = deserialize(entityType, tag, false, resourceManager);
			} catch (Exception e) {
				EpicFightMod.LOGGER.warn("Can't deserialize mob capability: " + registryName + ": " + e.getLocalizedMessage());
				continue;
			}
			
			MOB_PATCH_PROVIDERS.put(entityType, abstractMobpatchProvider);
			EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
			TAGMAP.put(entityType, filterClientData(tag));
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
				ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
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
		protected final Function<Entity, Supplier<EntityPatch<?>>> presetProvider;
		
		public MobPatchPresetProvider(Function<Entity, Supplier<EntityPatch<?>>> presetProvider) {
			this.presetProvider = presetProvider;
		}
		
		@Override
		public EntityPatch<?> get(Entity entity) {
			return this.presetProvider.apply(entity).get();
		}
	}
	
	public static class CustomHumanoidMobPatchProvider extends CustomMobPatchProvider {
		protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> humanoidCombatBehaviors;
		protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, AnimationAccessor<? extends StaticAnimation>>>>> humanoidWeaponMotions;
		
		@SuppressWarnings("rawtypes")
		@Override
		public EntityPatch<?> get(Entity entity) {
			if (this.humanoidCombatBehaviors == null && !entity.level().isClientSide()) {
				EpicFightMod.LOGGER.warn("Custom humanoid mob capability undefined combat behaviors");
				return null;
			}
			
			if (this.humanoidWeaponMotions == null && !entity.level().isClientSide()) {
				EpicFightMod.LOGGER.warn("Custom humanoid mob capability undefined weapon motions");
				return null;
			}
			
			return new CustomHumanoidMobPatch(this.faction, this);
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
		protected Object2DoubleMap<Attribute> attributeValues;
		protected Faction faction;
		protected double chasingSpeed = 1.0D;
		protected float scale;
		protected SoundEvent swingSound = EpicFightSounds.WHOOSH.get();
		protected SoundEvent hitSound = EpicFightSounds.BLUNT_HIT.get();
		protected HitParticleType hitParticle = EpicFightParticles.HIT_BLUNT.get();
		
		@Override
		@SuppressWarnings("rawtypes")
		public EntityPatch<?> get(Entity entity) {
			if (this.combatBehaviorsBuilder == null && !entity.level().isClientSide()) {
				EpicFightMod.LOGGER.warn("Combat behavior undefined for mob capability of " + entity.getClass());
				return null;
			}
			
			return new CustomMobPatch(this.faction, this);
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

		public Object2DoubleMap<Attribute> getAttributeValues() {
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
				EpicFightMod.LOGGER.info("Mob capability deserializing exception: Can't find a proper argument for %s. [name: %s, type: %s]".formatted("has_tags", "tags", "string list"));
			}
			
			predicate = new HasCustomTag(tag.getList("tags", 8));
		}
		
		if (predicate == null) {
			throw new IllegalArgumentException("Mob capability deserializing exception: No predicate type: " + predicateType);
		}
		
		return predicate;
	}
	
	public static AbstractMobPatchProvider deserializeMobPatchProvider(EntityType<?> entityType, CompoundTag tag, boolean clientSide, ResourceManager resourceManager) {
		boolean disabled = tag.contains("disabled") && tag.getBoolean("disabled");
		
		if (disabled) {
			return new NullPatchProvider();
		} else if (tag.contains("preset")) {
			String presetName = tag.getString("preset");
			Function<Entity, Supplier<EntityPatch<?>>> preset = EntityPatchProvider.get(presetName);
			Armatures.registerEntityTypeArmatureByPreset(entityType, presetName);
			MobPatchPresetProvider provider = new MobPatchPresetProvider(preset);
			return provider;
		} else {
			boolean humanoid = tag.getBoolean("isHumanoid");
			CustomMobPatchProvider provider = humanoid ? new CustomHumanoidMobPatchProvider() : new CustomMobPatchProvider();
			provider.attributeValues = deserializeAttributes(tag.getCompound("attributes"));
			ResourceLocation modelLocation = ResourceLocation.parse(tag.getString("model"));
			ResourceLocation armatureId = ResourceLocation.parse(tag.getString("armature"));
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
				Meshes.getOrCreate(modelLocation, (jsonAssetLoader) -> jsonAssetLoader.loadSkinnedMesh(humanoid ? SkinnedMesh::new : HumanoidMesh::new));
			}
			
			Armatures.registerEntityTypeArmature(entityType, Armatures.getOrCreate(armatureId, Armature::new));
			
			provider.defaultAnimations = deserializeDefaultAnimations(tag.getCompound("default_livingmotions"));
			provider.faction = Faction.ENUM_MANAGER.getOrThrow(tag.getString("faction"));
			
			provider.scale = tag.getCompound("attributes").contains("scale") ? (float)tag.getCompound("attributes").getDouble("scale") : 1.0F;
			
			if (tag.contains("swing_sound")) {
				provider.swingSound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("swing_sound")));
			}
			
			if (tag.contains("hit_sound")) {
				provider.hitSound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("hit_sound")));
			}
			
			if (tag.contains("hit_particle")) {
				provider.hitParticle = (HitParticleType)ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.getString("hit_particle")));
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
	
	public static Object2DoubleMap<Attribute> deserializeAttributes(CompoundTag tag) {
		Object2DoubleMap<Attribute> attributes = new Object2DoubleOpenHashMap<>();
		attributes.put(EpicFightAttributes.IMPACT.get(), tag.contains("impact", Tag.TAG_DOUBLE) ? tag.getDouble("impact") : 0.5D);
		attributes.put(EpicFightAttributes.ARMOR_NEGATION.get(), tag.contains("armor_negation", Tag.TAG_DOUBLE) ? tag.getDouble("armor_negation") : 0.0D);
		attributes.put(EpicFightAttributes.MAX_STRIKES.get(), (double)(tag.contains("max_strikes", Tag.TAG_INT) ? tag.getInt("max_strikes") : 1));
		attributes.put(EpicFightAttributes.STUN_ARMOR.get(), (double)(tag.contains("stun_armor", Tag.TAG_DOUBLE) ? tag.getDouble("stun_armor") : 0.0D));
		
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
			rl = ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, type);
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
			entry.getValue().putString("id", ForgeRegistries.ENTITY_TYPES.getKey(entry.getKey()).toString());
			return entry.getValue();
		});
		
		return tagStream;
	}
	
	public static int getTagCount() {
		return TAGMAP.size();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		for (CompoundTag tag : packet.getTags()) {
			boolean disabled = false;
			
			if (tag.contains("disabled")) {
				disabled = tag.getBoolean("disabled");
			}
			
			EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(tag.getString("id")));
			MOB_PATCH_PROVIDERS.put(entityType, deserialize(entityType, tag, true, Minecraft.getInstance().getResourceManager()));
			EntityPatchProvider.putCustomEntityPatch(entityType, (entity) -> () -> MOB_PATCH_PROVIDERS.get(entity.getType()).get(entity));
			
			if (!disabled) {
				if (tag.contains("preset")) {
					Armatures.registerEntityTypeArmatureByPreset(entityType, tag.getString("preset"));
				} else {
					ResourceLocation armatureLocation = ResourceLocation.parse(tag.getString("armature"));
					boolean humanoid = tag.getBoolean("isHumanoid");
					AssetAccessor<? extends Armature> armature = Armatures.getOrCreate(armatureLocation, humanoid ? Armature::new : HumanoidArmature::new);
					Armatures.registerEntityTypeArmature(entityType, armature);
				}
				
				ClientEngine.getInstance().renderEngine.registerCustomEntityRenderer(entityType, tag.contains("preset") ? tag.getString("preset") : tag.getString("renderer"), tag);
			}
		}
	}
}