package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.skill.SkillSlot;

public record SPSetSkillContainerValue(Target target, SkillSlot skillSlot, float floatVal, boolean boolVal, int entityId) implements ManagedCustomPacketPayload {
	public static SPSetSkillContainerValue enable(SkillSlot skillSlot, boolean flag, int entityId) {
		return new SPSetSkillContainerValue(Target.ENABLE, skillSlot, Float.NaN, flag, entityId);
	}
	
	public static SPSetSkillContainerValue activate(SkillSlot skillSlot, boolean flag, int entityId) {
		return new SPSetSkillContainerValue(Target.ACTIVATE, skillSlot, Float.NaN, flag, entityId);
	}
	
	public static SPSetSkillContainerValue resource(SkillSlot skillSlot, float value, int entityId) {
		return new SPSetSkillContainerValue(Target.RESOURCE, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue duration(SkillSlot skillSlot, int value, int entityId) {
		return new SPSetSkillContainerValue(Target.DURATION, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue stacks(SkillSlot skillSlot, int value, int entityId) {
		return new SPSetSkillContainerValue(Target.STACKS, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue maxResource(SkillSlot skillSlot, float value, int entityId) {
		return new SPSetSkillContainerValue(Target.MAX_RESOURCE, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue maxDuration(SkillSlot skillSlot, int value, int entityId) {
		return new SPSetSkillContainerValue(Target.MAX_DURATION, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue replaceCooldown(SkillSlot skillSlot, int value, int entityId) {
		return new SPSetSkillContainerValue(Target.REPLACE_COOLDOWN, skillSlot, value, false, entityId);
	}
	
	public static SPSetSkillContainerValue fromBytes(FriendlyByteBuf buf) {
		return new SPSetSkillContainerValue(buf.readEnum(Target.class), SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()), buf.readFloat(), buf.readBoolean(), buf.readInt());
	}
	
	public static final StreamCodec<ByteBuf, SPSetSkillContainerValue> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecsExtends.enumCodec(SPSetSkillContainerValue.Target.class),
			SPSetSkillContainerValue::target,
			ByteBufCodecsExtends.extendableEnumCodec(SkillSlot.ENUM_MANAGER),
			SPSetSkillContainerValue::skillSlot,
			ByteBufCodecs.FLOAT,
			SPSetSkillContainerValue::floatVal,
			ByteBufCodecs.BOOL,
			SPSetSkillContainerValue::boolVal,
			ByteBufCodecs.INT,
			SPSetSkillContainerValue::entityId,
			SPSetSkillContainerValue::new
	    );
	
	public enum Target {
		ENABLE, ACTIVATE, RESOURCE, DURATION, STACKS, MAX_RESOURCE, MAX_DURATION, REPLACE_COOLDOWN;
	}
}