package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import yesman.epicfight.registry.entries.EpicFightItems;

public class StrayPatch<T extends AbstractSkeleton> extends SkeletonPatch<T> {
	public StrayPatch(T original) {
		super(original);
	}
	
	@Override
	public void onJoinWorld(T entityIn, EntityJoinLevelEvent event) {
		super.onJoinWorld(entityIn, event);
		
		this.original.setItemSlot(EquipmentSlot.HEAD, new ItemStack(EpicFightItems.STRAY_HAT.get()));
		this.original.setItemSlot(EquipmentSlot.CHEST, new ItemStack(EpicFightItems.STRAY_ROBE.get()));
		this.original.setItemSlot(EquipmentSlot.LEGS, new ItemStack(EpicFightItems.STRAY_PANTS.get()));
	}
}