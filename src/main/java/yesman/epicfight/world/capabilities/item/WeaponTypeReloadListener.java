package yesman.epicfight.world.capabilities.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;

import io.netty.util.internal.StringUtil;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.forgeevent.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.data.conditions.EpicFightConditions;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.provider.ExtraEntryProvider;

public class WeaponTypeReloadListener extends SimpleJsonResourceReloadListener {
	public static void registerDefaultWeaponTypes() {
		Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> typeEntry = Maps.newHashMap();
		typeEntry.put(EpicFightMod.identifier("axe"), WeaponCapabilityPresets.AXE);
		typeEntry.put(EpicFightMod.identifier("fist"), WeaponCapabilityPresets.FIST);
		typeEntry.put(EpicFightMod.identifier("hoe"), WeaponCapabilityPresets.HOE);
		typeEntry.put(EpicFightMod.identifier("pickaxe"), WeaponCapabilityPresets.PICKAXE);
		typeEntry.put(EpicFightMod.identifier("shovel"), WeaponCapabilityPresets.SHOVEL);
		typeEntry.put(EpicFightMod.identifier("sword"), WeaponCapabilityPresets.SWORD);
		typeEntry.put(EpicFightMod.identifier("spear"), WeaponCapabilityPresets.SPEAR);
		typeEntry.put(EpicFightMod.identifier("greatsword"), WeaponCapabilityPresets.GREATSWORD);
		typeEntry.put(EpicFightMod.identifier("uchigatana"), WeaponCapabilityPresets.UCHIGATANA);
		typeEntry.put(EpicFightMod.identifier("tachi"), WeaponCapabilityPresets.TACHI);
		typeEntry.put(EpicFightMod.identifier("longsword"), WeaponCapabilityPresets.LONGSWORD);
		typeEntry.put(EpicFightMod.identifier("dagger"), WeaponCapabilityPresets.DAGGER);
		typeEntry.put(EpicFightMod.identifier("bow"), WeaponCapabilityPresets.BOW);
		typeEntry.put(EpicFightMod.identifier("crossbow"), WeaponCapabilityPresets.CROSSBOW);
		typeEntry.put(EpicFightMod.identifier("trident"), WeaponCapabilityPresets.TRIDENT);
		typeEntry.put(EpicFightMod.identifier("shield"), WeaponCapabilityPresets.SHIELD);
		
		WeaponCapabilityPresetRegistryEvent weaponCapabilityPresetRegistryEvent = new WeaponCapabilityPresetRegistryEvent(typeEntry);
		ModLoader.get().postEvent(weaponCapabilityPresetRegistryEvent);
		PRESETS.putAll(weaponCapabilityPresetRegistryEvent.getTypeEntry());
	}
	
	public static final String DIRECTORY = "capabilities/weapons/types";
	
	private static final Gson GSON = (new GsonBuilder()).create();
	private static final Map<ResourceLocation, Function<Item, CapabilityItem.Builder>> PRESETS = Maps.newHashMap();
	private static final Map<ResourceLocation, CompoundTag> CAPABILITY_COMPOUNDS = Maps.newHashMap();
	
	public WeaponTypeReloadListener() {
		super(GSON, DIRECTORY);
	}
	
	@Override
	protected void apply(Map<ResourceLocation, JsonElement> packEntry, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		clear();
		
		for (Map.Entry<ResourceLocation, JsonElement> entry : packEntry.entrySet()) {
			CompoundTag compTag = null;
			
			try {
				compTag = TagParser.parseTag(entry.getValue().toString());
			} catch (CommandSyntaxException e) {
				e.printStackTrace();
			}
			
			try {
				final CompoundTag comptagFinal = compTag;
				PRESETS.put(entry.getKey(), (itemstack) -> deserializeWeaponCapabilityBuilder(entry.getKey(), comptagFinal));
				CAPABILITY_COMPOUNDS.put(entry.getKey(), compTag);
			} catch (Exception e) {
				EpicFightMod.LOGGER.warn("Error while deserializing weapon type datapack: " + entry.getKey());
				e.printStackTrace();
			}
		}
	}
	
	public static Function<Item, CapabilityItem.Builder> getOrThrow(String typeName) {
		ResourceLocation rl = ResourceLocation.parse(typeName);
		
		if (!PRESETS.containsKey(rl)) {
			throw new IllegalArgumentException("Can't find weapon type: " + rl);
		}
		
		return PRESETS.get(rl);
	}
	
