package yesman.epicfight.api.event.subscription;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.event.EventContext;

/// A subscriptionType type that developers can inspect event, cancel history by [EventContext]
/// This event subscriptionType type called even after the event is canceled
@FunctionalInterface
public interface ContextAwareEventSubscription<T extends Event & CancelableEvent> extends EventSubscriptionType<T> {
	void fire(T event, EventContext eventContext);
}
