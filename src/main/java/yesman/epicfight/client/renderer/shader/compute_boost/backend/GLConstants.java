package yesman.epicfight.client.renderer.shader.compute_boost.backend;

import static org.lwjgl.opengl.GL46.*;

public class GLConstants {

	public static final int MAX_SHADER_STORAGE_BLOCK_SIZE	= glGetInteger(GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
	public static final int MAX_COMPUTE_WORK_GROUP_COUNT_X	= glGetIntegeri(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0);
	public static final int MAX_COMPUTE_WORK_GROUP_COUNT_Y	= glGetIntegeri(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 1);
	public static final int MAX_COMPUTE_WORK_GROUP_COUNT_Z	= glGetIntegeri(GL_MAX_COMPUTE_WORK_GROUP_COUNT, 2);
}
