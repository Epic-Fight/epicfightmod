package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

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
	
	@Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return List.of();
	}
}
