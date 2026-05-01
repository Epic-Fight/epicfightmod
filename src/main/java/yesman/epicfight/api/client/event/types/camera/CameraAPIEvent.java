package yesman.epicfight.api.client.event.types.camera;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.Event;

/// All events fired in [EpicFightCameraAPI] inherit this class
public abstract class CameraAPIEvent extends Event {
    private final EpicFightCameraAPI cameraApi;

    public CameraAPIEvent(EpicFightCameraAPI cameraApi) {
        this.cameraApi = cameraApi;
    }

    /// Developers can use [EpicFightCameraAPI#getInstance]. However, we still
    /// recommend to use this method to get CameraAPI for the maintainability
    public final EpicFightCameraAPI getCameraApi() {
        return this.cameraApi;
    }
}
