package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import yesman.epicfight.client.renderer.shader.compute.backend.Sync;

import static org.lwjgl.opengl.GL46.*;

public class MappedBuffer extends MutableBuffer implements IClientBuffer {

	private final Sync sync = new Sync();
	protected long address;
	protected long position;
	protected long current;

	public MappedBuffer(long initialSize) {
        super(initialSize, GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT);
		this.address	= map();
		this.position	= 0L;
		this.current	= 0L;
	}

	@Override
	public long reserve(long bytes, boolean occupied) {
		if (bytes <= 0) {
			return address + position;
		}

		var oldPosition = this.position;
		var newPosition = oldPosition + bytes;

		if (occupied) {
			this.current = oldPosition;
			this.position = newPosition;
		}

		if (newPosition <= size) {
			return address + oldPosition;
		}

        this.resize(newPosition);
		return address + oldPosition;
	}

	@Override
	public long reserve(long bytes) {
		return reserve(bytes, true);
	}

	@Override
	public long addressAt(long position) {
		return this.address + position;
	}

	@Override
	public void beforeExpand() {
        this.unmap();
	}

	@Override
	public void afterExpand() {
        this.address = map();
	}

	public void reset() {
        this.position = 0;
	}

	public long getTail() {
		return this.position;
	}

	public long map() {
		return this.map(GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
	}

	public boolean isSyncd() {
		if (!sync.isSyncSet()) {
			return true;
		}

		if (!sync.isSyncSignaled()) {
			return false;
		}
		sync.deleteSync	();
		sync.resetSync	();
		return true;
	}

	public void waitSync() {
		if (!sync.isSyncSet()) {
			return;
		}

		if (!sync.isSyncSignaled()) {
			sync.waitSync();
		}

		sync.deleteSync	();
		sync.resetSync	();
	}

	public void setSync(){
		sync.setSync();;
	}
}
