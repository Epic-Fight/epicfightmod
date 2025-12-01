package yesman.epicfight.api.event;

/**
 * An interface for event instances that represents
 * {@link CancelableEventHook}
 */
public interface CancelableEvent {
	/**
	 * Returns whether the event is cancelled
	 */
	boolean hasCanceled();
	
	/**
	 * Cancel the event
	 */
	void cancel();
}
