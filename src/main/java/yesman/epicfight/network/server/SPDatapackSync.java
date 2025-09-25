package yesman.epicfight.network.server;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;

public class SPDatapackSync implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPDatapackSync> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.enumCodec(SPDatapackSync.PacketType.class),
			SPDatapackSync::packetType,
			ByteBufCodecsExtends.listOf(ByteBufCodecs.COMPOUND_TAG),
			SPDatapackSync::tags,
			SPDatapackSync::new
	    );
	
	private final SPDatapackSync.PacketType packetType;
	private final List<CompoundTag> tags;
	
	public SPDatapackSync(SPDatapackSync.PacketType packetType, List<CompoundTag> tags) {
		this.packetType = packetType;
		this.tags = tags;
	}
	
	public SPDatapackSync(SPDatapackSync.PacketType packetType) {
		this.packetType = packetType;
		this.tags = new ArrayList<> ();
	}
	
	public SPDatapackSync.PacketType packetType() {
		return this.packetType;
	}
	
	public List<CompoundTag> tags() {
		return this.tags;
	}
	
	public void addTag(CompoundTag compound) {
		this.tags.add(compound);
	}
	
	public enum PacketType {
		ARMOR, WEAPON, MOB, SKILL_PARAMS, WEAPON_TYPE, ITEM_KEYWORD, MANDATORY_RESOURCE_PACK_ANIMATION, RESOURCE_PACK_ANIMATION
	}
}