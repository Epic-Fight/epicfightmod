package yesman.epicfight.network.server;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public class SPEntityPairingPacket implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPEntityPairingPacket> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT,
			SPEntityPairingPacket::entityId,
			ByteBufCodecsExtends.extendableEnumCodec(EntityPairingPacketType.ENUM_MANAGER),
			SPEntityPairingPacket::pairingPacketType,
			ByteBufCodecs.BYTE_ARRAY,
			payload -> payload.buffer.array(),
			SPEntityPairingPacket::new
	    );
	
	private final int entityId;
	private final EntityPairingPacketType type;
	private final FriendlyByteBuf buffer;
	
	public SPEntityPairingPacket(int entityId, EntityPairingPacketType eventType) {
		this.entityId = entityId;
		this.type = eventType;
		this.buffer = new FriendlyByteBuf(Unpooled.buffer());
	}
	
	public SPEntityPairingPacket(int entityId, EntityPairingPacketType eventType, byte[] bytes) {
		this.entityId = entityId;
		this.type = eventType;
		this.buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(bytes));
	}
	
	public int entityId() {
		return this.entityId;
	}
	
	public EntityPairingPacketType pairingPacketType() {
		return this.type;
	}
	
	public FriendlyByteBuf buffer() {
		return this.buffer;
	}
}