package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class SPPlayAnimationAndSetTarget extends SPAnimatorControl {
	protected int targetId;
	
	public SPPlayAnimationAndSetTarget(Action action, int animationId, int entityId, float modifyTime, boolean paused, int targetId) {
		super(action, animationId, entityId, modifyTime, paused);
		this.targetId = targetId;
	}
	
	public SPPlayAnimationAndSetTarget(Action action, AssetAccessor<? extends StaticAnimation> animation, float modifyTime, LivingEntityPatch<?> entitypatch) {
		super(action, animation, modifyTime, entitypatch);
		this.targetId = entitypatch.getTarget().getId();
	}
	
	@Override
	public void onArrive() {
		super.onArrive();
		
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.player.level().getEntity(this.entityId);
		Entity target = mc.player.level().getEntity(this.targetId);

		if (entity instanceof Mob entityliving && target instanceof LivingEntity) {
			entityliving.setTarget((LivingEntity)target);
		}
	}
	
	public static SPPlayAnimationAndSetTarget fromBytes(FriendlyByteBuf buf) {
		return new SPPlayAnimationAndSetTarget(buf.readEnum(Action.class), buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean(), buf.readInt());
	}

	public static void toBytes(SPPlayAnimationAndSetTarget msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.action);
		buf.writeInt(msg.animationId);
		buf.writeInt(msg.entityId);
		buf.writeFloat(msg.transitionTimeModifier);
		buf.writeBoolean(msg.pause);
		buf.writeInt(msg.targetId);
	}

	public static void handle(SPPlayAnimationAndSetTarget msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			msg.onArrive();
		});
		ctx.get().setPacketHandled(true);
	}
}