package yesman.epicfight.compat.geckolib;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import software.bernie.geckolib.event.GeoRenderEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.geckolib.client.GeoModelTransformer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GeckolibCompat implements ICompatModule {
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> HumanoidModelBaker.registerNewTransformer(new GeoModelTransformer())));
	}
	
	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.registerEvent(GeoModelTransformer::getGeoArmorTexturePath);
		eventBus.addListener(this::geoEntityRenderPreEvent);
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
	
	public void geoEntityRenderPreEvent(GeoRenderEvent.Entity.Pre event) {
		Entity entity = event.getEntity();
		
		if (entity.level() == null) {
			return;
		}
		
		if (entity instanceof LivingEntity livingentity) {
			RenderEngine renderEngine = RenderEngine.getInstance();
			
			if (renderEngine.hasRendererFor(livingentity)) {
				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(livingentity, LivingEntityPatch.class);
				LocalPlayerPatch playerpatch = null;
				float originalYRot = 0.0F;
				
				if ((event.getPartialTick() == 0.0F || event.getPartialTick() == 1.0F) && entitypatch instanceof LocalPlayerPatch localPlayerPatch) {
					playerpatch = localPlayerPatch;
					originalYRot = playerpatch.getModelYRot();
					playerpatch.setModelYRotInGui(livingentity.getYRot());
					event.getPoseStack().translate(0, 0.1D, 0);
				}
				
				if (entitypatch != null && entitypatch.overrideRender()) {
					event.setCanceled(true);
					renderEngine.renderEntityArmatureModel(livingentity, entitypatch, event.getRenderer(), event.getBufferSource(), event.getPoseStack(), event.getPackedLight(), event.getPartialTick());
				}
				
				if (playerpatch != null) {
					playerpatch.disableModelYRotInGui(originalYRot);
				}
			}
		}
	}
}
