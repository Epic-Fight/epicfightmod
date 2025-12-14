package yesman.epicfight.network.server;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

public class SPAddLearnedSkill {
	private String[] skillNames;
	private final int count;
	
	public SPAddLearnedSkill() {
		this("");
	}
	
	public SPAddLearnedSkill(String... skillNames) {
		this.skillNames = skillNames;
		this.count = skillNames.length;
	}
	
	public static SPAddLearnedSkill fromBytes(FriendlyByteBuf buf) {
		SPAddLearnedSkill msg = new SPAddLearnedSkill();
		int count  = buf.readInt();
		msg.skillNames = new String[count];
		
		for (int i = 0; i < count; i++) {
			msg.skillNames[i] = buf.readUtf();
		}
		
		return msg;
	}
	
	public static void toBytes(SPAddLearnedSkill msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.count);
		for (String skillName : msg.skillNames) {
			buf.writeUtf(skillName);
		}
	}
	
	public static void handle(SPAddLearnedSkill msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			PlayerPatch<?> playerpatch = (PlayerPatch<?>)mc.player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			CapabilitySkill skillCapability = playerpatch.getSkillCapability();
			
			for (String skillName : msg.skillNames) {
				skillCapability.addLearnedSkill(SkillManager.getSkill(skillName));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}