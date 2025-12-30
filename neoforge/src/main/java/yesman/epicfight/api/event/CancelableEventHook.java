package yesman.epicfight.api.event;

import yesman.epicfight.api.event.subscription.ContextAwareEventSubscription;
import yesman.epicfight.api.event.subscription.DefaultEventSubscription;
import yesman.epicfight.api.utils.side.LogicalSide;

import java.util.stream.Stream;

/// EventHook definition for [CancelableEvent]
public class CancelableEventHook<T extends Event & CancelableEvent> extends EventHook<T> {
    protected CancelableEventHook(LogicalSide logicalSide) {
        super(logicalSide);
    }

    /// Executes the subscribers' task by their priorities
    /// [ContextAwareEventSubscription] will ignore the canceled state and fired always, developers must
    /// validate whether fire the event or not by provided [EventContext]
    @Override
    public T post(T event) {
        EventContext eventContext = event.getEventContext();

        for (EventListener<T> subscriber : this.subscribers.values()) {
            eventContext.subscriptionStart(subscriber.identifier());

            if (subscriber.subscriptionType() instanceof DefaultEventSubscription<T> defaultSubscription) {
                if (!event.isCanceled()) {
                    defaultSubscription.fire(event);
                    eventContext.onCalled();
                }
            } else if (subscriber.subscriptionType() instanceof ContextAwareEventSubscription<T> contextAwareSubscription) {
                contextAwareSubscription.fire(event, eventContext);
                eventContext.onCalled();
            }
        }

        eventContext.subscriptionEnd();

        return event;
    }

    /// Post the event to subscribers including from [EntityEventListener], and execute tasks by their priority in descending order
    @Override
    public T postWithListener(T event, EntityEventListener eventListener) {
        if (!(event instanceof LivingEntityPatchEvent)) {
            throw new IllegalArgumentException("EventHook instance must be a subtype of LivingEntityPatchEvent to be posted with EntityEventListener");
        }

        EventContext eventContext = event.getEventContext();

        Stream.concat(this.subscribers.values().stream(), eventListener.getListenersFor(this))
            .sorted((o1, o2) -> Integer.compare(o2.priority(), o1.priority()))
            .forEach(subscriber -> {
                eventContext.subscriptionStart(subscriber.identifier());

                if (subscriber.subscriptionType() instanceof DefaultEventSubscription<T> defaultSubscription) {
                    if (!event.isCanceled()) {
                        defaultSubscription.fire(event);
                        eventContext.onCalled();
                    }
                } else if (subscriber.subscriptionType() instanceof ContextAwareEventSubscription<T> contextAwareSubscription) {
                    contextAwareSubscription.fire(event, eventContext);
                    eventContext.onCalled();
                }
            });

        eventContext.subscriptionEnd();

        return event;
    }

    /// Registers an event with default identifier and priority
    public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription) {
        this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), 0);
    }

    /// Registers an event with default identifier
    /// @param priority determines the order of the event in descending order
    public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, int priority) {
        this.registerContextAwareEvent(subscription, getDefaultSubscriberName(), priority);
    }

    /// Registers an event with default priority
    /// @param name specify the subscriber identifier to be referenced by other events, it will be stored at {@link EventContext}
    public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name) {
        this.registerContextAwareEvent(subscription, name, 0);
    }

    /// Registers an event with full parameters
    public void registerContextAwareEvent(ContextAwareEventSubscription<T> subscription, String name, int priority) {
        this.subscribers.put(priority, new EventListener<> (name, priority, subscription));
    }

    /// Defines a cancelable event hook
    public static <T extends Event & CancelableEvent> CancelableEventHook<T> createCancelableEventHook() {
        return new CancelableEventHook<>(LogicalSide.BOTH);
    }

    /// Defines a sided cancelable event hook
    public static <T extends Event & CancelableEvent> CancelableEventHook<T> createSidedCancelableEventHook(LogicalSide logicalSide) {
        return new CancelableEventHook<>(logicalSide);
    }
}
