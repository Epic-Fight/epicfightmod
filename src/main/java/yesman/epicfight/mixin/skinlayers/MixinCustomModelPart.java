package yesman.epicfight.mixin.skinlayers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;

@Mixin(value = CustomModelPart.class, remap = false)
public interface MixinCustomModelPart {
	@Accessor("x")
	float getX();
	
	@Accessor("y")
	float getY();
	
	@Accessor("z")
	float getZ();
	
	@Accessor("xRot")
	float getXRot();
	
	@Accessor("yRot")
	float getYRot();
	
	@Accessor("zRot")
	float getZRot();
}
