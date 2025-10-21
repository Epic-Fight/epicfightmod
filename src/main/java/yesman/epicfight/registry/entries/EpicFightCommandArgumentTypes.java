package yesman.epicfight.registry.entries;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.server.commands.arguments.AnimationArgument;
import yesman.epicfight.server.commands.arguments.SkillArgument;

public final class EpicFightCommandArgumentTypes {
	private EpicFightCommandArgumentTypes() {}
	
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, EpicFightMod.MODID);
	
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<SkillArgument, ?>> SKILL = REGISTRY.register("skill", () -> SingletonArgumentInfo.contextFree(SkillArgument::skill));
	public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<AnimationArgument, ?>> ANIMATION = REGISTRY.register("animation", () -> SingletonArgumentInfo.contextFree(AnimationArgument::animation));
	
	public static void registerArgumentTypes() {
		ArgumentTypeInfos.registerByClass(SkillArgument.class, SKILL.get());
		ArgumentTypeInfos.registerByClass(AnimationArgument.class, ANIMATION.get());
	}
}