	public static Function<Item, CapabilityItem.Builder> get(String typeName) {
		return get(ResourceLocation.parse(typeName));
	}
	
	public static Function<Item, CapabilityItem.Builder> get(ResourceLocation typeName) {
		return PRESETS.get(typeName);
	}
	
	public static void register(ResourceLocation rl, CapabilityItem.Builder builder) {
		PRESETS.put(rl, (item) -> builder);
	}
	
	
	public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(ResourceLocation rl, CompoundTag tag) {
		return deserializeWeaponCapabilityBuilder(rl, tag, null);
	}
	
	/**
	 * @deprecated Use Non-datapack sensitive version. {@link #deserializeWeaponCapabilityBuilder(ResourceLocation, CompoundTag)}
	 * @param extraEntryProvider Returns extra-entry created in runtime. (Datapack editor) Exists to access animations
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static WeaponCapability.Builder deserializeWeaponCapabilityBuilder(ResourceLocation rl, CompoundTag tag, @Nullable ExtraEntryProvider extraEntryProvider) {
		WeaponCapability.Builder builder = WeaponCapability.builder();
		
		if (!tag.contains("category") || StringUtil.isNullOrEmpty(tag.getString("category"))) {
			throw new IllegalArgumentException("Define weapon category.");
		}
		
		builder.category(WeaponCategory.ENUM_MANAGER.getOrThrow(tag.getString("category")));
		builder.collider(ColliderPreset.deserializeSimpleCollider(tag.getCompound("collider")));
		builder.canBePlacedOffhand(tag.contains("usable_in_offhand") ? tag.getBoolean("usable_in_offhand") : true);
		
		if (tag.contains("hit_particle")) {
			ParticleType<?> particleType = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.parse(tag.getString("hit_particle")));
			
			if (particleType == null) {
				EpicFightMod.LOGGER.warn("Can't find a particle type " + tag.getString("hit_particle") + " in " + rl);
			} else if (!(particleType instanceof HitParticleType)) {
				EpicFightMod.LOGGER.warn(tag.getString("hit_particle") + " is not a hit particle type in " + rl);
			} else {
				builder.hitParticle((HitParticleType)particleType);
			}
		}
		
		if (tag.contains("swing_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("swing_sound")));
			
			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a swing sound " + tag.getString("swing_sound") + " in " + rl);
			} else {
				builder.swingSound(sound);
			}
		}
		
		if (tag.contains("hit_sound")) {
			SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.parse(tag.getString("hit_sound")));
			
			if (sound == null) {
				EpicFightMod.LOGGER.warn("Can't find a hit sound " + tag.getString("hit_sound") + " in " + rl);
			} else {
				builder.hitSound(sound);
			}
		}
		
		CompoundTag combosTag = tag.getCompound("combos");
		
		for (String key : combosTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);
			ListTag comboAnimations = combosTag.getList(key, Tag.TAG_STRING);
			List<AnimationAccessor<? extends AttackAnimation>> anims = new ArrayList<> ();
			
			for (int i = 0; i < comboAnimations.size(); i++) {
				String animId = comboAnimations.getString(i);
				AnimationAccessor<? extends AttackAnimation> animation = extraEntryProvider == null ? AnimationManager.byKey(animId) : extraEntryProvider.getExtraOrBuiltInAnimation(animId);
				
				if (animation == null) {
					EpicFightMod.LOGGER.warn("Can't find an animation named " + comboAnimations.getString(i) + " in " + rl);
				} else {
					anims.add(animation);
				}
			}
			
			builder.newStyleCombo(style, anims.toArray(new AnimationAccessor[0]));
		}
		
		CompoundTag innateSkillsTag = tag.getCompound("innate_skills");
		
		for (String key : innateSkillsTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(key);
			
			builder.innateSkill(style, (itemstack) -> SkillManager.getSkill(innateSkillsTag.getString(key)));
		}
		
		CompoundTag livingmotionModifierTag = tag.getCompound("livingmotion_modifier");
		
		for (String sStyle : livingmotionModifierTag.getAllKeys()) {
			Style style = Style.ENUM_MANAGER.getOrThrow(sStyle);
			CompoundTag styleAnimationTag = livingmotionModifierTag.getCompound(sStyle);
			
			for (String sLivingmotion : styleAnimationTag.getAllKeys()) {
				LivingMotion livingmotion = LivingMotion.ENUM_MANAGER.getOrThrow(sLivingmotion);
				
				String animId = styleAnimationTag.getString(sLivingmotion);
				AnimationAccessor<? extends AttackAnimation> animation = extraEntryProvider == null ? AnimationManager.byKey(animId) : extraEntryProvider.getExtraOrBuiltInAnimation(animId);
				
				if (animation == null) {
					EpicFightMod.LOGGER.warn("No animation named {}", styleAnimationTag.getString(sLivingmotion));
				} else {
					builder.livingMotionModifier(style, livingmotion, animation);
				}
			}
		}
		
		CompoundTag stylesTag = tag.getCompound("styles");
		final List<Pair<Predicate<LivingEntityPatch<?>>, Style>> conditions = Lists.newArrayList();
		final Style defaultStyle = Style.ENUM_MANAGER.getOrThrow(stylesTag.getString("default"));
		
		for (Tag caseTag : stylesTag.getList("cases", Tag.TAG_COMPOUND)) {
			CompoundTag caseCompTag = (CompoundTag)caseTag;
			List<EntityPatchCondition> conditionList = Lists.newArrayList();
			
			for (Tag offhandTag : caseCompTag.getList("conditions", Tag.TAG_COMPOUND)) {
				CompoundTag offhandCompound = (CompoundTag)offhandTag;
				Supplier<EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(offhandCompound.getString("predicate")));
				EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}
			
			conditions.add(Pair.of((entitypatch) -> {
				for (EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}
				
				return true;
			}, Style.ENUM_MANAGER.getOrThrow(caseCompTag.getString("style"))));
		}
		
		builder.styleProvider((entitypatch) -> {
			for (Pair<Predicate<LivingEntityPatch<?>>, Style> entry : conditions) {
				if (entry.getFirst().test(entitypatch)) {
					return entry.getSecond();
				}
			}
			
			return defaultStyle;
		});
		
		if (tag.contains("offhand_item_compatible_predicate")) {
			ListTag offhandValidatorList = tag.getList("offhand_item_compatible_predicate", Tag.TAG_COMPOUND);
			List<EntityPatchCondition> conditionList = Lists.newArrayList();
			
			for (Tag offhandTag : offhandValidatorList) {
				CompoundTag offhandCompound = (CompoundTag)offhandTag;
				Supplier<EntityPatchCondition> conditionProvider = EpicFightConditions.getConditionOrThrow(ResourceLocation.parse(offhandCompound.getString("predicate")));
				EntityPatchCondition condition = conditionProvider.get();
				condition.read(offhandCompound);
				conditionList.add(condition);
			}
			
			builder.weaponCombinationPredicator((entitypatch) -> {
				for (EntityPatchCondition condition : conditionList) {
					if (!condition.predicate(entitypatch)) {
						return false;
					}
				}
				
				return true;
			});
		}
		
		if (tag.contains("custom_tags")) {
            for (Tag customTag : tag.getList("custom_tags", Tag.TAG_STRING)) {
                builder.addTag(ResourceLocation.parse(customTag.getAsString()));
            }
        }
		
		// Add data pack path as default tag
		builder.addTag(rl);
		
		return builder;
	}
	
	public static int getTagCount() {
		return CAPABILITY_COMPOUNDS.size();
	}
	
	public static Stream<CompoundTag> getWeaponTypeDataStream() {
		Stream<CompoundTag> tagStream = CAPABILITY_COMPOUNDS.entrySet().stream().map((entry) -> {
			entry.getValue().putString("registry_name", entry.getKey().toString());
			return entry.getValue();
		});
		return tagStream;
	}
	
	public static Set<Map.Entry<ResourceLocation, Function<Item, CapabilityItem.Builder>>> entries() {
		return PRESETS.entrySet();
	}
	
	public static void clear() {
		PRESETS.clear();
		WeaponTypeReloadListener.registerDefaultWeaponTypes();
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void processServerPacket(SPDatapackSync packet) {
		if (packet.getType() == SPDatapackSync.Type.WEAPON_TYPE) {
			PRESETS.clear();
			registerDefaultWeaponTypes();


			for (CompoundTag tag : packet.getTags()) {
				ResourceLocation rl = ResourceLocation.parse(tag.getString("registry_name"));

				try
				{
					PRESETS.put(rl, (itemstack) -> deserializeWeaponCapabilityBuilder(rl, tag));
				}
				catch (Exception e)
				{
					throw new RuntimeException("Weapon type " + rl + " encountered an error",e);
				}
			}
			
			ItemCapabilityReloadListener.weaponTypeProcessedCheck();
		}
	}
}