package yesman.epicfight.world.damagesource;

public enum StunType {
	NONE("damage_source.epicfight.stun_none", true),
	SHORT("damage_source.epicfight.stun_short", false),
	LONG("damage_source.epicfight.stun_long", true),
	HOLD("damage_source.epicfight.stun_hold", false),
	KNOCKDOWN("damage_source.epicfight.stun_knockdown", true),
	NEUTRALIZE("damage_source.epicfight.stun_neutralize", true),
	FALL("damage_source.epicfight.stun_fall", true);
	
	private final String tooltip;
	private final boolean fixedStunTime;
	
	StunType(String tooltip, boolean fixedStunTime) {
		this.tooltip = tooltip;
		this.fixedStunTime = fixedStunTime;
	}
	
	public boolean hasFixedStunTime() {
		return this.fixedStunTime;
	}
	
	@Override
	public String toString() {
		return this.tooltip;
	}
}