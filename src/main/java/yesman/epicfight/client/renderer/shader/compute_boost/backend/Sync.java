package yesman.epicfight.client.renderer.shader.compute_boost.backend;

import static org.lwjgl.opengl.GL46.*;

public class Sync {

	private long syncHandle;

	public Sync() {
		this.syncHandle = -1;
	}

	public boolean isSyncSet() {
		return syncHandle != -1;
	}

	public boolean isSyncSignaled() {
		return glGetSynci(syncHandle, GL_SYNC_STATUS, null) == GL_SIGNALED;
	}

	public void waitSync() {
		glClientWaitSync(syncHandle, GL_SYNC_FLUSH_COMMANDS_BIT, Long.MAX_VALUE);
	}

	public void setSync() {
		syncHandle = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
	}

	public void deleteSync() {
		glDeleteSync(syncHandle);
	}

	public void resetSync() {
		syncHandle = -1;
	}
}
