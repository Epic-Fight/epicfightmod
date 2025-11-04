package yesman.epicfight.compat.playeranimator;

import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.client.neoevent.RenderEpicFightPlayerEvent;
import yesman.epicfight.compat.ICompatModule;

public class PlayerAnimatorCompat implements ICompatModule {
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
        AnimationApplier emote = ((IAnimatedPlayer) event.getPlayerPatch().getOriginal()).playerAnimator_getAnimation();
        if (emote.isActive()) event.setShouldRender(false);
    }
}
