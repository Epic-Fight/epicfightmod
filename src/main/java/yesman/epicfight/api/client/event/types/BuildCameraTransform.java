package yesman.epicfight.api.client.event.types;

import net.minecraft.client.Camera;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.event.CancelableEvent;

public abstract class BuildCameraTransform extends CameraAPIEvent {
	private final Camera camera;
	private final float partialTick;
	
	public BuildCameraTransform(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
		super(cameraApi);
		this.camera = camera;
		this.partialTick = partialTick;
	}
	
	public Camera getCamera() {
		return this.camera;
	}
	
	public float getPartialTick() {
		return this.partialTick;
	}
	
	public static final class Pre extends BuildCameraTransform implements CancelableEvent {
		public Pre(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
			super(cameraApi, camera, partialTick);
		}
	}
	
	public static final class Post extends BuildCameraTransform {
		public Post(EpicFightCameraAPI cameraApi, Camera camera, float partialTick) {
			super(cameraApi, camera, partialTick);
		}
	}
}
