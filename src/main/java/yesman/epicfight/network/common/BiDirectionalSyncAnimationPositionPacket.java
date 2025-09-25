package yesman.epicfight.network.common;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public record BiDirectionalSyncAnimationPositionPacket(int entityId, float elapsedTime, Vec3 position, int lerpSteps) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, BiDirectionalSyncAnimationPositionPacket> STREAM_CODEC =
		StreamCodec.composite(
	        ByteBufCodecs.INT,
	        BiDirectionalSyncAnimationPositionPacket::entityId,
	        ByteBufCodecs.FLOAT,
	        BiDirectionalSyncAnimationPositionPacket::elapsedTime,
	        ByteBufCodecsExtends.VEC3,
	        BiDirectionalSyncAnimationPositionPacket::position,
	        ByteBufCodecs.INT,
	        BiDirectionalSyncAnimationPositionPacket::lerpSteps,
	        BiDirectionalSyncAnimationPositionPacket::new
	    );
}
