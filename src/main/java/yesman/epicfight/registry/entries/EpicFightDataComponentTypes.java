package yesman.epicfight.registry.entries;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;

public final class EpicFightDataComponentTypes {
	private EpicFightDataComponentTypes() {}
	
	public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Skill>>> SKILL = REGISTRY.register("skill", () ->
		DataComponentType.<Holder<Skill>>builder().persistent(Skill.CODEC).networkSynchronized(Skill.STREAM_CODEC).cacheEncoding().build()
	);
}
