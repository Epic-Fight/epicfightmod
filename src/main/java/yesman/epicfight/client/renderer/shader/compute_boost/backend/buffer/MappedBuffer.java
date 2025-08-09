package yesman.epicfight.client.renderer.shader.compute_boost.backend.buffer;

import lombok.Getter;

import static org.lwjgl.opengl.GL46.*;

@Getter
public class MappedBuffer extends MutableBuffer implements IClientBuffer {

	protected long address;
	protected long position;

	public MappedBuffer(long initialSize) {
		super(initialSize,	GL_MAP_PERSISTENT_BIT
				| 			GL_MAP_WRITE_BIT
				|			GL_MAP_COHERENT_BIT);

		this.address	= map();
		this.position	= 0L;
	}

	@Override
	public long reserve(long bytes, boolean occupied) {
		if (bytes <= 0) {
			return address + position;
		}

		var oldPosition = this.position;
		var newPosition = oldPosition + bytes;

		if (occupied) {
			this.position = newPosition;
		}

		if (newPosition <= size) {
			return address + oldPosition;
		}

		resize(newPosition);
		return address + oldPosition;
	}

	@Override
	public long reserve(long bytes) {
		return reserve(bytes, true);
	}

	@Override
	public long addressAt(long position) {
		return address + position;
	}

	@Override
	public void beforeExpand() {
		unmap();
	}

	@Override
	public void afterExpand() {
		address = map();
	}

	public void reset() {
		position = 0;
	}

	public long map() {
		return map(	GL_MAP_WRITE_BIT
				|	GL_MAP_PERSISTENT_BIT
				|	GL_MAP_COHERENT_BIT);
	}
}
