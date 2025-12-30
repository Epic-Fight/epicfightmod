package yesman.epicfight.api.animation;

public class Keyframe {
	private float timeStamp;
	private final JointTransform transform;

	public Keyframe(float timeStamp, JointTransform trasnform) {
		this.timeStamp = timeStamp;
		this.transform = trasnform;
	}
	
	public Keyframe(Keyframe original) {
		this.transform = JointTransform.empty();
		this.copyFrom(original);
	}
	
	public void copyFrom(Keyframe target) {
		this.timeStamp = target.timeStamp;
		this.transform.copyFrom(target.transform);
	}
	
	public float time() {
		return this.timeStamp;
	}
	
	public void setTime(float time) {
		this.timeStamp = time;
	}
	
	public JointTransform transform() {
		return this.transform;
	}
	
	public String toString() {
		return "Keyframe[Time: " + this.timeStamp + ", " + (this.transform == null ? "null" : this.transform.toString()) + "]";
	}
	
	public static Keyframe empty() {
		return new Keyframe(0.0F, JointTransform.empty());
	}
}