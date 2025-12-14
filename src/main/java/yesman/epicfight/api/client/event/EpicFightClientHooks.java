package yesman.epicfight.api.client.event;

import yesman.epicfight.api.client.event.types.ActivateTPSCamera;
import yesman.epicfight.api.client.event.types.BuildCameraTransform;
import yesman.epicfight.api.client.event.types.CoupleTPSCamera;
import yesman.epicfight.api.client.event.types.ItemUsedInDecoupledCamera;
import yesman.epicfight.api.client.event.types.LockOnEvent;
import yesman.epicfight.api.event.CancelableEventHook;
import yesman.epicfight.api.event.EventHook;

public final class EpicFightClientHooks {
	
    /// We will eventually put all epic fight neoforge events here to decouple the event handling system originally
    /// conducted by mod-loaders (Forge, Neoforge, Fabric) in later Minecraft ports
    /// There are a bunch of event definitions under {@link yesman.epicfight.api.forgeevent} package. We
    /// plan to define each class as static fields of {@link yesman.epicfight.api.event.Event} in future API model.
    /// For now, we only have event hooks for {@link EpicFightCameraAPI} to demonstrate our API behavior.
	
	// Camera Events
	public static final class Camera {
		public static final CancelableEventHook<BuildCameraTransform.Pre> BUILD_TRANSFORM_PRE = CancelableEventHook.createCancelableEventHook();
		public static final EventHook<BuildCameraTransform.Post> BUILD_TRANSFORM_POST = EventHook.createEventHook();
		public static final EventHook<ItemUsedInDecoupledCamera> ITEM_USED_WHEN_DECOUPLED = EventHook.createEventHook();
		public static final EventHook<ActivateTPSCamera> ACTIVATE_TPS_CAMERA = CancelableEventHook.createCancelableEventHook();
		public static final EventHook<CoupleTPSCamera> COUPLE_CAMERA = EventHook.createEventHook();
		public static final EventHook<LockOnEvent.Start> LOCK_ON_START = CancelableEventHook.createCancelableEventHook();
	    public static final EventHook<LockOnEvent.Tick> LOCK_ON_TICK = EventHook.createEventHook();
	    public static final EventHook<LockOnEvent.Release> LOCK_ON_RELEASED = CancelableEventHook.createCancelableEventHook();
	}
	
	private EpicFightClientHooks() {}
}