package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPAnimatorControl extends AnimatorControlPacket {
	protected int entityId;
	protected Layer layer = Layer.ANIMATION;
	protected Priority priority = Priority.ANIMATION;
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch<?> entitypatch) {
		this(action, animation.get().getId(), entitypatch.getOriginal().getId(), transitionTimeModifier, false);
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, int entityId, float transitionTimeModifier, boolean pause) {
		this(action, animation.get().getId(), entityId, transitionTimeModifier, pause);
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, int animationId, int entityId, float transitionTimeModifier, boolean pause) {
		super(action, animationId, transitionTimeModifier, pause);
		
		this.entityId = entityId;
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, AssetAccessor<? extends StaticAnimation> animation, float transitionTimeModifier, LivingEntityPatch<?> entitypatch, Layer layer, Priority priority) {
		this(action, animation.get().getId(), entitypatch.getOriginal().getId(), transitionTimeModifier, false);
		
		this.layer = layer;
		this.priority = priority;
	}
	
	public SPAnimatorControl(AnimatorControlPacket.Action action, int animationId, int entityId, float transitionTimeModifier, boolean pause, Layer layer, Priority priority) {
		super(action, animationId, transitionTimeModifier, pause);
		
		this.entityId = entityId;
		this.layer = layer;
		this.priority = priority;
	}
	
	public <T extends SPAnimatorControl> void onArrive() {
		EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.getInstance().level.getEntity(this.entityId), LivingEntityPatch.class).ifPresent(entitypatch -> {
			if (this.action == Action.PLAY_CLIENT && this.layer != Layer.ANIMATION && this.priority != Priority.ANIMATION) {
				entitypatch.getClientAnimator().playAnimationAt(AnimationManager.byId(this.animationId), this.transitionTimeModifier, this.layer, this.priority);
			} else {
				this.process(entitypatch);
			}
		});
	}
	
	public static SPAnimatorControl fromBytes(FriendlyByteBuf buf) {
		return new SPAnimatorControl(buf.readEnum(Action.class), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readEnum(Layer.class), buf.readEnum(Priority.class));
	}
	
	public static void toBytes(SPAnimatorControl msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeInt(msg.animationId);
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.transitionTimeModifier);
		buf.writeBoolean(msg.pause);
		buf.writeEnum(msg.layer);
		buf.writeEnum(msg.priority);
	}
	
	public static void handle(SPAnimatorControl msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			msg.onArrive();
		});
		
		ctx.get().setPacketHandled(true);
	}
}