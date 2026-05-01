package yesman.epicfight.network.server;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.common.AbstractAnimatorControl;
import yesman.epicfight.network.common.BiDirectionalAnimationVariable;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPAnimatorControl extends AbstractAnimatorControl {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPAnimatorControl> STREAM_CODEC =
		ByteBufCodecsExtends.composite8(
	        ByteBufCodecsExtends.enumCodec(AbstractAnimatorControl.Action.class),
	        SPAnimatorControl::action,
	        ByteBufCodecsExtends.ANIMATION,
	        SPAnimatorControl::animation,
	        ByteBufCodecs.INT,
	        SPAnimatorControl::entityId,
	        ByteBufCodecs.FLOAT,
	        SPAnimatorControl::transitionTimeModifier,
	        ByteBufCodecs.BOOL,
	        SPAnimatorControl::pause,
	        ByteBufCodecsExtends.listOf(BiDirectionalAnimationVariable.STREAM_CODEC),
	        AbstractAnimatorControl::animationVariables,
	        ByteBufCodecsExtends.enumCodec(AbstractAnimatorControl.Layer.class),
	        SPAnimatorControl::layer,
	        ByteBufCodecsExtends.enumCodec(AbstractAnimatorControl.Priority.class),
	        SPAnimatorControl::priority,
	        SPAnimatorControl::new
	    );
	
	protected final int entityId;
	protected Layer layer = Layer.ANIMATION;
	protected Priority priority = Priority.ANIMATION;	

	public SPAnimatorControl(AbstractAnimatorControl.Action action, AssetAccessor<? extends StaticAnimation> animation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier) {
		this(action, animation, entitypatch.getOriginal().getId(), transitionTimeModifier, false);
	}
	
	public SPAnimatorControl(AbstractAnimatorControl.Action action, AssetAccessor<? extends StaticAnimation> animation, LivingEntityPatch<?> entitypatch, float transitionTimeModifier, Layer layer, Priority priority) {
		this(action, animation, entitypatch.getOriginal().getId(), transitionTimeModifier, false);
		
		this.layer = layer;
		this.priority = priority;	
	}
	
	public SPAnimatorControl(AbstractAnimatorControl.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause) {
		this(action, animation, entityId, transitionTimeModifier, pause, new ArrayList<> ());
	}
	
	public SPAnimatorControl(AbstractAnimatorControl.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause, List<BiDirectionalAnimationVariable> animationVariables) {
		super(action, animation, transitionTimeModifier, pause, animationVariables);
		
		this.entityId = entityId;
	}
	
	public SPAnimatorControl(AbstractAnimatorControl.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause, List<BiDirectionalAnimationVariable> animationVariables, Layer layer, Priority priority) {
		super(action, animation, transitionTimeModifier, pause, animationVariables);
		
		this.entityId = entityId;
		this.layer = layer;
		this.priority = priority;
	}
	
	public int entityId() {
		return this.entityId;
	}
	
	public Layer layer() {
		return this.layer;
	}
	
	public Priority priority() {
		return this.priority;
	}
}