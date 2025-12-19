package yesman.epicfight.data.conditions;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.data.conditions.entity.HealthPoint;
import yesman.epicfight.data.conditions.entity.OffhandItemCategory;
import yesman.epicfight.data.conditions.entity.PlayerName;
import yesman.epicfight.data.conditions.entity.PlayerSkillActivated;
import yesman.epicfight.data.conditions.entity.RandomChance;
import yesman.epicfight.data.conditions.entity.TargetInDistance;
import yesman.epicfight.data.conditions.entity.TargetInEyeHeight;
import yesman.epicfight.data.conditions.entity.TargetInPov;
import yesman.epicfight.data.conditions.itemstack.TagValueCondition;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightConditions {
	public static final DeferredRegister<Supplier<Condition<?>>> CONDITIONS = DeferredRegister.create(EpicFightMod.identifier("conditions"), EpicFightMod.MODID);
	public static final Supplier<IForgeRegistry<Supplier<Condition<?>>>> REGISTRY = CONDITIONS.makeRegistry(RegistryBuilder::new);
	
	public static <T extends Condition<?>> Supplier<T> getConditionOrThrow(ResourceLocation key) throws NoSuchElementException, ClassCastException {
		if (!REGISTRY.get().containsKey(key)) {
			throw new NoSuchElementException("No condition named " + key);
		}
		
		return getConditionOrNull(key);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Condition<?>> Supplier<T> getConditionOrNull(ResourceLocation key) throws ClassCastException {
		return (Supplier<T>) REGISTRY.get().getValue(key);
	}
	
	//EntityPatch conditions
	public static final RegistryObject<Supplier<Condition<?>>> OFFHAND_ITEM_CATEGORY = CONDITIONS.register("offhand_item_category", () -> OffhandItemCategory::new);
	public static final RegistryObject<Supplier<Condition<?>>> PLAYER_SKILL_ACTIVATED = CONDITIONS.register("skill_active", () -> PlayerSkillActivated::new);
	public static final RegistryObject<Supplier<Condition<?>>> PLAYER_NAME = CONDITIONS.register("player_name", () -> PlayerName::new);
	public static final RegistryObject<Supplier<Condition<?>>> HEALTH_POINT = CONDITIONS.register("health", () -> HealthPoint::new);
	public static final RegistryObject<Supplier<Condition<?>>> RANDOM = CONDITIONS.register("random_chance", () -> RandomChance::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_DISTANCE = CONDITIONS.register("within_distance", () -> TargetInDistance::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_EYE_HEIGHT = CONDITIONS.register("within_eye_height", () -> TargetInEyeHeight::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_POV = CONDITIONS.register("within_angle", () -> TargetInPov::new);
	public static final RegistryObject<Supplier<Condition<?>>> TARGET_IN_POV_HORIZONTAL = CONDITIONS.register("within_angle_horizontal", () -> TargetInPov.TargetInPovHorizontal::new);
	
	//Itemstack conditions
	public static final RegistryObject<Supplier<Condition<?>>> TAG_VALUE = CONDITIONS.register("tag_value", () -> TagValueCondition::new);
}
