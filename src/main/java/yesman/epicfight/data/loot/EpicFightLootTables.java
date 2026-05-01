package yesman.epicfight.data.loot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import yesman.epicfight.api.event.types.registry.RegisterMobSkillBookLootTableEvent;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightItems;
import yesman.epicfight.registry.entries.EpicFightSkills;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public class EpicFightLootTables {
	@SubscribeEvent
	public static void onLootTableRegistry(final LootTableLoadEvent event) {
		int modifier = CommonConfig.skillBookChestLootModifier;
		int dropChance = 100 + modifier;
		int antiDropChance = 100 - modifier;
		float dropChanceModifier = dropChance / (float)(antiDropChance + dropChance);
		
    	if (event.getName().equals(BuiltInLootTables.DESERT_PYRAMID.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
    			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
    			)).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier)))
    		.build());
    		
    		event.getTable().addPool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.25F))
    			.add(LootItem.lootTableItem(EpicFightItems.UCHIGATANA.get()))
    		.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.JUNGLE_TEMPLE.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier))
        	.build());
    		
    		event.getTable().addPool(LootPool.lootPool().when(LootItemRandomChanceCondition.randomChance(0.25F))
    			.add(LootItem.lootTableItem(EpicFightItems.UCHIGATANA.get()))
    		.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.SIMPLE_DUNGEON.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
        	.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.ABANDONED_MINESHAFT.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
        	.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.PILLAGER_OUTPOST.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
        	.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.UNDERWATER_RUIN_BIG.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
        	.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.SHIPWRECK_MAP.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 2.0F))
        		.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL
        		))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
        	.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.STRONGHOLD_LIBRARY.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 5.0F))
    			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.HYPERVITALITY,
    				EpicFightSkills.FORBIDDEN_STRENGTH,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
    			))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
    		.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.WOODLAND_MANSION.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 5.0F))
    			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.HYPERVITALITY,
    				EpicFightSkills.FORBIDDEN_STRENGTH,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
    			))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
    		.build());
    	}
    	
    	if (event.getName().equals(BuiltInLootTables.BASTION_OTHER.location())) {
    		event.getTable().addPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 4.0F))
    			.add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
    				EpicFightSkills.BERSERKER,
    				EpicFightSkills.STAMINA_PILLAGER,
    				EpicFightSkills.TECHNICIAN,
    				EpicFightSkills.SWORD_MASTER,
    				EpicFightSkills.HYPERVITALITY,
    				EpicFightSkills.FORBIDDEN_STRENGTH,
    				EpicFightSkills.GUARD,
    				EpicFightSkills.STEP,
    				EpicFightSkills.ROLL,
    				EpicFightSkills.PHANTOM_ASCENT
    			))).when(LootItemRandomChanceCondition.randomChance(dropChanceModifier * 0.3F))
    		.build());
    	}
    	
    	OnSkillBookDroppedByEntity.registerEntitySkillLootTable();
    }

	public static void createSkillLootTable(RegisterMobSkillBookLootTableEvent skillLootTableRegistryEvent) {
        int modifier = CommonConfig.skillBookMobDropChanceModifier;
        int dropChance = 100 + modifier;
        int antiDropChance = 100 - modifier;
        float dropChanceModifier = antiDropChance == 0 ? Float.MAX_VALUE : dropChance / (float) antiDropChance;

        skillLootTableRegistryEvent.put(
            EntityType.ZOMBIE,
            LootTable.lootTable().withPool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1.0F))
                    .when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                    .add(
                        LootItem
                            .lootTableItem(EpicFightItems.SKILLBOOK.get())
                            .apply(
                                SetSkillFunction.builder(
                                    1.0F, EpicFightSkills.BERSERKER,
                                    1.0F, EpicFightSkills.STAMINA_PILLAGER,
                                    1.0F, EpicFightSkills.ROLL,
                                    1.0F, EpicFightSkills.STEP,
                                    1.0F, EpicFightSkills.GUARD,
                                    0.5F, EpicFightSkills.ENDURANCE
                                )
                            )
                    )
            )
        );

        skillLootTableRegistryEvent.put(EntityType.HUSK, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    1.0F, EpicFightSkills.BERSERKER,
                    1.0F, EpicFightSkills.STAMINA_PILLAGER,
                    1.0F, EpicFightSkills.ROLL,
                    1.0F, EpicFightSkills.STEP,
                    1.0F, EpicFightSkills.GUARD,
                    0.5F, EpicFightSkills.ENDURANCE
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.DROWNED, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    1.0F, EpicFightSkills.BERSERKER,
                    1.0F, EpicFightSkills.STAMINA_PILLAGER,
                    1.0F, EpicFightSkills.ROLL,
                    1.0F, EpicFightSkills.STEP,
                    1.0F, EpicFightSkills.GUARD,
                    0.5F, EpicFightSkills.ENDURANCE
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.SKELETON, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    1.0F, EpicFightSkills.SWORD_MASTER,
                    1.0F, EpicFightSkills.TECHNICIAN,
                    1.0F, EpicFightSkills.ROLL,
                    1.0F, EpicFightSkills.STEP,
                    1.0F, EpicFightSkills.GUARD,
                    0.5F, EpicFightSkills.EMERGENCY_ESCAPE
                )))));
        skillLootTableRegistryEvent.put(EntityType.STRAY, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    1.0F, EpicFightSkills.SWORD_MASTER,
                    1.0F, EpicFightSkills.TECHNICIAN,
                    1.0F, EpicFightSkills.ROLL,
                    1.0F, EpicFightSkills.STEP,
                    1.0F, EpicFightSkills.GUARD,
                    0.5F, EpicFightSkills.EMERGENCY_ESCAPE
                )))));
        skillLootTableRegistryEvent.put(EntityType.SPIDER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.ROLL,
                    EpicFightSkills.STEP,
                    EpicFightSkills.GUARD
                )))));
        skillLootTableRegistryEvent.put(EntityType.CAVE_SPIDER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.ROLL,
                    EpicFightSkills.STEP,
                    EpicFightSkills.GUARD
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.CREEPER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.HYPERVITALITY,
                    EpicFightSkills.IMPACT_GUARD
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.ENDERMAN, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.HYPERVITALITY,
                    EpicFightSkills.FORBIDDEN_STRENGTH,
                    EpicFightSkills.ENDURANCE,
                    EpicFightSkills.EMERGENCY_ESCAPE,
                    EpicFightSkills.PARRYING,
                    EpicFightSkills.IMPACT_GUARD
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.VINDICATOR, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.HYPERVITALITY,
                    EpicFightSkills.BERSERKER,
                    EpicFightSkills.GUARD,
                    EpicFightSkills.STEP,
                    EpicFightSkills.ROLL
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.PILLAGER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.HYPERVITALITY,
                    EpicFightSkills.STAMINA_PILLAGER,
                    EpicFightSkills.GUARD,
                    EpicFightSkills.STEP,
                    EpicFightSkills.ROLL
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.WITCH, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.FORBIDDEN_STRENGTH,
                    EpicFightSkills.BERSERKER
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.EVOKER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.PARRYING,
                    EpicFightSkills.IMPACT_GUARD
                )))).withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.1F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.DEATH_HARVEST,
                    EpicFightSkills.EMERGENCY_ESCAPE
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.PIGLIN, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.SWORD_MASTER,
                    EpicFightSkills.STAMINA_PILLAGER,
                    EpicFightSkills.GUARD,
                    EpicFightSkills.STEP,
                    EpicFightSkills.ROLL
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.PIGLIN_BRUTE, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.HYPERVITALITY,
                    EpicFightSkills.PARRYING,
                    EpicFightSkills.ENDURANCE,
                    EpicFightSkills.IMPACT_GUARD
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.ZOMBIFIED_PIGLIN, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.BERSERKER,
                    EpicFightSkills.STAMINA_PILLAGER,
                    EpicFightSkills.GUARD,
                    EpicFightSkills.STEP,
                    EpicFightSkills.ROLL
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.WITHER_SKELETON, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(LootItemRandomChanceCondition.randomChance(0.025F * dropChanceModifier))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    1.0F, EpicFightSkills.SWORD_MASTER,
                    1.0F, EpicFightSkills.STAMINA_PILLAGER,
                    1.0F, EpicFightSkills.GUARD,
                    1.0F, EpicFightSkills.STEP,
                    1.0F, EpicFightSkills.ROLL,
                    0.75F, EpicFightSkills.DEATH_HARVEST
                )))
        ));
        skillLootTableRegistryEvent.put(EntityType.WITHER, LootTable.lootTable().withPool(
            LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(EpicFightItems.SKILLBOOK.get()).apply(SetSkillFunction.builder(
                    EpicFightSkills.DEATH_HARVEST
                )))
        ));
	}
}