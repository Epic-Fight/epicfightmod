package yesman.epicfight.world.entity.eventlistener;

import net.minecraftforge.event.entity.living.LivingFallEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FallEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private final LivingFallEvent forgeEvent;
	
	public FallEvent(PlayerPatch<?> playerpatch, LivingFallEvent forgeEvent) {
		super(playerpatch, false);
		this.forgeEvent = forgeEvent;
	}
	
	public LivingFallEvent getForgeEvent() {
		return this.forgeEvent;
	}
}