package yesman.epicfight.compat.playeranimator;

import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.neoforged.bus.api.IEventBus;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.ValidatePlayerModelEvent;
import yesman.epicfight.compat.ICompatModule;

public class PlayerAnimatorCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {}

    @Override
    public void onGameEventBus(IEventBus eventBus) {}

    @Override
    public void onModEventBusClient(IEventBus eventBus) {}

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Render.VALIDATE_PLAYER_MODEL_TO_RENDER.registerEvent(this::renderEvent);
    }

    private void renderEvent(ValidatePlayerModelEvent event) {
        AnimationApplier emote = ((IAnimatedPlayer) event.getPlayerPatch().getOriginal()).playerAnimator_getAnimation();
        if (emote.isActive()) event.setShouldRender(false);
    }
}
