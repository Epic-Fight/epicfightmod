package yesman.epicfight.compat.mca;

import net.conczin.mca.registry.EntitiesMCA;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.mca.entity_patch.MCAVillagerEntityPatch;
import yesman.epicfight.compat.mca.renderer.PMCAVillagerRenderer;
import yesman.epicfight.gameasset.Armatures;

public class MCARebornCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {
        eventBus.addListener(FMLCommonSetupEvent.class, (event) -> event.enqueueWork(() -> {
            Armatures.registerEntityTypeArmature(EntitiesMCA.MALE_VILLAGER, Armatures.BIPED);
            Armatures.registerEntityTypeArmature(EntitiesMCA.FEMALE_VILLAGER, Armatures.BIPED);
        }));
        EpicFightEventHooks.Registry.ENTITY_PATCH.registerEvent(entityPatch -> {
            entityPatch.registerEntityPatch(EntitiesMCA.MALE_VILLAGER, MCAVillagerEntityPatch::new);
            entityPatch.registerEntityPatch(EntitiesMCA.FEMALE_VILLAGER, MCAVillagerEntityPatch::new);
        });
    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Registry.ADD_PATCHED_ENTITY.registerEvent(event -> {
            event.addPatchedEntityRenderer(EntitiesMCA.MALE_VILLAGER, entityType -> new PMCAVillagerRenderer(event.getContext(),entityType));
            event.addPatchedEntityRenderer(EntitiesMCA.FEMALE_VILLAGER, entityType -> new PMCAVillagerRenderer(event.getContext(),entityType));
        });
    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
