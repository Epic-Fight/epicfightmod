package yesman.epicfight.network.client;

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

public record CPHandleSkillData(SkillSlot skillSlot, Holder<SkillDataKey<?>> skillDataKey, FriendlyByteBuf buffer) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, CPHandleSkillData> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			CPHandleSkillData::skillSlot,
			SkillDataKey.STREAM_CODEC,
			CPHandleSkillData::skillDataKey,
			ByteBufCodecs.BYTE_ARRAY,
			payload -> payload.buffer().array(),
			CPHandleSkillData::new
	    );
	
	// From codec
	public CPHandleSkillData(SkillSlot skillSlot, Holder<SkillDataKey<?>> skillDataKey, byte[] bytes) {
		this(skillSlot, skillDataKey, new FriendlyByteBuf(Unpooled.copiedBuffer(bytes)));
	}
	
	public CPHandleSkillData(SkillSlot skillSlot, Holder<SkillDataKey<?>> skillDataKey) {
		this(skillSlot, skillDataKey, new FriendlyByteBuf(Unpooled.buffer()));
	}
}