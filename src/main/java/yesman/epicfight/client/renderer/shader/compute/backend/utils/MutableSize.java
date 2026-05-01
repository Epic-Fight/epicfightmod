package yesman.epicfight.client.renderer.shader.compute.backend.utils;

public class MutableSize {
	protected boolean resized;
	protected long size;

	public MutableSize(long initialSize) {
		this.resized = false;
		this.size = initialSize;
	}

	public void expand(long bytes) {
		if (bytes <= 0) {
			return;
		}

		this.beforeExpand();
        this.onExpand(bytes);
        this.doExpand(this.size, bytes);

        this.resized = true;
        this.size += bytes;

        this.afterExpand();
	}

	public void onExpand(long bytes) {
	}

	public void doExpand(long size, long bytes) {
	}

	public void beforeExpand() {
	}

	public void afterExpand() {
	}

	public void resize(long atLeast) {
        this.resizeTo(Long.highestOneBit(atLeast) << 1);
	}

	public void resizeTo(long newBufferSize) {
        this.expand(newBufferSize - this.size);
	}

	public void resetResized() {
        this.resized = false;
	}

	public long getSize() {
        return this.size;
    }
}
