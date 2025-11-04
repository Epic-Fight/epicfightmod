package yesman.epicfight.compat.mcreator;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.client.neoevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.compat.ICompatModule;

public class MCreatorPlayerAnimationsCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {}

    @Override
    public void onGameEventBus(IEventBus eventBus) {}

    @Override
    public void onModEventBusClient(IEventBus eventBus) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onGameEventBusClient(IEventBus eventBus) {
        eventBus.addListener(this::renderEvent);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderEvent(RenderEpicFightPlayerEvent event) {
        String animation = event.getPlayerPatch().getOriginal().getPersistentData().getString("PlayerCurrentAnimation");
        if (!animation.isEmpty()) event.setShouldRender(false);
    }
}
