package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.client.ClientEngine;

public record SPPlayUISound(SoundEvent sound, float pitch, float volume) {
	public SPPlayUISound(SoundEvent sound) {
		this(sound, 1.0F, 1.0F);
	}
	
	public static SPPlayUISound fromBytes(FriendlyByteBuf buf) {
		return new SPPlayUISound(buf.readRegistryId(), buf.readFloat(), buf.readFloat());
	}
	
	public static void toBytes(SPPlayUISound msg, FriendlyByteBuf buf) {
		buf.writeRegistryId(ForgeRegistries.SOUND_EVENTS, msg.sound);
		buf.writeFloat(msg.pitch());
		buf.writeFloat(msg.volume());
	}
	
	public static void handle(SPPlayUISound msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientEngine.getInstance().playUISound(msg);
		});
		ctx.get().setPacketHandled(true);
	}
}
