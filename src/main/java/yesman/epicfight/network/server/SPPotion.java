package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

public class SPPotion {
	private final MobEffectInstance effectInstance;
	private final Action action;
	private final int entityId;

	public SPPotion() {
		this.effectInstance = null;
		this.entityId = 0;
		this.action = Action.REMOVE;
	}

	public SPPotion(MobEffectInstance effect, Action action, int entityId) {
		this.effectInstance = effect;
		this.entityId = entityId;
		this.action = action;
	}

	public static SPPotion fromBytes(FriendlyByteBuf buf) {
		MobEffect effect = MobEffect.byId(buf.readInt());
		MobEffectInstance effectInstance = new MobEffectInstance(effect, 0, buf.readInt());
		
		int entityId = buf.readInt();
		Action action = buf.readEnum(Action.class);
		
		return new SPPotion(effectInstance, action, entityId);
	}

	public static void toBytes(SPPotion msg, FriendlyByteBuf buf) {
		buf.writeInt(MobEffect.getId(msg.effectInstance.getEffect()));
		buf.writeInt(msg.effectInstance.getAmplifier());
		buf.writeInt(msg.entityId);
		buf.writeEnum(msg.action);
	}
	
	public static void handle(SPPotion msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(()->{
			Minecraft mc = Minecraft.getInstance();
			Entity entity = mc.level.getEntity(msg.entityId);
			
			if (entity != null && entity instanceof LivingEntity livEntity) {
				switch (msg.action) {
				case ACTIVATE -> {
					livEntity.addEffect(msg.effectInstance);
				}
				case REMOVE -> {
					livEntity.removeEffect(msg.effectInstance.getEffect());
				}
				}
			}
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	public enum Action {
		ACTIVATE, REMOVE;
	}
}