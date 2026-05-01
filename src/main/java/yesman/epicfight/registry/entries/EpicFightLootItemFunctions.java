package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.data.loot.function.SetSkillFunction;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightLootItemFunctions {
	private EpicFightLootItemFunctions() {}
	
	public static final DeferredRegister<LootItemFunctionType<?>> REGISTRY = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetSkillFunction>> SKILLS = REGISTRY.register("skillbook_loot_table_modifier", () -> 
		new LootItemFunctionType<> (SetSkillFunction.CODEC)
	);
}
