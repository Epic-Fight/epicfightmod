package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.utils.LevelUtil;

public class SPFracture {
	private final double radius;
	private final Vec3 location;
	private final boolean noSound;
	private final boolean noParticle;
	
	public SPFracture() {
		this(Vec3.ZERO, 0.0D);
	}

	public SPFracture(Vec3 location, double radius) {
		this(location, radius, false, false);
	}
	
	public SPFracture(Vec3 location, double radius, boolean noSound, boolean noParticle) {
		this.location = location;
		this.radius = radius;
		this.noSound = noSound;
		this.noParticle = noParticle;
	}
	
	public static SPFracture fromBytes(FriendlyByteBuf buf) {
		return new SPFracture(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readDouble(), buf.readBoolean(), buf.readBoolean());
	}

	public static void toBytes(SPFracture msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.location.x);
		buf.writeDouble(msg.location.y);
		buf.writeDouble(msg.location.z);
		buf.writeDouble(msg.radius);
		buf.writeBoolean(msg.noSound);
		buf.writeBoolean(msg.noParticle);
	}
	
	public static void handle(SPFracture msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			LevelUtil.circleSlamFracture(null, mc.level, msg.location, msg.radius, msg.noSound, msg.noParticle);
		});
		
		ctx.get().setPacketHandled(true);
	}
}