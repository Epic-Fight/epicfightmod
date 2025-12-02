package yesman.epicfight.api.event;

import org.jetbrains.annotations.ApiStatus;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.subscriptions.ContextAwareEventSubscription;

/**
 * When {@link EventHook} is fired, the typed event instance should be created. Refer to
 * {@link EpicFightCameraAPI#onItemUseEvent} to see the usage
 */
public abstract class Event {
	/**
	 * Holds information about whose called the event by hook so far,
	 * and who canceled the event
	 * <p>
	 * the name of subscribers are specified as parameter in {@link EventHook#registerEvent} and
	 * {@link CancelableEventHook#registerCancelableEvent} and {@link CancelableEventHook#registerContextAwareEvent}
	 */
	private EventContext eventContext;
	
	/**
	 * Returns whether the event hook canceled
	 * This method requires the class to inherit {@link CancelableEvent} to be used property,
	 * or it always returns false
	 */
	public boolean hasCanceled() {
		if (this instanceof CancelableEvent) {
			return this.eventContext.isCanceled();
		}
		
		return false;
	}
	
	/**
	 * Cancels the event hook
	 * This method requires the class to inherit {@link CancelableEvent} to be used
	 */
	public void cancel() {
		if (!(this instanceof CancelableEvent)) {
			throw new IllegalStateException("Unable to cancel a non cancelable hook");
		}
		
		this.eventContext.onCanceled();
	}
	
	/**
	 * Initialize {@link EventContext}, which is used by {@link ContextAwareEventSubscription}
	 * only called by {@link EventHook#post}
	 */
	@ApiStatus.Internal
	public EventContext initEventContext() {
		this.eventContext = new EventContext();
		return this.eventContext;
	}
}
