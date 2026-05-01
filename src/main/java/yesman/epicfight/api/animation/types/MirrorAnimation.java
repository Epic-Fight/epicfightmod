package yesman.epicfight.api.animation.types;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class MirrorAnimation extends StaticAnimation {
	public DirectStaticAnimation original;
	public DirectStaticAnimation mirror;
	
	public MirrorAnimation(float transitionTime, boolean repeatPlay, AnimationAccessor<? extends MirrorAnimation> accessor, String path1, String path2, AssetAccessor<? extends Armature> armature) {
		super(transitionTime, false, accessor, armature);
		
		this.original = new DirectStaticAnimation(transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().getNamespace(), path1), armature);
		this.mirror = new DirectStaticAnimation(transitionTime, repeatPlay, ResourceLocation.fromNamespaceAndPath(accessor.registryName().getNamespace(), path2), armature);
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		if (entitypatch.isLogicalClient()) {
			DirectStaticAnimation animation = this.checkHandAndReturnAnimation(entitypatch.getOriginal().getUsedItemHand());
			entitypatch.getClientAnimator().playAnimation(animation, 0.0F);
		}
	}
	
	@Override
	public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
		return List.of(this.original, this.mirror);
	}
	
	@Override
	public boolean isMetaAnimation() {
		return true;
	}
	
	@Override
	public boolean isClientAnimation() {
		return true;
	}
	
	@Override @ClientOnly
	public Layer.Priority getPriority() {
		return this.original.getProperty(ClientAnimationProperties.PRIORITY).orElse(Layer.Priority.LOWEST);
	}
	
	@Override @ClientOnly
	public Layer.LayerType getLayerType() {
		return this.original.getProperty(ClientAnimationProperties.LAYER_TYPE).orElse(Layer.LayerType.BASE_LAYER);
	}
	
	private DirectStaticAnimation checkHandAndReturnAnimation(InteractionHand hand) {
		if (hand == InteractionHand.OFF_HAND) {
			return this.mirror;
		}
		
		return this.original;
	}
}