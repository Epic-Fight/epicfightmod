package yesman.epicfight.data.loot;

import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.forgeevent.SkillLootTableRegistryEvent;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.world.item.EpicFightItems;

public class SkillBookLootModifier extends LootModifier {
	public static final Supplier<Codec<SkillBookLootModifier>> SKILL_CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SkillBookLootModifier::new)));
	public static final Map<EntityType<?>, LootTable> SKILL_LOOT_TABLE = Maps.newHashMap();
	
	public SkillBookLootModifier(LootItemCondition[] lootItemConditions) {
		super(lootItemConditions);
	}
	
	/**
	 * Skill List
	 * Passive
	 * epicfight:berserker
	 * epicfight:stamina_pillager
	 * epicfight:swordmaster
	 * epicfight:technician
	 * epicfight:hypervitality
	 * epicfight:forbidden_strength
	 * epicfight:death_harvest
	 * epicfight:endurance
	 * epicfight:emergency_escape
	 * 
	 * Guard
	 * epicfight:guard
	 * epicfight:impact_guard
	 * epicfight:parrying
	 * 
	 * Dodge
	 * epicfight:roll
	 * epicfight:step
	 * 
	 * Mover
	 * epicfight:demolishing_leap
	 */
	public static void createSkillLootTable() {
		Map<EntityType<?>, LootTable.Builder> builders = Maps.newHashMap();
		SkillLootTableRegistryEvent lootTableRegistryEvent = new SkillLootTableRegistryEvent(builders);
		int modifier = CommonConfig.skillBookMobDropChanceModifier;
		int dropChance = 100 + modifier;
		int antiDropChance = 100 - modifier;
		float dropChanceModifier = (antiDropChance == 0) ? Float.MAX_VALUE : dropChance / (float)antiDropChance;
		
		lootTableRegistryEvent.put(EntityType.ZOMBIE, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:berserker",
					1.0F, "epicfight:stamina_pillager",
					1.0F, "epicfight:roll",
					1.0F, "epicfight:step",
					1.0F, "epicfight:guard",
					0.5F, "epicfight:endurance"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.HUSK, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:berserker",
					1.0F, "epicfight:stamina_pillager",
					1.0F, "epicfight:roll",
					1.0F, "epicfight:step",
					1.0F, "epicfight:guard",
					0.5F, "epicfight:endurance"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.DROWNED, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:berserker",
					1.0F, "epicfight:stamina_pillager",
					1.0F, "epicfight:roll",
					1.0F, "epicfight:step",
					1.0F, "epicfight:guard",
					0.5F, "epicfight:endurance"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.SKELETON, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:swordmaster",
					1.0F, "epicfight:technician",
					1.0F, "epicfight:roll",
					1.0F, "epicfight:step",
					1.0F, "epicfight:guard",
					0.5F, "epicfight:emergency_escape"
			)))));
		lootTableRegistryEvent.put(EntityType.STRAY, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:swordmaster",
					1.0F, "epicfight:technician",
					1.0F, "epicfight:roll",
					1.0F, "epicfight:step",
					1.0F, "epicfight:guard",
					0.5F, "epicfight:emergency_escape"
			)))));
		lootTableRegistryEvent.put(EntityType.SPIDER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:roll",
					"epicfight:step",
					"epicfight:guard"
			)))));
		lootTableRegistryEvent.put(EntityType.CAVE_SPIDER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:roll",
					"epicfight:step",
					"epicfight:guard"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.CREEPER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:hypervitality",
					"epicfight:impact_guard"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.ENDERMAN, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:hypervitality",
					"epicfight:forbidden_strength",
					"epicfight:endurance",
					"epicfight:emergency_escape",
					"epicfight:parrying",
					"epicfight:impact_guard"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.VINDICATOR, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:hypervitality",
					"epicfight:berserker",
					"epicfight:guard",
					"epicfight:step",
					"epicfight:roll"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.PILLAGER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:hypervitality",
					"epicfight:stamina_pillager",
					"epicfight:guard",
					"epicfight:step",
					"epicfight:roll"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.WITCH, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:forbidden_strength",
					"epicfight:berserker"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.EVOKER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:parrying",
					"epicfight:impact_guard"
			)))).withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.1F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:death_harvest",
					"epicfight:emergency_escape"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.PIGLIN, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:swordmaster",
					"epicfight:stamina_pillager",
					"epicfight:guard",
					"epicfight:step",
					"epicfight:roll"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.PIGLIN_BRUTE, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:hypervitality",
					"epicfight:parrying",
					"epicfight:endurance",
					"epicfight:impact_guard"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.ZOMBIFIED_PIGLIN, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:berserker",
					"epicfight:stamina_pillager",
					"epicfight:guard",
					"epicfight:step",
					"epicfight:roll"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.WITHER_SKELETON, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					1.0F, "epicfight:swordmaster",
					1.0F, "epicfight:stamina_pillager",
					1.0F, "epicfight:guard",
					1.0F, "epicfight:step",
					1.0F, "epicfight:roll",
					0.75F, "epicfight:death_harvest"
			)))
    	));
		lootTableRegistryEvent.put(EntityType.WITHER, LootTable.lootTable().withPool(
			LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
					"epicfight:death_harvest"
			)))
    	));
		
		ModLoader.get().postEvent(lootTableRegistryEvent);
		
		SKILL_LOOT_TABLE.clear();
		
		builders.forEach((k, v) -> {
			SKILL_LOOT_TABLE.put(k, v.build());
		});
	}

	/**
	 * Applies the modifier to the generated loot (all loot conditions have already been checked
	 * and have returned true).
	 *
	 * @param generatedLoot the list of ItemStacks that will be dropped, generated by loot tables
	 * @param context       the LootContext, identical to what is passed to loot tables
	 * @return modified loot drops
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		
		if (entity != null && SKILL_LOOT_TABLE.containsKey(entity.getType())) {
			SKILL_LOOT_TABLE.get(entity.getType()).getRandomItemsRaw(context, generatedLoot::add);
		}
		
		return generatedLoot;
	}

	/**
	 * Returns the registered codec for this modifier
	 */
	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return SKILL_CODEC.get();
	}
}