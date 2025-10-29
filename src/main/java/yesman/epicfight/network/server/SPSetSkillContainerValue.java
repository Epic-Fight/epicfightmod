package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPSetSkillContainerValue(Target target, SkillSlot skillSlot, float floatVal, boolean boolVal, int entityId) {
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
	
	public static void toBytes(SPSetSkillContainerValue msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.target());
		buf.writeInt(msg.skillSlot().universalOrdinal());
		buf.writeFloat(msg.floatVal());
		buf.writeBoolean(msg.boolVal());
		buf.writeInt(msg.entityId());
	}
	
	public static void handle(SPSetSkillContainerValue msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.getInstance().level.getEntity(msg.entityId()), PlayerPatch.class).ifPresent(playerpatch -> {
				SkillContainer container = playerpatch.getSkill(msg.skillSlot());
				
				switch (msg.target()) {
				case ENABLE -> container.setDisabled(msg.boolVal());
				case ACTIVATE -> { if (msg.boolVal()) container.activate(); else container.deactivate(); }
				case RESOURCE -> container.setResource(msg.floatVal());
				case DURATION -> container.setDuration((int)msg.floatVal());
				case MAX_DURATION -> container.setMaxDuration((int)msg.floatVal());
				case STACKS -> container.setStack((int)msg.floatVal());
				case MAX_RESOURCE -> container.setMaxResource(msg.floatVal());
				case REPLACE_COOLDOWN -> container.setReplaceCooldown((int)msg.floatVal());
				}
			});
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	public enum Target {
		ENABLE, ACTIVATE, RESOURCE, DURATION, STACKS, MAX_RESOURCE, MAX_DURATION, REPLACE_COOLDOWN;
	}
}