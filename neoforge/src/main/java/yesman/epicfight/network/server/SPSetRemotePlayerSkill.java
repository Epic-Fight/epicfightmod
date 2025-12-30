package yesman.epicfight.network.server;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;

public record SPSetRemotePlayerSkill(SkillSlot skillSlot, int entityId, @Nullable Holder<Skill> skill) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPSetRemotePlayerSkill> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			SPSetRemotePlayerSkill::skillSlot,
			ByteBufCodecs.INT,
			SPSetRemotePlayerSkill::entityId,
			ByteBufCodecsExtends.ofNullable(Skill.STREAM_CODEC),
			SPSetRemotePlayerSkill::skill,
			SPSetRemotePlayerSkill::new
	    );
}
