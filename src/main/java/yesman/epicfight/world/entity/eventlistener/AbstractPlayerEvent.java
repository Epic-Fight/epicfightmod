package yesman.epicfight.world.entity.eventlistener;

import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class AbstractPlayerEvent<T extends PlayerPatch<?>> implements DetachablePlayerEvent<T> {
	private final T playerpatch;
	private final boolean cancelable;
	private boolean canceled;
	
	public AbstractPlayerEvent(T playerpatch, boolean cancelable) {
		this.playerpatch = playerpatch;
		this.cancelable = cancelable;
	}
	
	@Override
	public T getPlayerPatch() {
		return this.playerpatch;
	}
	
	@Override
	public void setCanceled(boolean canceled) {
		if (!this.cancelable) {
			throw new UnsupportedOperationException(String.format("Event %s is not cancelable.", this));
		}
		
		this.canceled = canceled;
	}
	
	@Override
	public boolean isCanceled() {
		return this.cancelable && this.canceled;
	}
	
	@Override
	public String toString() {
		return this.getClass().toString();
	}
}