package yesman.epicfight.network.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.api.utils.ByteBufCodecsExtends;
import yesman.epicfight.network.ManagedCustomPacketPayload;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public abstract class SPModifyPlayerData implements ManagedCustomPacketPayload {
	public static SPModifyPlayerData.SetPlayerYRot setPlayerYRot(int entityId, float yaw) {
		return new SPModifyPlayerData.SetPlayerYRot(entityId, yaw);
	}
	
	public static SPModifyPlayerData.DisablePlayerYRot disablePlayerYRot(int entityId) {
		return new SPModifyPlayerData.DisablePlayerYRot(entityId);
	}
	
	public static SPModifyPlayerData.SetLastAttackResult setLastAttackResult(int entityId, boolean lastAttackSuccess) {
		return new SPModifyPlayerData.SetLastAttackResult(entityId, lastAttackSuccess);
	}
	
	public static SPModifyPlayerData.SetPlayerMode setPlayerMode(int entityId, PlayerPatch.PlayerMode mode) {
		return new SPModifyPlayerData.SetPlayerMode(entityId, mode);
	}
	
	public static SPModifyPlayerData.SetGrapplingTarget setGrapplingTarget(int entityId, Entity grapplingTarget) {
		return new SPModifyPlayerData.SetGrapplingTarget(entityId, grapplingTarget == null ? -1 : grapplingTarget.getId());
	}
	
	public static class SetPlayerYRot extends SPModifyPlayerData {
		public static final StreamCodec<ByteBuf, SPModifyPlayerData.SetPlayerYRot> STREAM_CODEC =
			StreamCodec.composite(
				ByteBufCodecs.INT,
				SPModifyPlayerData::entityId,
				ByteBufCodecs.FLOAT,
				SetPlayerYRot::yRot,
				SetPlayerYRot::new
		    );
		
		private final float yRot;
		
		public SetPlayerYRot(int entityId, float yRot) {
			super(entityId);
			
			this.yRot = yRot;
		}
		
		public float yRot() {
			return this.yRot;
		}
	}
	
	public static class DisablePlayerYRot extends SPModifyPlayerData {
		public static final StreamCodec<ByteBuf, SPModifyPlayerData.DisablePlayerYRot> STREAM_CODEC =
			StreamCodec.composite(
				ByteBufCodecs.INT,
				SPModifyPlayerData::entityId,
				DisablePlayerYRot::new
		    );
		
		public DisablePlayerYRot(int entityId) {
			super(entityId);
		}
	}
	
	public static class SetLastAttackResult extends SPModifyPlayerData {
		public static final StreamCodec<ByteBuf, SPModifyPlayerData.SetLastAttackResult> STREAM_CODEC =
			StreamCodec.composite(
				ByteBufCodecs.INT,
				SetLastAttackResult::entityId,
				ByteBufCodecs.BOOL,
				SetLastAttackResult::lastAttackSuccess,
				SetLastAttackResult::new
		    );
		
		private final boolean lastAttackSuccess;
		
		public SetLastAttackResult(int entityId, boolean lastAttackSuccess) {
			super(entityId);
			
			this.lastAttackSuccess = lastAttackSuccess;
		}
		
		public boolean lastAttackSuccess() {
			return this.lastAttackSuccess;
		}
	}
	
	public static class SetPlayerMode extends SPModifyPlayerData {
		public static final StreamCodec<ByteBuf, SPModifyPlayerData.SetPlayerMode> STREAM_CODEC =
			StreamCodec.composite(
				ByteBufCodecs.INT,
				SetPlayerMode::entityId,
				ByteBufCodecsExtends.enumCodec(PlayerPatch.PlayerMode.class),
				SetPlayerMode::mode,
				SetPlayerMode::new
		    );
		
		private final PlayerPatch.PlayerMode mode;
		
		public SetPlayerMode(int entityId, PlayerPatch.PlayerMode mode) {
			super(entityId);
			
			this.mode = mode;
		}
		
		public PlayerPatch.PlayerMode mode() {
			return this.mode;
		}
	}
	
	public static class SetGrapplingTarget extends SPModifyPlayerData {
		public static final StreamCodec<ByteBuf, SPModifyPlayerData.SetGrapplingTarget> STREAM_CODEC =
			StreamCodec.composite(
				ByteBufCodecs.INT,
				SetGrapplingTarget::entityId,
				ByteBufCodecs.INT,
				SetGrapplingTarget::grapplingTargetEntityId,
				SetGrapplingTarget::new
		    );
		
		private final int grapplingTargetEntityId;
		
		public SetGrapplingTarget(int entityId, int grapplingTargetEntityId) {
			super(entityId);
			
			this.grapplingTargetEntityId = grapplingTargetEntityId;
		}
		
		public int grapplingTargetEntityId() {
			return this.grapplingTargetEntityId;
		}
	}
	
	private final int entityId;
	
	private SPModifyPlayerData(int entityId) {
		this.entityId = entityId;
	}
	
	public int entityId() {
		return this.entityId;
	}
}