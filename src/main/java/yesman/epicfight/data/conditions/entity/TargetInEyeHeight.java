package yesman.epicfight.data.conditions.entity;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class TargetInEyeHeight extends EntityPatchCondition {
	@Override
	public TargetInEyeHeight read(CompoundTag tag) {
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		return new CompoundTag();
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		double veticalDistance = Math.abs(target.getOriginal().getY() - target.getTarget().getY());
		return veticalDistance < target.getOriginal().getEyeHeight();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return List.of();
	}
}
