package yesman.epicfight.api.event.subscription;

import yesman.epicfight.api.event.Event;
import yesman.epicfight.api.event.EventContext;

/// [DefaultEventSubscription] for common event firing rule that doesn't fire lower prioritized
/// events if it's canceled by higher event.
///
/// [ContextAwareEventSubscription] for events that still fired if it's canceled by higher
/// prioritized events, with receving a [EventContext] as parameter to decide whether fire
/// or cancel the event by custom logics
public interface EventSubscriptionType<T extends Event> {
}
