package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public record SPRemoveSkillAndLearn(SkillSlot skillSlot, Skill skill) {
	public SPRemoveSkillAndLearn() {
		this(SkillSlots.BASIC_ATTACK, EpicFightSkills.BASIC_ATTACK);
	}
	
	public static SPRemoveSkillAndLearn fromBytes(FriendlyByteBuf buf) {
		return new SPRemoveSkillAndLearn(SkillSlot.ENUM_MANAGER.getOrThrow(buf.readInt()), buf.readRegistryId());
	}
	
	public static void toBytes(SPRemoveSkillAndLearn msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillSlot.universalOrdinal());
		
		if (msg.skill != null) {
			buf.writeRegistryId(RegistryManager.ACTIVE.getRegistry(SkillManager.SKILL_REGISTRY_KEY), msg.skill);
		}
	}
	
	public static void handle(SPRemoveSkillAndLearn msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)mc.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (playerpatch != null) {
				playerpatch.getSkillCapability().removeLearnedSkill(msg.skill);
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
				
				if (skillContainer.getSkill() == msg.skill) {
					skillContainer.setSkill(null);
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}