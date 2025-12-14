package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.EventSubscription;

/**
 * An event subscription info
 * <p>
 * name: you can specify the name of subscriber, this will effect {@link EventContext} to inspect who called,
 * and who canceled the event. (default is a class name called {@link EventHook#registerPassiveEvent},
 * {@link CancelableEventHook#registerCancelableEvent}, and {@link CancelableEventHook#registerContextAwareEvent}
 * <p>
 * subscription: a task provided as a lambda expression
 */
public record EventListener<T extends Event> (String name, EventSubscription<T> subscription) {
}
