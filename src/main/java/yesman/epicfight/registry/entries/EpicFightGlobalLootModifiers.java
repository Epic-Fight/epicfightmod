package yesman.epicfight.registry.entries;

import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yesman.epicfight.data.loot.OnSkillBookDroppedByEntity;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightGlobalLootModifiers {
	private EpicFightGlobalLootModifiers() {}
	
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, EpicFightMod.MODID);
	
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<OnSkillBookDroppedByEntity>> SKILLS = REGISTRY.register("skillbook_loot_table_modifier", () ->  OnSkillBookDroppedByEntity.SKILL_CODEC);
}
