package yesman.epicfight.server.commands.arguments;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightCommandArgumentTypes {
	public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, EpicFightMod.MODID);
	
	public static final RegistryObject<ArgumentTypeInfo<SkillArgument, ?>> SKILL = COMMAND_ARGUMENT_TYPES.register("skill", () -> SingletonArgumentInfo.contextFree(SkillArgument::skill));
	public static final RegistryObject<ArgumentTypeInfo<AnimationArgument, ?>> ANIMATION = COMMAND_ARGUMENT_TYPES.register("animation", () -> SingletonArgumentInfo.contextFree(AnimationArgument::animation));
	
	public static void registerArgumentTypes() {
		ArgumentTypeInfos.registerByClass(SkillArgument.class, SKILL.get());
		ArgumentTypeInfos.registerByClass(AnimationArgument.class, ANIMATION.get());
	}
}