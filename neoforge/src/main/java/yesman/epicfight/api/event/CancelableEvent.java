package yesman.epicfight.api.event;

/// An interface for [Event] that can be canceled by subscribers
/// [CancelableEventHook]
public interface CancelableEvent {
	/// Returns whether the event is cancelled
	default boolean isCanceled() {
        return ((Event)this).getEventContext().isCanceled();
    }

    /// Cancels the event
    default void cancel() {
        ((Event)this).getEventContext().onCanceled();
    }
}
