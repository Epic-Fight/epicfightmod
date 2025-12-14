package yesman.epicfight.world.entity.eventlistener;

import java.util.UUID;
import java.util.function.Consumer;

public class EventTrigger<T extends DetachablePlayerEvent<?>> implements Comparable<EventTrigger<?>> {
	private final UUID uuid;
	private final Consumer<T> function;
	private final int priority;
	
	public EventTrigger(UUID uuid, Consumer<T> function, int priority) {
		this.uuid = uuid;
		this.function = function;
		this.priority = priority;
	}
	
	public boolean is(UUID uuid) {
		return this.uuid.equals(uuid);
	}
	
	public void trigger(T args) {
		this.function.accept(args);
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	@Override
	public int compareTo(EventTrigger<?> o) {
		if (this.uuid == o.uuid) {
			return 0;
		} else {
			return this.priority > o.priority ? 1 : -1;
		}
	}
	
	public static <T extends DetachablePlayerEvent<?>> EventTrigger<T> makeEvent(UUID uuid, Consumer<T> function, int priority) {
		return new EventTrigger<T>(uuid, function, priority);
	}
}