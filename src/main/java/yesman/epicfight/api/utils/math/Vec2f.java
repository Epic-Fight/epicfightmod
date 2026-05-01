package yesman.epicfight.api.utils.math;

public class Vec2f {
	public float x;
	public float y;

	public Vec2f() {
		this.x = 0;
		this.y = 0;
	}
	
	public Vec2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vec2f scale(float f) {
		this.x *= f;
		this.y *= f;
		return this;
	}
	
	@Override
	public String toString() {
		return "Vec2f[" + this.x + ", " + this.y + ", " + "]";
	}
}