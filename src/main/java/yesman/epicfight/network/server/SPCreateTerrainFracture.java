package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record SPCreateTerrainFracture(Vec3 location, double radius, boolean noSound, boolean noParticle) implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPCreateTerrainFracture> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.VEC3,
			SPCreateTerrainFracture::location,
			ByteBufCodecs.DOUBLE,
			SPCreateTerrainFracture::radius,
			ByteBufCodecs.BOOL,
			SPCreateTerrainFracture::noSound,
			ByteBufCodecs.BOOL,
			SPCreateTerrainFracture::noParticle,
			SPCreateTerrainFracture::new
	    );

	public SPCreateTerrainFracture(Vec3 location, double radius) {
		this(location, radius, false, false);
	}
}