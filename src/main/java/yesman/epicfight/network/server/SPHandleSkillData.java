package yesman.epicfight.network.server;

import io.netty.buffer.Unpooled;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillSlot;

public record SPHandleSkillData(SPHandleSkillData.WorkType workType, SkillSlot skillSlot, int entityId, Holder<SkillDataKey<?>> skillDataKey, FriendlyByteBuf buffer) implements ManagedCustomPacketPayload {
	// From packet buffer
	public SPHandleSkillData(SPHandleSkillData.WorkType workType, SkillSlot skillSlot, int entityId, Holder<SkillDataKey<?>> skillDataKey, byte[] bytes) {
		this(workType, skillSlot, entityId, skillDataKey, new FriendlyByteBuf(Unpooled.copiedBuffer(bytes)));
	}
	
	public SPHandleSkillData(SPHandleSkillData.WorkType workType, SkillSlot skillSlot, int entityId, Holder<SkillDataKey<?>> skillDataKey) {
		this(workType, skillSlot, entityId, skillDataKey, new FriendlyByteBuf(Unpooled.buffer()));
	}
	
	public static final StreamCodec<RegistryFriendlyByteBuf, SPHandleSkillData> STREAM_CODEC =
		StreamCodec.composite(
	        ByteBufCodecsExtends.enumCodec(SPHandleSkillData.WorkType.class),
	        SPHandleSkillData::workType,
	        ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
	        SPHandleSkillData::skillSlot,
	        ByteBufCodecs.INT,
	        SPHandleSkillData::entityId,
	        SkillDataKey.STREAM_CODEC,
	        SPHandleSkillData::skillDataKey,
	        ByteBufCodecs.BYTE_ARRAY,
	        payload -> payload.buffer().array(),
	        SPHandleSkillData::new
	    );
	
	public enum WorkType {
		REGISTER, REMOVE, MODIFY
	}
}