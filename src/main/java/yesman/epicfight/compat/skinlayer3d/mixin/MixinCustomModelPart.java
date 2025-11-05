package yesman.epicfight.compat.skinlayer3d.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;

@Mixin(value = CustomModelPart.class, remap = false)
public interface MixinCustomModelPart {
	@Accessor(value = "x", remap = false)
	public float getX();

	@Accessor(value = "y", remap = false)
	public float getY();

	@Accessor(value = "z", remap = false)
	public float getZ();

	@Accessor(value = "xRot", remap = false)
	public float getXRot();

	@Accessor(value = "yRot", remap = false)
	public float getYRot();

	@Accessor(value = "zRot", remap = false)
	public float getZRot();
}
