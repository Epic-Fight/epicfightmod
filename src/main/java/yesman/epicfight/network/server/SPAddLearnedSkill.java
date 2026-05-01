package yesman.epicfight.network.server;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.Skill;

public record SPAddLearnedSkill(List<Holder<Skill>> skills) implements ManagedCustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, SPAddLearnedSkill> STREAM_CODEC =
		StreamCodec.composite(
	        ByteBufCodecsExtends.listOfHolder(Skill.STREAM_CODEC),
	        SPAddLearnedSkill::skills,
	        SPAddLearnedSkill::new
	    );
}