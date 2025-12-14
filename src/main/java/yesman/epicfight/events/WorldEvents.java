package yesman.epicfight.events;

import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.data.loot.SkillBookLootModifier;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID)
public class WorldEvents {
	@SubscribeEvent
	public static void onLootTableRegistry(final LootTableLoadEvent event) {
		EpicFightLootTables.modifyVanillaLootPools(event);
		SkillBookLootModifier.createSkillLootTable();
    }
	
	@SubscribeEvent
	public static void onDatapackSync(final OnDatapackSyncEvent event) {
		if (event.getPlayer() != null) {
			PayloadBundleBuilder payloadBundleBuilder = PayloadBundleBuilder.create();
			
			EpicFightGameRules.GAME_RULES.values().stream().filter(ConfigurableGameRule::shouldSync).forEach(gamerule -> {
				payloadBundleBuilder.and(gamerule.getSyncPacket(event.getPlayer()));
			});
			
			payloadBundleBuilder.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, event.getPlayer(), others));
			
			if (!event.getPlayer().getServer().isSingleplayerOwner(event.getPlayer().getGameProfile())) {
				synchronizeWorldData(event.getPlayer());
			} else {
				EpicFightCapabilities.getUnparameterizedEntityPatch(event.getPlayer(), ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
					CapabilitySkill skillCapability = serverplayerpatch.getSkillCapability();
					
					skillCapability.listSkillContainers().forEach(skillContainer -> {
						if (skillContainer.getSkill() != null) {
							// Reload skill
							skillContainer.setSkill(SkillManager.getSkill(skillContainer.getSkill().toString()), true);
						}
					});
				});
			}
		} else {
			event.getPlayerList().getPlayers().forEach(WorldEvents::synchronizeWorldData);
		}
    }
	
	public static void synchronizeWorldData(ServerPlayer player) {
		EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
			CapabilitySkill skillCapability = serverplayerpatch.getSkillCapability();
			
			skillCapability.listSkillContainers().forEach(skillContainer -> {
				if (skillContainer.getSkill() != null) {
					// Reload skill
					skillContainer.setSkill(SkillManager.getSkill(skillContainer.getSkill().toString()), true);
				}
			});
			
			List<CompoundTag> skillParams = SkillManager.getSkillParams();
			SPDatapackSync skillParamsPacket = new SPDatapackSync(skillParams.size(), SPDatapackSync.Type.SKILL_PARAMS);
			skillParams.forEach(skillParamsPacket::write);
			EpicFightNetworkManager.sendToPlayer(skillParamsPacket, player);
		});
		
		SPDatapackSync animationPacket = new SPDatapackSync(AnimationManager.getInstance().getResourcepackAnimationCount(), player.getServer().isResourcePackRequired() ? SPDatapackSync.Type.MANDATORY_RESOURCE_PACK_ANIMATION : SPDatapackSync.Type.RESOURCE_PACK_ANIMATION);
		SPDatapackSync armorPacket = new SPDatapackSync(ItemCapabilityReloadListener.armorCount(), SPDatapackSync.Type.ARMOR);
		SPDatapackSync weaponPacket = new SPDatapackSync(ItemCapabilityReloadListener.weaponCount(), SPDatapackSync.Type.WEAPON);
		SPDatapackSync mobCapabilityPacket = new SPDatapackSync(MobPatchReloadListener.getTagCount(), SPDatapackSync.Type.MOB);
		SPDatapackSync weaponTypePacket = new SPDatapackSync(WeaponTypeReloadListener.getTagCount(), SPDatapackSync.Type.WEAPON_TYPE);
		SPDatapackSync itemKeywordPacket = new SPDatapackSync(ItemKeywordReloadListener.getRegexes().size(), SPDatapackSync.Type.ITEM_KEYWORD);
		
		AnimationManager.getInstance().getResourcepackAnimationStream().forEach(animationPacket::write);
		ItemCapabilityReloadListener.getArmorDataStream().forEach(armorPacket::write);
		ItemCapabilityReloadListener.getWeaponDataStream().forEach(weaponPacket::write);
		MobPatchReloadListener.getDataStream().forEach(mobCapabilityPacket::write);
		WeaponTypeReloadListener.getWeaponTypeDataStream().forEach(weaponTypePacket::write);
		ItemKeywordReloadListener.getCompounds().forEach(itemKeywordPacket::write);
		
		EpicFightNetworkManager.sendToPlayer(animationPacket, player);
		EpicFightNetworkManager.sendToPlayer(weaponTypePacket, player);
		EpicFightNetworkManager.sendToPlayer(armorPacket, player);
		EpicFightNetworkManager.sendToPlayer(weaponPacket, player);
		EpicFightNetworkManager.sendToPlayer(mobCapabilityPacket, player);
		EpicFightNetworkManager.sendToPlayer(itemKeywordPacket, player);
	}
	
	@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, value = Dist.CLIENT)
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