package yesman.epicfight.world.capabilities.entitypatch;

import yesman.epicfight.api.utils.math.MathUtils;

public enum Factions implements Faction {
	NEUTRAL(MathUtils.packColor(255, 255, 0, 100)),
	UNDEAD(MathUtils.packColor(255, 0, 0, 100)),
	BLAZE(MathUtils.packColor(183, 227, 255, 255)),
	ENDERMAN(MathUtils.packColor(255, 0, 0, 100)),
	ILLAGER(MathUtils.packColor(255, 0, 0, 100)),
	PIGLINS(MathUtils.packColor(255, 0, 0, 100)),
	WITHER(MathUtils.packColor(255, 0, 0, 100)),
	VILLAGER(MathUtils.packColor(255, 0, 0, 100)),
	ZOMBIFIED_PIGLIN(MathUtils.packColor(255, 0, 0, 100))
	;
	
	final int damageColor;
	final int id;
	
	Factions(int damageColor) {
		this.id = Faction.ENUM_MANAGER.assign(this);
		this.damageColor = damageColor;
	}
	
	@Override
	public int universalOrdinal() {
		return this.id;
	}
	
	@Override
	public int damageColor() {
		return this.damageColor;
	}
	
}
