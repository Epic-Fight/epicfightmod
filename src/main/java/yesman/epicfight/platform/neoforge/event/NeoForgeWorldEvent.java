package yesman.epicfight.platform.neoforge.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;

@EventBusSubscriber(modid = EpicFight.MODID)
public final class NeoForgeWorldEvent {
	private NeoForgeWorldEvent() {}
	
	@SubscribeEvent
	public static void onDatapackSync(final OnDatapackSyncEvent event) {
		if (event.getPlayer() != null) {
			PayloadBundleBuilder payloadBundleBuilder = PayloadBundleBuilder.create();
			
			EpicFightGameRules.GAME_RULES.values().stream().filter(ConfigurableGameRule::shouldSync).forEach(gamerule -> {
				payloadBundleBuilder.and(gamerule.getSyncPacket(event.getPlayer()));
			});
			
			payloadBundleBuilder.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, event.getPlayer(), others));
			
			if (!event.getPlayer().getServer().isSingleplayerOwner(event.getPlayer().getGameProfile())) {
				sendLevelData(event.getPlayer());
			} else {
				EpicFightCapabilities.getUnparameterizedEntityPatch(event.getPlayer(), ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
					PlayerSkills skillCapability = serverplayerpatch.getPlayerSkills();
					
					skillCapability.listSkillContainers().forEach(skillContainer -> {
						if (skillContainer.getSkill() != null) {
							// Reload skill
							skillContainer.setSkill(skillContainer.getSkill(), true);
						}
					});
				});
			}
		} else {
			event.getPlayerList().getPlayers().forEach(NeoForgeWorldEvent::sendLevelData);
		}
    }
	
	private static void sendLevelData(ServerPlayer player) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
			PlayerSkills skillCapability = serverplayerpatch.getPlayerSkills();
			
			skillCapability.listSkillContainers().forEach(skillContainer -> {
				if (skillContainer.getSkill() != null) {
					// Reload skill
					skillContainer.setSkill(skillContainer.getSkill(), true);
				}
			});
			
			SPDatapackSync skillParamsPacket = new SPDatapackSync(SPDatapackSync.PacketType.SKILL_PARAMS);
			SkillReloadListener.getSkillParams().forEach(skillParamsPacket::addTag);
			EpicFightNetworkManager.sendToPlayer(skillParamsPacket, player);
		});
		
		SPDatapackSync animationPacket = new SPDatapackSync(player.getServer().isResourcePackRequired() ? SPDatapackSync.PacketType.MANDATORY_RESOURCE_PACK_ANIMATION : SPDatapackSync.PacketType.RESOURCE_PACK_ANIMATION);
		SPDatapackSync armorPacket = new SPDatapackSync(SPDatapackSync.PacketType.ARMOR);
		SPDatapackSync weaponPacket = new SPDatapackSync(SPDatapackSync.PacketType.WEAPON);
		SPDatapackSync mobCapabilityPacket = new SPDatapackSync(SPDatapackSync.PacketType.MOB);
        SPDatapackSync itemPresetPacket = new SPDatapackSync(SPDatapackSync.PacketType.ITEM_PRESET);
        SPDatapackSync conditionalPacket = new SPDatapackSync(SPDatapackSync.PacketType.PROVIDER_CONDITIONAL);
        SPDatapackSync movesetPacket = new SPDatapackSync(SPDatapackSync.PacketType.MOVESET);
		SPDatapackSync weaponModifierPacket = new SPDatapackSync(SPDatapackSync.PacketType.MODIFIER);
		SPDatapackSync weaponTypePacket = new SPDatapackSync(SPDatapackSync.PacketType.WEAPON_TYPE);
		SPDatapackSync itemKeywordPacket = new SPDatapackSync(SPDatapackSync.PacketType.ITEM_KEYWORD);
		
		AnimationManager.getInstance().getResourcepackAnimationStream().forEach(animationPacket::addTag);
		ItemCapabilityReloadListener.getArmorDataStream().forEach(armorPacket::addTag);
		ItemCapabilityReloadListener.getWeaponDataStream().forEach(weaponPacket::addTag);
		MobPatchReloadListener.getDataStream().forEach(mobCapabilityPacket::addTag);
		WeaponTypeReloadListener.getWeaponTypeDataStream().forEach(weaponTypePacket::addTag);
		ItemKeywordReloadListener.getCompounds().forEach(itemKeywordPacket::addTag);
		
		EpicFightNetworkManager.PayloadBundleBuilder
			.beginWith(animationPacket)
            .and(itemPresetPacket)
			.and(weaponModifierPacket)
			.and(conditionalPacket)
            .and(movesetPacket)
			.and(weaponTypePacket)
			.and(armorPacket)
			.and(weaponPacket)
			.and(mobCapabilityPacket)
			.and(itemKeywordPacket)
			.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, player, others));
	}
	
	@EventBusSubscriber(modid = EpicFight.MODID, value = Dist.CLIENT)
	public static class WorldEventsClient {
		@SubscribeEvent
		public static void loadLevel(LevelEvent.Load event) {
			// Prevent infinite loop
			if (event.getLevel() instanceof FakeLevel) return;
			if (event.getLevel() instanceof ClientLevel clientLevel) FakeLevel.getFakeLevel(clientLevel);
		}
		
		@SubscribeEvent
		public static void unloadLevel(LevelEvent.Unload event) {
			FakeLevel.unloadFakeLevel();
		}
	}
}
