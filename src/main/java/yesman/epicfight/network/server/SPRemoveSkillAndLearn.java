package yesman.epicfight.network.server;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;

public record SPRemoveSkillAndLearn(Holder<Skill> skill, SkillSlot skillSlot) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPRemoveSkillAndLearn> STREAM_CODEC =
		StreamCodec.composite(
			Skill.STREAM_CODEC,
			SPRemoveSkillAndLearn::skill,
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			SPRemoveSkillAndLearn::skillSlot,
			SPRemoveSkillAndLearn::new
	    );
	
	public SPRemoveSkillAndLearn(Skill skill, SkillSlot skillSlot) {
		this(skill.holder(), skillSlot);
	}
}