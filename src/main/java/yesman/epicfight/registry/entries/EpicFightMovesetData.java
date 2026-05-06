package yesman.epicfight.registry.entries;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.registry.deferred.CustomDataRegister;
import yesman.epicfight.registry.deferred.holders.DeferredCustomData;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.item.custom.CustomData;

import java.util.function.BiFunction;

public class EpicFightMovesetData
{
    public static final CustomDataRegister REGISTER = CustomDataRegister.createMoveset(EpicFight.MODID);

    public static final DeferredCustomData<CustomData<BiFunction<SkillContainer, CompoundTag, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>> DODGE_ANIMATION = REGISTER.registerCustomData("dodge_animation", () -> new CustomData<BiFunction<SkillContainer, CompoundTag, AnimationManager.AnimationAccessor<? extends StaticAnimation>>>((container, tag) -> null));
}
