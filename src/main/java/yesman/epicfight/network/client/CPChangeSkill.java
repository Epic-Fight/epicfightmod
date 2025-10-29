package yesman.epicfight.network.client;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.RegistryManager;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPSetRemotePlayerSkill;
import yesman.epicfight.network.server.SPSetSkillContainerValue;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class CPChangeSkill {
	private final SkillSlot skillSlot;
	private final int skillBookSlotIndex;
	@Nullable
	private final Skill skill;
	
	public CPChangeSkill() {
		this(null, -1, null);
	}
	
	public CPChangeSkill(SkillSlot skillSlot, int skillBookSlotIndex, @Nullable Skill skill) {
		this.skillSlot = skillSlot;
		this.skillBookSlotIndex = skillBookSlotIndex;
		this.skill = skill;
	}
	
	public static CPChangeSkill fromBytes(FriendlyByteBuf buf) {
		return new CPChangeSkill(SkillSlot.ENUM_MANAGER.get(buf.readInt()), buf.readInt(), buf.isReadable() ? buf.readRegistryId() : null);
	}
	
	public static void toBytes(CPChangeSkill msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.skillSlot.universalOrdinal());
		buf.writeInt(msg.skillBookSlotIndex);
		
		if (msg.skill != null) {
			buf.writeRegistryId(RegistryManager.ACTIVE.getRegistry(SkillManager.SKILL_REGISTRY_KEY), msg.skill);
		}
	}
	
	public static void handle(CPChangeSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			EpicFightCapabilities.getUnparameterizedEntityPatch(ctx.get().getSender(), ServerPlayerPatch.class).ifPresent(playerpatch -> {
				SkillContainer skillContainer = playerpatch.getSkill(msg.skillSlot);
				boolean skillEquipped = (!skillContainer.onReplaceCooldown() || msg.skillBookSlotIndex >= 0) && skillContainer.setSkill(msg.skill);
				
				if (msg.skill != null) {
					if (msg.skill.getCategory().learnable()) {
						playerpatch.getSkillCapability().addLearnedSkill(msg.skill);
					}
					
					if (skillEquipped && msg.skillBookSlotIndex >= 0) {
						if (!playerpatch.getOriginal().isCreative()) playerpatch.getOriginal().getInventory().removeItem(playerpatch.getOriginal().getInventory().getItem(msg.skillBookSlotIndex));
					}
				}
				
				if (skillEquipped) {
					skillContainer.setReplaceCooldown(EpicFightGameRules.SKILL_REPLACE_COOLDOWN.getRuleValue(playerpatch.getOriginal().level()));
					EpicFightNetworkManager.sendToPlayer(SPSetSkillContainerValue.replaceCooldown(skillContainer.getSlot(), skillContainer.getReplaceCooldown(), playerpatch.getOriginal().getId()), playerpatch.getOriginal());
					EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(new SPSetRemotePlayerSkill(playerpatch.getOriginal().getId(), msg.skillSlot, msg.skill), playerpatch.getOriginal());
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
