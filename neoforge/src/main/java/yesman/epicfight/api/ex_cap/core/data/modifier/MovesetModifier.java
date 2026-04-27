package yesman.epicfight.api.ex_cap.core.data.modifier;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record MovesetModifier(ResourceLocation moveSet, WeaponModifier.Operation operation)
{

}
