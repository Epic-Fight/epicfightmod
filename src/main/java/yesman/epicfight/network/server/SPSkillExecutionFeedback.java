package yesman.epicfight.network.server;

import java.util.function.Supplier;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.modules.HoldableSkill;

public class SPSkillExecutionFeedback {
	private final int skillSlot;
	private FeedbackType feedbackType;
	private final FriendlyByteBuf buffer;
	
	public SPSkillExecutionFeedback() {
		this(0, FeedbackType.EXECUTED);
	}
	
	public static SPSkillExecutionFeedback executed(int slotIndex) {
		return new SPSkillExecutionFeedback(slotIndex, FeedbackType.EXECUTED);
	}
	
	public static SPSkillExecutionFeedback expired(int slotIndex) {
		return new SPSkillExecutionFeedback(slotIndex, FeedbackType.EXPIRED);
	}

	public static SPSkillExecutionFeedback held(int slotIndex) {
		return new SPSkillExecutionFeedback(slotIndex, FeedbackType.HOLDING_START);
	}
	
	private SPSkillExecutionFeedback(int slotIndex, FeedbackType feedbackType) {
		this.skillSlot = slotIndex;
		this.feedbackType = feedbackType;
		this.buffer = new FriendlyByteBuf(Unpooled.buffer());
	}

	public FriendlyByteBuf getBuffer() {
		return buffer;
	}
	
	public void setFeedbackType(FeedbackType feedbackType) {
		this.feedbackType = feedbackType;
	}
	
	public static SPSkillExecutionFeedback fromBytes(FriendlyByteBuf buf) {
		SPSkillExecutionFeedback msg = new SPSkillExecutionFeedback(buf.readInt(), buf.readEnum(FeedbackType.class));

		while (buf.isReadable()) {
			msg.buffer.writeByte(buf.readByte());
		}
		
		return msg;
	}

	public static void toBytes(SPSkillExecutionFeedback msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillSlot);
		buf.writeEnum(msg.feedbackType);

		while (msg.buffer.isReadable()) {
			buf.writeByte(msg.buffer.readByte());
		}
	}
	
	public static void handle(SPSkillExecutionFeedback msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			LocalPlayerPatch playerpatch = ClientEngine.getInstance().getPlayerPatch();
			
			if (playerpatch != null) {
				switch(msg.feedbackType) {
					case EXECUTED -> {
						SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
						skillContainer.getSkill().executeOnClient(skillContainer, msg.getBuffer());
					}
					case HOLDING_START -> {
						SkillContainer container = playerpatch.getSkill(msg.skillSlot);
						
						if (container.getSkill() instanceof HoldableSkill holdableSkill) {
							playerpatch.startSkillHolding(holdableSkill);
							ClientEngine.getInstance().controlEngine.setHoldingKey(container.getSlot(), holdableSkill.getKeyMapping());
						}
					}
					case EXPIRED -> {
						SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
						skillContainer.getSkill().cancelOnClient(skillContainer, msg.getBuffer());
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
	
	public enum FeedbackType {
		EXECUTED, HOLDING_START, EXPIRED
	}
}