package yesman.epicfight.network.client;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;

public record CPChangeSkill(SkillSlot skillSlot, @Nullable Holder<Skill> skill, int skillBookSlotIndex) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, CPChangeSkill> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			CPChangeSkill::skillSlot,
			ByteBufCodecsExtends.ofNullable(Skill.STREAM_CODEC),
			CPChangeSkill::skill,
			ByteBufCodecs.INT,
			CPChangeSkill::skillBookSlotIndex,
			CPChangeSkill::new
	    );
}
