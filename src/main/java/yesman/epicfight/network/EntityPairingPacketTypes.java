package yesman.epicfight.network;

public enum EntityPairingPacketTypes implements EntityPairingPacketType {
	ADRENALINE_ACTIVATED,
	BONEBREAKER_BEGIN,
	BONEBREAKER_MAX_STACK,
	BONEBREAKER_CLEAR,
	STAMINA_PILLAGER_BODY_ASHES,
	EMERGENCY_ESCAPE_ACTIVATED,
	ENDERMAN_RAGE,
	FLASH_WHITE,
	PIGLIN_BABY_SPAWN,
	SET_BOSS_EVENT_OWNER,
	TECHNICIAN_ACTIVATED,
	TRIDENT_THROWN,
	VENGEANCE_OVERLAY,
	VENGEANCE_TARGET_CANCEL,
	ZOMBIE_SPAWN
	;
	
	private int universalOrdinal;
	
	EntityPairingPacketTypes() {
		this.universalOrdinal = EntityPairingPacketType.ENUM_MANAGER.assign(this);
	}
	
	@Override
	public int universalOrdinal() {
		return this.universalOrdinal;
	}
}
