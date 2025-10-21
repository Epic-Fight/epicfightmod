package yesman.epicfight.network.common;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.animation.AnimationVariables;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public record BiDirectionalAnimationVariable(Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, Holder<SynchedAnimationVariableKey<?>> synchedAnimationVariableKey, FriendlyByteBuf buf) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, BiDirectionalAnimationVariable> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.enumCodec(Action.class),
			BiDirectionalAnimationVariable::action,
			ByteBufCodecsExtends.ANIMATION,
			BiDirectionalAnimationVariable::animation,
			ByteBufCodecs.INT,
			BiDirectionalAnimationVariable::entityId,
			SynchedAnimationVariableKey.STREAM_CODEC,
			BiDirectionalAnimationVariable::synchedAnimationVariableKey,
			ByteBufCodecs.BYTE_ARRAY,
			payload -> payload.buf.array(),
			BiDirectionalAnimationVariable::new
	    );
	
	public BiDirectionalAnimationVariable(Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, Holder<SynchedAnimationVariableKey<?>> synchedAnimationVariableKey, byte[] bytes) {
		this(action, animation, entityId, synchedAnimationVariableKey, new FriendlyByteBuf(Unpooled.copiedBuffer(bytes)));
	}
	
	@SuppressWarnings("unchecked")
	public void commonProcess(LivingEntityPatch<?> entitypatch) {
		switch (this.action) {
		case PUT -> {
			Object value = this.synchedAnimationVariableKey().value().decode(this.buf());
			
			if (this.synchedAnimationVariableKey().value().isSharedKey()) {
				entitypatch.getAnimator().getVariables().putSharedVariable((AnimationVariables.SharedVariableKey<Object>)this.synchedAnimationVariableKey().value(), value, false);
			} else {
				entitypatch.getAnimator().getVariables().put((AnimationVariables.IndependentVariableKey<Object>)this.synchedAnimationVariableKey().value(), this.animation, value, false);
			}
		}
		case REMOVE -> {
			if (this.synchedAnimationVariableKey().value().isSharedKey()) {
				entitypatch.getAnimator().getVariables().removeSharedVariable((AnimationVariables.SharedVariableKey<Object>)this.synchedAnimationVariableKey().value(), false);
			} else {
				entitypatch.getAnimator().getVariables().remove((AnimationVariables.IndependentVariableKey<Object>)this.synchedAnimationVariableKey().value(), this.animation, false);
			}
		}
		}
	}
	
	public enum Action {
		PUT, REMOVE
	}
}