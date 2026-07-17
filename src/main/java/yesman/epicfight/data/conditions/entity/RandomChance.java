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

public class RandomChance extends EntityPatchCondition {
	private double chance;
	
	public RandomChance() {
		this.chance = 0.0D;
	}
	
	public RandomChance(double chance) {
		this.chance = chance;
	}
	
	@Override
	public RandomChance read(CompoundTag tag) {
		this.chance = this.assertTag("chance", "decimal", tag, NumericTag.class, CompoundTag::getDouble);
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putDouble("chance", this.chance);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		return target.getOriginal().getRandom().nextDouble() < this.chance;
	}
}
