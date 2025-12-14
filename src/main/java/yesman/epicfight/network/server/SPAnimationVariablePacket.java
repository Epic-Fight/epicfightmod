package yesman.epicfight.network.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.network.common.AnimationVariablePacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPAnimationVariablePacket<T> extends AnimationVariablePacket<T> {
	protected int entityId;
	
	public SPAnimationVariablePacket(LivingEntityPatch<?> entitypatch, SynchedAnimationVariableKey<T> animationVariableKey, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, Action action) {
		super(animationVariableKey, animation, value, action);
		this.entityId = entitypatch.getOriginal().getId();
	}
	
	public SPAnimationVariablePacket(int entityId, SynchedAnimationVariableKey<T> animationVariableKey, @Nullable AssetAccessor<? extends StaticAnimation> animation, T value, Action action) {
		super(animationVariableKey, animation, value, action);
		this.entityId = entityId;
	}
	
	public static <T> SPAnimationVariablePacket<T> fromBytes(FriendlyByteBuf buf) {
		int entityId = buf.readInt();
		SynchedAnimationVariableKey<T> variableKey = SynchedAnimationVariableKey.byId(buf.readInt());
		AssetAccessor<? extends StaticAnimation> animation = AnimationManager.byId(buf.readInt());
		Action action = Action.values()[buf.readInt()];
		
		return new SPAnimationVariablePacket<> (entityId, variableKey, animation, action == AnimationVariablePacket.Action.PUT ? variableKey.getPacketBufferCodec().decode(buf) : null, action);
	}
	
	public static <T> void toBytes(SPAnimationVariablePacket<T> msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.entityId);
		buf.writeInt(msg.animationVariableKey.getId());
		buf.writeInt(msg.animation.get().getId());
		buf.writeInt(msg.action.ordinal());
		
		if (msg.action == AnimationVariablePacket.Action.PUT) {
			msg.animationVariableKey.getPacketBufferCodec().encode(msg.value, buf);
		}
	}
	
	public static <T> void handle(SPAnimationVariablePacket<T> msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.getInstance().player.level().getEntity(msg.entityId), LivingEntityPatch.class).ifPresent(msg::process);
		});
		ctx.get().setPacketHandled(true);
	}
}
