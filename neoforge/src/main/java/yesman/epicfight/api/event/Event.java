package yesman.epicfight.api.event;

import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.subscription.ContextAwareEventSubscription;

/// When [EventHook] is fired, the typed event instance should be created. Refer to
/// [EpicFightCameraAPI#onItemUseEvent] to see the usage
public abstract class Event {
    /// Holds information about who has subscribed, and who canceled the event
    /// the identifier of subscribers can be specified as parameter in [EventHook#registerEvent],
    /// [CancelableEventHook#registerCancelableEvent], and [CancelableEventHook#registerContextAwareEvent]
    private final EventContext eventContext = new EventContext();
	
	/// Requires the event to inherit [CancelableEvent] to be used property, or it always returns false
	public boolean isCanceled() {
		if (this instanceof CancelableEvent) {
			return this.eventContext.isCanceled();
		}
		
		return false;
	}
	
	/// Cancels the event hook
	/// This method requires the class to inherit [CancelableEvent] to be used
	public void cancel() {
		if (!(this instanceof CancelableEvent)) {
			throw new IllegalStateException("Unable to cancel a non cancelable hook");
		}
		
		this.eventContext.onCanceled();
	}
	
	/// Returns [EventContext], which is used by [ContextAwareEventSubscription]
    ///
    /// This should only be called by [EventHook#post] and developers should use
    /// the event context provided as a parameter of [ContextAwareEventSubscription]
	@ApiStatus.Internal
	public EventContext getEventContext() {
		return this.eventContext;
	}
}
