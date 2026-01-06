package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import yesman.epicfight.client.renderer.shader.compute.backend.utils.MutableSize;

import java.nio.ByteBuffer;

public class MutableBuffer extends MutableSize implements IServerBuffer {
	private final int bits;
	protected ImmutableBuffer glBuffer;

	public MutableBuffer(long initialSize, int bits) {
		super(initialSize);

		this.bits = bits;
		this.glBuffer = new ImmutableBuffer(this.size, bits);
	}

	@Override
	public void doExpand(long size, long bytes) {
		var newSize	= size + bytes;
		var newBuffer = new ImmutableBuffer(newSize, this.bits);

		this.glBuffer.copyTo(newBuffer, size);
        this.glBuffer.delete();
        this.glBuffer = newBuffer;
	}

	public long map(int flags) {
		return this.glBuffer.map(this.size, flags);
	}

	public void unmap() {
        this.glBuffer.unmap();
	}

	public void copyTo(IServerBuffer buffer) {
        this.glBuffer.copyTo(buffer, this.size);
	}

	@Override
	public int getBufferHandle() {
		return this.glBuffer.getBufferHandle();
	}

	@Override
	public void delete() {
        this.glBuffer.delete();
	}

	@Override
	public void bind(int target) {
        this.glBuffer.bind(target);
	}

	@Override
	public void data(ByteBuffer data) {
        this.glBuffer.data(data);
	}

	@Override
	public void bindBase(int target, int index) {
        this.glBuffer.bindBase(target, index);
	}

	@Override
	public void bindRange(int target, int index, long offset, long size) {
        this.glBuffer.bindRange(target, index, offset, size);
	}
}
