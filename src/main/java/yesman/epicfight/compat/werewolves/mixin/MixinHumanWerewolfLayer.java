package yesman.epicfight.compat.werewolves.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import de.teamlapen.werewolves.client.render.layer.HumanWerewolfLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

@Mixin(value = HumanWerewolfLayer.class, remap = false)
public interface MixinHumanWerewolfLayer<T extends LivingEntity, A extends HumanoidModel<T>> {
	@Accessor(remap = false)
	public List<ResourceLocation> getTextures();

	@Accessor(remap = false)
	public A getModel();
}