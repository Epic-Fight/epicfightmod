package yesman.epicfight.compat.geckolib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

@Mixin(value = GeoArmorRenderer.class)
public interface MixinGeoArmorRenderer {
	@Accessor
	public GeoBone getHead();
	
	@Accessor
	public GeoBone getBody();
	
	@Accessor
	public GeoBone getRightArm();
	
	@Accessor
	public GeoBone getLeftArm();
	
	@Accessor
	public GeoBone getRightLeg();
	
	@Accessor
	public GeoBone getLeftLeg();
	
	@Accessor
	public GeoBone getRightBoot();
	
	@Accessor
	public GeoBone getLeftBoot();
}
