package yesman.epicfight.network.client;

import java.util.function.Supplier;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class CPSkillRequest {
	private final SkillSlot skillSlot;
	private final WorkType workType;
	private final FriendlyByteBuf buffer;
	
	public CPSkillRequest(SkillSlot skillSlot) {
		this(skillSlot, WorkType.CAST);
	}
	
	public CPSkillRequest(SkillSlot skillSlot, WorkType active) {
		this.skillSlot = skillSlot;
		this.workType = active;
		this.buffer = new FriendlyByteBuf(Unpooled.buffer());
	}
	
	public CPSkillRequest(SkillSlot skillSlot, WorkType active, FriendlyByteBuf pb) {
		this.skillSlot = skillSlot;
		this.workType = active;
		this.buffer = new FriendlyByteBuf(Unpooled.buffer());
		
		if (pb != null) {
			this.buffer.writeBytes(pb);
		}
	}

	public FriendlyByteBuf getBuffer() {
		return this.buffer;
	}

	public static CPSkillRequest fromBytes(FriendlyByteBuf buf) {
		CPSkillRequest msg = new CPSkillRequest(SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()), buf.readEnum(WorkType.class));

		while (buf.isReadable()) {
			msg.buffer.writeByte(buf.readByte());
		}
		
		return msg;
	}

	public static void toBytes(CPSkillRequest msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillSlot.universalOrdinal());
		buf.writeEnum(msg.workType);
		
		while (msg.buffer.isReadable()) {
			buf.writeByte(msg.buffer.readByte());
		}
	}
	
	public static void handle(CPSkillRequest msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
				
				switch (msg.workType) {
					case CAST -> skillContainer.requestCasting(playerpatch, msg.getBuffer());
					case CANCEL -> skillContainer.requestCancel(playerpatch, msg.getBuffer());
					case HOLD_START -> skillContainer.requestHold(playerpatch, msg.getBuffer());
				}
			});
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	public enum WorkType {
		CAST, CANCEL, HOLD_START
	}
}