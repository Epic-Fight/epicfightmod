package yesman.epicfight.api.event;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A event context that holds who called the event so far
 * and who canceled the event
 * <p>
 * Developers can decide whether fire the event or not
 * depending on the call and cancel history
 */
public class EventContext {
	private List<String> calledBy = new ArrayList<> ();
	private List<String> canceledBy = new ArrayList<> ();
	
	/**
	 * A scoped variable for subscriber's name
	 */
	@Nullable
	private String currentSubscriber;
	
	/**
	 * Returns if the event is cancel by anyone
	 */
	public boolean isCanceled() {
		return !this.canceledBy.isEmpty();
	}
	
	/**
	 * Returns if the event is cancel by a specific subscriber
	 */
	public boolean isCanceledBy(String name) {
		return this.canceledBy.contains(name);
	}
	
	/**
	 * Returns if the event is called by a specific subscriber
	 */
	public boolean hasCalledBy(String name) {
		return this.calledBy.contains(name);
	}
	
	/**
	 * Only used by {@link EventHook#post} and {@link CancelableEventHook#post}
	 */
	@ApiStatus.Internal
	public void onCalled() {
		this.calledBy.add(this.currentSubscriber);
	}
	
	/**
	 * Only used by {@link Event#cancel}
	 */
	@ApiStatus.Internal
	public void onCanceled() {
		this.canceledBy.add(this.currentSubscriber);
	}
	
	/**
	 * Only used by {@link EventHook#post} and {@link CancelableEventHook#post}
	 */
	@ApiStatus.Internal
	public void subscriptionStart(String name) {
		this.currentSubscriber = name;
	}
	
	/**
	 * Only used by {@link EventHook#post} and {@link CancelableEventHook#post}
	 */
	@ApiStatus.Internal
	public void subscriptionEnd() {
		this.currentSubscriber = null;
	}
}
