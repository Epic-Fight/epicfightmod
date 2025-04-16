package yesman.epicfight.api.animation.types;

import java.util.List;
import java.util.function.Function;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.AnimationVariables.IndependentAnimationVariableKey;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.SimpleEvent;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SelectiveAnimation extends StaticAnimation {
	public static final IndependentAnimationVariableKey<Integer> PREVIOUS_STATE = AnimationVariables.independent(() -> -1, true);
	
	private final Function<LivingEntityPatch<?>, Integer> selector;
	private final List<AssetAccessor<? extends StaticAnimation>> selectOptions;
	
	/**
	 * All animations should have same priority and layer type
	 */
	@SafeVarargs
	public SelectiveAnimation(Function<LivingEntityPatch<?>, Integer> selector, AnimationAccessor<? extends SelectiveAnimation> accessor, AssetAccessor<? extends StaticAnimation>... selectOptions) {
		super(0.15F, false, accessor, null);
		
		this.selector = selector;
		this.selectOptions = List.of(selectOptions);
		
		for (AssetAccessor<? extends StaticAnimation> subAnimations : this.selectOptions) {
			subAnimations.get().addEvents(SimpleEvent.create((entitypatch, animation, params) -> {
				int result = this.selector.apply(entitypatch);
				
				if (entitypatch.getAnimator().getVariables().get(PREVIOUS_STATE, this.getAccessor()) != null && entitypatch.getAnimator().getVariables().get(PREVIOUS_STATE, this.getAccessor()) != result) {
					entitypatch.getAnimator().playAnimation(this.selectOptions.get(result), 0.0F);
					entitypatch.getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), result);
				}
				else
				{
					entitypatch.getAnimator().playAnimation(this.selectOptions.get(0), 0.0F);
					entitypatch.getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), 0);
				}
				
			}, AnimationEvent.Side.BOTH));
		}
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		int result = this.selector.apply(entitypatch);
		entitypatch.getAnimator().getVariables().put(PREVIOUS_STATE, this.getAccessor(), result);
		entitypatch.getAnimator().playAnimation(this.selectOptions.get(result), 0.0F);
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
	}
	
	@Override
	public boolean isMetaAnimation() {
		return true;
	}
	
	@Override
	public List<AssetAccessor<? extends StaticAnimation>> getSubAnimations() {
		return this.selectOptions;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Layer.Priority getPriority() {
		return this.selectOptions.get(0).get().getPriority();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public Layer.LayerType getLayerType() {
		return this.selectOptions.get(0).get().getLayerType();
	}
}
