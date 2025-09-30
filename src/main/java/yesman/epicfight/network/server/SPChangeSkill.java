package yesman.epicfight.network.server;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPChangeSkill(SkillSlot skillSlot, int entityId, @Nullable Skill skill) {
	public static SPChangeSkill fromBytes(FriendlyByteBuf buf) {
		return new SPChangeSkill(SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()), buf.readInt(), buf.isReadable() ? buf.readRegistryId() : null);
	}
	
	public static void toBytes(SPChangeSkill msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillSlot().universalOrdinal());
		buf.writeInt(msg.entityId());
		
		if (msg.skill() != null) {
			buf.writeRegistryId(RegistryManager.ACTIVE.getRegistry(SkillManager.SKILL_REGISTRY_KEY), msg.skill());
		}
	}
	
	public static void handle(SPChangeSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(Minecraft.getInstance().level.getEntity(msg.entityId()), PlayerPatch.class).ifPresent(playerpatch -> {
				playerpatch.getSkill(msg.skillSlot()).setSkill(msg.skill());
				
				if (msg.skill() != null && msg.skillSlot().category().learnable()) {
					playerpatch.getSkillCapability().addLearnedSkill(msg.skill());
				}
				
				playerpatch.getSkill(msg.skillSlot()).setDisabled(false);
			});
		});
		
		ctx.get().setPacketHandled(true);
	}
}