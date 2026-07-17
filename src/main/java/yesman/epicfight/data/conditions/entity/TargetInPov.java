package yesman.epicfight.data.conditions.entity;

import io.netty.util.internal.StringUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.Function;

public class TargetInPov extends EntityPatchCondition {
	protected double min;
	protected double max;
	
	public TargetInPov() {
	}
	
	public TargetInPov(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public TargetInPov read(CompoundTag tag) {
		this.min = this.assertTag("min", "decimal", tag, NumericTag.class, CompoundTag::getDouble);
		this.max = this.assertTag("max", "decimal", tag, NumericTag.class, CompoundTag::getDouble);
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putDouble("min", this.min);
		tag.putDouble("max", this.max);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> entitypatch) {
		double degree = entitypatch.getAngleTo(entitypatch.getTarget());
		return this.min < degree && degree < this.max;
	}
	
	public static class TargetInPovHorizontal extends TargetInPov {
		public TargetInPovHorizontal() {
		}
		
		public TargetInPovHorizontal(double min, double max) {
			super(min, max);
		}
		
		@Override
		public boolean predicate(LivingEntityPatch<?> entitypatch) {
			double degree = entitypatch.getAngleToHorizontal(entitypatch.getTarget());
			return this.min < degree && degree < this.max;
		}
	}
}
