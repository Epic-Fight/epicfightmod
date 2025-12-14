package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscriptions.ContextAwareEventSubscription;
import yesman.epicfight.api.event.subscriptions.DefaultEventSubscription;

/**
 * Event definition for {@link CancelableEvent}
 */
public class CancelableEventHook<T extends Event & CancelableEvent> extends EventHook<T> {
	/**
	 * Executes the subscribers' task by their priorities
	 * {@link ContextAwareEventSubscription} will ignore the canceled state and fired always, developers must
	 * validate whether fire the event or not by provided {@link EventContext}
	 * 
	 * @return whether the event is canceled
	 */
	@Override
	public boolean post(T event) {
		EventContext eventContext = event.getEventContext();
		
		for (EventListener<T> subscriber : this.subscriptions.values()) {
			eventContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof DefaultEventSubscription<T> passiveSubscription) {
				if (!event.hasCanceled()) {
					passiveSubscription.fire(event);
					eventContext.onCalled();
				}
			} else if (subscriber.subscription() instanceof ContextAwareEventSubscription<T> contextAwareSubscription) {
				contextAwareSubscription.fire(event, eventContext);
				eventContext.onCalled();
			}
		}
		
		eventContext.subscriptionEnd();
		
		return event.hasCanceled();
	}
	
	/**
	 * Registers an event with default name and priority
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription) {
		this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Registers an event with default name
	 * @param priority determines the order of the event in descending order
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, int priority) {
		this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Registers an event with default priority
	 * @param name you can specify the subscriber name to be referenced by other events, it will be stored
	 * 		  at {@link EventContext}
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name) {
		this.registerContextAwareEvent(subscription, name, 0);
	}
	
	/**
	 * Registers an event with full parameters
	 */
	public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new EventListener<>(name, subscription));
	}
	
	/**
	 * Defines a cancelable event type
	 */
	public static <T extends Event & CancelableEvent> CancelableEventHook<T> createCancelableEventHook() {
		return new CancelableEventHook<> ();
	}
}
