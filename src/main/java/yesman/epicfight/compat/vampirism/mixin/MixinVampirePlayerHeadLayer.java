package yesman.epicfight.compat.vampirism.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import de.teamlapen.vampirism.client.renderer.entity.layers.VampirePlayerHeadLayer;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = VampirePlayerHeadLayer.class, remap = false)
public interface MixinVampirePlayerHeadLayer {
	@Accessor(remap = false)
    public ResourceLocation[] getEyeOverlays();

	@Accessor(remap = false)
	public ResourceLocation[] getFangOverlays();
}