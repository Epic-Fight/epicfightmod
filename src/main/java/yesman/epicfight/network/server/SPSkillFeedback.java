package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.SkillSlot;

public class SPSkillFeedback implements ManagedCustomPacketPayload {
	public static final StreamCodec<ByteBuf, SPSkillFeedback> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			SPSkillFeedback::skillSlot,
			ByteBufCodecsExtends.enumCodec(FeedbackType.class),
			SPSkillFeedback::feedbackType,
			ByteBufCodecsExtends.ofNullable(ByteBufCodecs.COMPOUND_TAG),
			SPSkillFeedback::arguments,
			SPSkillFeedback::new
	    );
	
	private final CompoundTag arguments;
	private final SkillSlot skillSlot;
	private FeedbackType feedbackType;
	
	public static SPSkillFeedback executed(SkillSlot skillSlot) {
		return new SPSkillFeedback(skillSlot, FeedbackType.EXECUTED);
	}
	
	public static SPSkillFeedback expired(SkillSlot skillSlot) {
		return new SPSkillFeedback(skillSlot, FeedbackType.EXPIRED);
	}
	
	public static SPSkillFeedback held(SkillSlot skillSlot) {
		return new SPSkillFeedback(skillSlot, FeedbackType.HOLDING_START);
	}
	
	private SPSkillFeedback(SkillSlot skillSlot, FeedbackType feedbackType) {
		this.skillSlot = skillSlot;
		this.feedbackType = feedbackType;
		this.arguments = new CompoundTag();
	}
	
	private SPSkillFeedback(SkillSlot skillSlot, FeedbackType feedbackType, CompoundTag args) {
		this.skillSlot = skillSlot;
		this.feedbackType = feedbackType;
		this.arguments = args;
	}
	
	public SkillSlot skillSlot() {
		return this.skillSlot;
	}
	
	public FeedbackType feedbackType() {
		return this.feedbackType;
	}
	
	public CompoundTag arguments() {
		return this.arguments;
	}
	
	public void setFeedbackType(FeedbackType feedbackType) {
		this.feedbackType = feedbackType;
	}
	
	public enum FeedbackType {
		EXECUTED, CHARGING_BEGIN, EXPIRED, HOLDING_START
	}
}
