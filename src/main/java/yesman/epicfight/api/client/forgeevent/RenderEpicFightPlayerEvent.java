package yesman.epicfight.api.client.forgeevent;

import net.minecraftforge.eventbus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class RenderEpicFightPlayerEvent extends Event {
	private final PlayerPatch<?> playerpatch;
	private final boolean shouldRenderOriginal;
	private boolean shouldRender;
	
	public RenderEpicFightPlayerEvent(PlayerPatch<?> playerpatch, boolean shouldRenderOriginal) {
		this.playerpatch = playerpatch;
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
	
	public PlayerPatch<?> getPlayerPatch() {
		return this.playerpatch;
	}
}