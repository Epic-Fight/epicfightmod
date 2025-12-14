package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GloveCapability extends WeaponCapability {

	protected GloveCapability(CapabilityItem.Builder builder) {
		super(builder);
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		ItemStack offhandItme = entitypatch.getOriginal().getOffhandItem();
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(offhandItme);
		boolean isFist = itemCap.getWeaponCategory() == WeaponCategories.FIST;
		return isFist || !(offhandItme.getItem() instanceof SwordItem || offhandItme.getItem() instanceof DiggerItem);
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return true;
	}
}