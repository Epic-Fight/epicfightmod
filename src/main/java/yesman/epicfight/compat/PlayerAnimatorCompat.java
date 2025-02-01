package yesman.epicfight.compat;

import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.api.client.forgeevent.RenderEpicFightPlayerEvent;

public class PlayerAnimatorCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {}

    @Override
    public void onForgeEventBus(IEventBus eventBus) {}

    @Override
    public void onModEventBusClient(IEventBus eventBus) {}

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onForgeEventBusClient(IEventBus eventBus) {
        eventBus.addListener(this::renderEvent);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderEvent(RenderEpicFightPlayerEvent event) {
        AnimationApplier emote = ((IAnimatedPlayer) event.getPlayerPatch().getPlayer()).playerAnimator_getAnimation();
        if (emote.isActive()) event.setShouldRender(false);
    }
}
