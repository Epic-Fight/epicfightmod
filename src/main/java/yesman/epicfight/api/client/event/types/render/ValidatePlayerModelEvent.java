package yesman.epicfight.api.client.event.types.render;

import yesman.epicfight.api.event.Event;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

public class ValidatePlayerModelEvent extends Event {
    private final AbstractClientPlayerPatch<?> playerPatch;
    private final boolean shouldRenderOriginal;
    private boolean shouldRender;

    public ValidatePlayerModelEvent(AbstractClientPlayerPatch<?> playerPatch, boolean shouldRenderOriginal) {
        this.playerPatch = playerPatch;
        this.shouldRenderOriginal = shouldRenderOriginal;
        this.shouldRender = shouldRenderOriginal;
    }

    public boolean getShouldRenderOriginal() {
        return this.shouldRenderOriginal;
    }

    public boolean getShouldRender() {
        return this.shouldRender;
    }

    public void setShouldRender(boolean shouldRender) {
        this.shouldRender = shouldRender;
    }

    public AbstractClientPlayerPatch<?> getPlayerPatch() {
        return this.playerPatch;
    }
}