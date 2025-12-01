package yesman.epicfight.api.event.subscriptions;

import yesman.epicfight.api.event.Event;

/**
 * A default event subscription type
 * If you want to fire the event with custom validation, {@link ContextAwareEventSubscription}
 */
@FunctionalInterface
public interface DefaultEventSubscription<T extends Event> extends EventSubscription<T> {
	void fire(T event);
}
