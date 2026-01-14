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
        AnimationApplier playerAnimatorAnimation = ((IAnimatedPlayer) event.getPlayerPatch().getOriginal()).playerAnimator_getAnimation();
        
        if (!event.getPlayerPatch().getClientAnimator().getPlayerFor(null).getAnimation().get().isMainFrameAnimation() && // The case when playing EF animation that controls player location
        		playerAnimatorAnimation.isActive()
        ) {
        	event.setShouldRender(false);
        }
    }
}
