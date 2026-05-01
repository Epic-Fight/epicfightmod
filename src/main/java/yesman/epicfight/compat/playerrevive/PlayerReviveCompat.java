package yesman.epicfight.compat.playerrevive;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import team.creative.playerrevive.server.PlayerReviveServer;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.IdentifierProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public class PlayerReviveCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBus(IEventBus eventBus) {
        eventBus.<EntityJoinLevelEvent>addListener(event -> {
            if (event.getEntity() instanceof Player player) {
                EpicFightCapabilities.getPlayerPatchAsOptional(player).ifPresent(playerPatch -> {
                    playerPatch.getEventListener().registerEvent(EpicFightEventHooks.Player.CAST_SKILL, skillCastEvent -> {
                        if (PlayerReviveServer.isBleeding(player)) {
                            skillCastEvent.cancel();
                        }
                    },
                    IdentifierProvider.PERMANENT_LISTENER);
                });
            }
        });
    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {

    }

    @Override
    public void onGameEventBusClient(IEventBus eventBus) {

    }
}
