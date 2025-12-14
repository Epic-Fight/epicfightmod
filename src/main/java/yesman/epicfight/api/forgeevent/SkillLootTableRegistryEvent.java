package yesman.epicfight.api.forgeevent;

import java.util.Map;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class SkillLootTableRegistryEvent extends Event implements IModBusEvent {
	private final Map<EntityType<?>, LootTable.Builder> builders;
	
	public SkillLootTableRegistryEvent(Map<EntityType<?>, LootTable.Builder> builders) {
		this.builders = builders;
	}
	
	public LootTable.Builder get(EntityType<?> entityType) {
		return this.builders.get(entityType);
	}
	
	public SkillLootTableRegistryEvent put(EntityType<?> entityType, LootTable.Builder builder) {
		this.builders.put(entityType, builder);
		return this;
	}
	
	public SkillLootTableRegistryEvent add(EntityType<?> entityType, LootPool.Builder builder) {
		this.builders.computeIfAbsent(entityType, (k) -> LootTable.lootTable()).withPool(builder);
		return this;
	}
}