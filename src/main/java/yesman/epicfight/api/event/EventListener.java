package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscription.EventSubscriptionType;

/// An event subscription info
/// identifier: you can specify the identifier of subscriber, this will affect [EventContext] to inspect who called,
/// and who canceled the event. (default is a class identifier called [EventHook#registerEvent],
/// [CancelableEventHook#registerCancelableEvent], and [CancelableEventHook#registerContextAwareEvent]
///
/// priority: a descending order priority among subscribers subscribing to the same event
///
/// subscriptionType: a task provided as a lambda expression
public record EventListener<T extends Event> (String identifier, int priority, EventSubscriptionType<T> subscriptionType) {
}
