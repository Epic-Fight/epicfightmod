package yesman.epicfight.api.event.types.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import yesman.epicfight.api.event.Event;

import java.util.Map;

public class RegisterMobSkillBookLootTableEvent extends Event {
	private final Map<EntityType<?>, LootTable.Builder> builders;
	
	public RegisterMobSkillBookLootTableEvent(Map<EntityType<?>, LootTable.Builder> builders) {
		this.builders = builders;
	}
	
	public LootTable.Builder get(EntityType<?> entityType) {
		return this.builders.get(entityType);
	}
	
	public RegisterMobSkillBookLootTableEvent put(EntityType<?> entityType, LootTable.Builder builder) {
		this.builders.put(entityType, builder);
		return this;
	}
	
	public RegisterMobSkillBookLootTableEvent add(EntityType<?> entityType, LootPool.Builder builder) {
		this.builders.computeIfAbsent(entityType, (k) -> LootTable.lootTable()).withPool(builder);
		return this;
	}
}