package yesman.epicfight.world.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import yesman.epicfight.main.EpicFightMod;

public class UchigatanaItem extends WeaponItem {
	public UchigatanaItem(Item.Properties build) {
		super(EpicFightItemTier.UCHIGATANA, 0, -2.0F, build);
	}
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return toRepair.getItem() == Items.IRON_BARS;
	}
    
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal(""));
		tooltip.add(Component.translatable(EpicFightMod.format("item.%s.uchigatana.tooltip")));
	}
	
	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState blockstate) {
		if (blockstate.is(Blocks.COBWEB)) return 15.0F;
		else return blockstate.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
	}
	
	@Override
	public boolean isCorrectToolForDrops(BlockState blockstate) {
		return blockstate.is(Blocks.COBWEB);
	}
}