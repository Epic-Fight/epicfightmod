package yesman.epicfight.compat.fgm.mixin;

import com.wildfire.render.GenderLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(GenderLayer.class)
public interface FemaleLayerAccessor<E extends LivingEntity, M extends HumanoidModel<E>> {

    @Invoker(value = "getBreastTexture", remap = false)
    @Nullable ResourceLocation getTexture(LivingEntity entity);

}
