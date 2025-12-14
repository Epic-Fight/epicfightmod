package yesman.epicfight.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.provider.EntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.ItemCapabilityProvider;
import yesman.epicfight.world.capabilities.provider.SkillCapabilityProvider;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID)
public class CapabilityEvents {
    private static final ResourceLocation ENTITY_CAPABILITY_KEY = EpicFightMod.identifier("entity_cap");
    private static final ResourceLocation ITEM_CAPABILITY_KEY = EpicFightMod.identifier("item_cap");
    private static final ResourceLocation SKILL_CAPABILITY_KEY = EpicFightMod.identifier("skill_cap");
	
	@SubscribeEvent
	public static void attachItemCapability(AttachCapabilitiesEvent<ItemStack> event) {
		if (event.getObject() != null) {
			ItemCapabilityProvider prov = new ItemCapabilityProvider(event.getObject());
			
			if (prov.hasCapability()) {
				event.addCapability(ITEM_CAPABILITY_KEY, prov);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SubscribeEvent
	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
		EntityPatch oldEntitypatch = EpicFightCapabilities.getEntityPatch(event.getObject(), EntityPatch.class);
		
		if (oldEntitypatch == null) {
			EntityPatchProvider prov = new EntityPatchProvider(event.getObject());
			
			if (prov.hasCapability()) {
				EntityPatch entitypatch = prov.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
				
				entitypatch.onConstructed(event.getObject());
				event.addCapability(ENTITY_CAPABILITY_KEY, prov);
				
				if (entitypatch instanceof PlayerPatch<?> playerpatch) {
					if (event.getObject().getCapability(EpicFightCapabilities.CAPABILITY_SKILL).orElse(null) == null) {
						
						if (playerpatch != null) {
							SkillCapabilityProvider skillProvider = new SkillCapabilityProvider(playerpatch);
							event.addCapability(SKILL_CAPABILITY_KEY, skillProvider);
						}
					}
				}
			}
		}
	}
}