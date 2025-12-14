package yesman.epicfight.world.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillBookItem extends Item {
	public static void setContainingSkill(String name, ItemStack stack) {
		stack.getOrCreateTag().put("skill", StringTag.valueOf(name));
	}
	
	public static void setContainingSkill(Skill skill, ItemStack stack) {
		setContainingSkill(skill.toString(), stack);
	}
	
	public static Skill getContainSkill(ItemStack stack) {
		if (stack.getTag() == null || !stack.getTag().contains("skill")) {
			return null;
		}
		
		String skillName = stack.getTag().getString("skill");
		
		return SkillManager.getSkill(skillName);
	}
	
	public SkillBookItem(Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.getTag() != null && stack.getTag().contains("skill");
	}
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		if (stack.getTag() != null && stack.getTag().contains("skill")) {
			ResourceLocation rl = ResourceLocation.parse(stack.getTag().getString("skill"));
			tooltip.add(Component.translatable(String.format("skill.%s.%s", rl.getNamespace(), rl.getPath())).withStyle(ChatFormatting.DARK_GRAY));
		}
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
		ItemStack itemstack = playerIn.getItemInHand(hand);
		playerIn.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).ifPresent((capability) -> {
			if (capability instanceof PlayerPatch<?> playerpatch) {
				playerpatch.openSkillBook(itemstack, hand);
			}
		});
		
		playerIn.awardStat(Stats.ITEM_USED.get(this));
		
		return InteractionResultHolder.pass(itemstack);
	}
}