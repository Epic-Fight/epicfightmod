package yesman.epicfight.world.entity.eventlistener;

import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ItemUseEndEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private final LivingEntityUseItemEvent.Stop forgeEvent;
	
	public ItemUseEndEvent(ServerPlayerPatch playerpatch, LivingEntityUseItemEvent.Stop forgeEvent) {
		super(playerpatch, true);
		
		this.forgeEvent = forgeEvent;
	}
	
	public LivingEntityUseItemEvent.Stop getForgeEvent() {
		return this.forgeEvent;
	}
}