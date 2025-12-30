package yesman.epicfight.api.client.event.types.camera;

import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.CancelableEvent;

public class ActivateTPSCamera extends CameraAPIEvent implements CancelableEvent {
    public ActivateTPSCamera(EpicFightCameraAPI cameraApi) {
        super(cameraApi);
    }
}
