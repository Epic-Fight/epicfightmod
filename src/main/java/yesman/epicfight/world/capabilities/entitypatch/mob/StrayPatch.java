package yesman.epicfight.world.capabilities.entitypatch.mob;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.registry.entries.EpicFightItems;

public class StrayPatch<T extends AbstractSkeleton> extends SkeletonPatch<T> {
	public StrayPatch(T original) {
		super(original);
	}

    @Override
    public void onJoinWorld(T entity, Level level, boolean worldgenSpawn) {
        super.onJoinWorld(entity, level, worldgenSpawn);
		
		this.original.setItemSlot(EquipmentSlot.HEAD, new ItemStack(EpicFightItems.STRAY_HAT.get()));
		this.original.setItemSlot(EquipmentSlot.CHEST, new ItemStack(EpicFightItems.STRAY_ROBE.get()));
		this.original.setItemSlot(EquipmentSlot.LEGS, new ItemStack(EpicFightItems.STRAY_PANTS.get()));
	}
}