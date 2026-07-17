package yesman.epicfight.data.conditions.entity;

import io.netty.util.internal.StringUtil;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.Locale;

public class HealthPoint extends EntityPatchCondition {
	private double health;
	private Comparator comparator;
	
	public HealthPoint() {
		this.health = 0.0D;
	}
	
	public HealthPoint(double health, Comparator comparator) {
		this.health = health;
		this.comparator = comparator;
	}
	
	@Override
	public HealthPoint read(CompoundTag tag) {
		this.health = this.assertTag("health", "decimal", tag, NumericTag.class, CompoundTag::getDouble);
		this.comparator = this.assertEnumTag("comparator", Comparator.class, tag);
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("comparator", ParseUtil.toLowerCase(this.comparator.toString()));
		tag.putDouble("health", this.health);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		switch (this.comparator) {
		case LESS_ABSOLUTE:
			return this.health > target.getOriginal().getHealth();
		case GREATER_ABSOLUTE:
			return this.health < target.getOriginal().getHealth();
		case LESS_RATIO:
			return this.health > target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
		case GREATER_RATIO:
			return this.health < target.getOriginal().getHealth() / target.getOriginal().getMaxHealth();
		}
		
		return true;
	}
	
	public enum Comparator {
		GREATER_ABSOLUTE, LESS_ABSOLUTE, GREATER_RATIO, LESS_RATIO
	}
}
