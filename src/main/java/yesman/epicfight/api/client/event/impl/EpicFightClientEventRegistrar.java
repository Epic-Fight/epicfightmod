package yesman.epicfight.api.client.event.impl;

import net.minecraft.client.player.LocalPlayer;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.client.events.engine.ControlEngine;

public final class EpicFightClientEventRegistrar {
    public static void registerEvents() {
        EpicFightEventHooks.Animation.START_ACTION.registerEvent(event -> {
            if (event.getEntityPatch().getOriginal() instanceof LocalPlayer) {
                ControlEngine.getInstance().unlockHotkeys();
            }
        });
    }

    private EpicFightClientEventRegistrar() {}
}
