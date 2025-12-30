package yesman.epicfight.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Blocks;
import yesman.epicfight.generated.LangKeys;

import java.util.List;

public class UchigatanaItem extends WeaponItem {
	public static ItemAttributeModifiers createUchigatanaAttributes() {
		return TieredWeaponItem.createAttributes(6.0F, -2.0F);
	}
	
	public UchigatanaItem(Item.Properties properties) {
		super(properties.component(DataComponents.TOOL, createToolProperties()));
	}
	
	public static Tool createToolProperties() {
        return new Tool(List.of(Tool.Rule.minesAndDrops(List.of(Blocks.COBWEB), 15.0F), Tool.Rule.overrideSpeed(BlockTags.SWORD_EFFICIENT, 1.5F)), 1.0F, 2);
    }
	
	@Override
    public int getEnchantmentValue() {
        return 22;
    }
	
	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
		return toRepair.getItem() == Items.IRON_BARS;
	}
    
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.add(Component.literal(""));
		tooltipComponents.add(Component.translatable(LangKeys.ITEM_UCHIGATANA_TOOLTIP));
	}
}