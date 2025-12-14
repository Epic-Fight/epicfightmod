package yesman.epicfight.api.event;

import java.util.TreeMap;

import yesman.epicfight.api.client.event.EpicFightClientHooks;
import yesman.epicfight.api.event.subscriptions.DefaultEventSubscription;

/**
 * This class handles event subscription in Epic Fight API, inspired by
 * Forge/NeoForge's Event, and Fabric's Callback/Event
 * <p>
 * Note this object isn't created each time a event is occur. it only
 * defines event type and its subscriptions
 * <p>
 * To create custom evens, follow these codebase: {@link EpicFightHooks}
 * and {@link EpicFightClientHooks} for client-side only events
 */
public class EventHook<T extends Event> {
	/**
	 * Treemap to order subscribers in descending order
	 */
	final TreeMap<Integer, EventListener<T>> subscriptions = new TreeMap<> ((i1, i2) -> Integer.compare(i2, i1));
	
	/**
	 * Executes the subscribers' task by their priorities
	 * @return whether the event is canceled. Always returns false since the event is not cancelable
	 */
	public boolean post(T eventInstance) {
		EventContext eventContext = new EventContext();
		
		for (EventListener<T> subscriber : this.subscriptions.values()) {
			eventContext.subscriptionStart(subscriber.name());
			
			if (subscriber.subscription() instanceof DefaultEventSubscription<T> passiveSubscription) {
				passiveSubscription.fire(eventInstance);
				eventContext.onCalled();
			}
		}
		
		eventContext.subscriptionEnd();
		
		return false;
	}
	
	/**
	 * Register an event with default name and priority
	 */
	public void registerEvent(DefaultEventSubscription<T> subscription) {
		this.registerEvent(subscription, getDefaultSubscriberName(), 0);
	}
	
	/**
	 * Register an event with default name
	 * @param priority determines the order of the event in descending order
	 */
	public void registerEvent(DefaultEventSubscription<T> subscription, int priority) {
		this.registerEvent(subscription, getDefaultSubscriberName(), priority);
	}
	
	/**
	 * Register an event with default priority
	 * @param name you can specify the subscriber name to be referenced by other events, it will be stored
	 * 			   at {@link EventContext}
	 */
	public void registerEvent(DefaultEventSubscription<T> subscription, String name) {
		this.registerEvent(subscription, name, 0);
	}
	
	/**
	 * Register an event with full parameters
	 */
	public void registerEvent(DefaultEventSubscription<T> subscription, String name, int priority) {
		this.subscriptions.put(priority, new EventListener<>(name, subscription));
	}
	
	/**
	 * Returns a class name who called register_Event methods
	 */
	protected static String getDefaultSubscriberName() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        return caller.getClassName();
	}
	
	/**
	 * Defines a default event type
	 */
	public static <T extends Event> EventHook<T> createEventHook() {
		return new EventHook<> ();
	}
}
