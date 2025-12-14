package yesman.epicfight.network.common;

import net.minecraft.world.phys.Vec3;

public class SyncAnimationPositionPacket {
	protected final int entityId;
	protected final float elapsedTime;
	protected final Vec3 position;
	protected final int lerpSteps;
	
	public SyncAnimationPositionPacket(int entityId, float elapsedTime, Vec3 position, int lerpSteps) {
		this.entityId = entityId;
		this.elapsedTime = elapsedTime;
		this.position = position;
		this.lerpSteps = lerpSteps;
	}
}
