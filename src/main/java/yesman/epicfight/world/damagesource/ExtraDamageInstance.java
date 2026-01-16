package yesman.epicfight.world.damagesource;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.Enchantments;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.weaponinnate.EviscerateSkill;

public class ExtraDamageInstance {
	@SuppressWarnings("deprecation")
	public static final ExtraDamage EVISCERATE_LOST_HEALTH = new ExtraDamage(
		(attacker, itemstack, target, baseDamage, params) -> {
			int tier = 0;
			
			if (itemstack.getItem() instanceof TieredItem tieredItem) {
				tier += tieredItem.getTier().getLevel();
			}
			
			// Bad implementation: add parameter to ExtraDamageFunction to accept skill parameters
			return Math.min((target.getMaxHealth() - target.getHealth()) * (params[0] + 0.05F * tier), ((EviscerateSkill)EpicFightSkills.EVISCERATE).getDamageCap());
		},
		(itemstack, tooltips, baseDamage, params) -> {
			int tier = 0;
			
			if (itemstack.getItem() instanceof TieredItem tieredItem) {
				tier += tieredItem.getTier().getLevel();
			}
			
			tooltips.append(Component.translatable("damage_source.epicfight.target_lost_health", Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((params[0] + tier * 0.05F) * 100F) + "%").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.DARK_GRAY));
		});
	
	public static final ExtraDamage SWEEPING_EDGE_ENCHANTMENT = new ExtraDamage(
		(attacker, itemstack, target, baseDamage, params) -> {
			int i = itemstack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
			float modifier = (i > 0) ? (float)i / (i + 1.0F) : 0.0F;
			
			return baseDamage * modifier;
		},
		(itemstack, tooltips, baseDamage, params) -> {
			int i = itemstack.getEnchantmentLevel(Enchantments.SWEEPING_EDGE);
			
			if (i > 0) {
				double modifier = (double)i / (i + 1.0D);
				double damage = baseDamage * modifier;
				
				MutableComponent sweepedgetooltip = Component.translatable("damage_source.epicfight.sweeping_edge_enchant_level", Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(damage)).withStyle(ChatFormatting.DARK_PURPLE), i).withStyle(ChatFormatting.DARK_GRAY);
				tooltips.append(sweepedgetooltip);
			}
		});
	
	private final ExtraDamage calculator;
	private final float[] params;
	
	public ExtraDamageInstance(ExtraDamage calculator, float... params) {
		this.calculator = calculator;
		this.params = params;
	}
	
	public float[] getParams() {
		return this.params;
	}
	
	public Object[] toTransableComponentParams() {
		Object[] params = new Object[this.params.length];
		
		for (int i = 0; i < params.length; i++) {
			params[i] = Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(this.params[i] * 100F) + "%").withStyle(ChatFormatting.RED);
		}
		
		return params;
	}
	
	public float get(LivingEntity attacker, ItemStack hurtItem, LivingEntity target, float baseDamage) {
		return this.calculator.extraDamage.getBonusDamage(attacker, hurtItem, target, baseDamage, this.params);
	}
	
	public void setTooltips(ItemStack itemstack, MutableComponent tooltip, double baseDamage) {
		this.calculator.tooltip.setTooltip(itemstack, tooltip, baseDamage, this.params);
	}
	
	@FunctionalInterface
	public interface ExtraDamageFunction {
		float getBonusDamage(LivingEntity attacker, ItemStack hurtItem, LivingEntity target, float baseDamage, float[] params);
	}
	
	@FunctionalInterface
	public interface ExtraDamageTooltipFunction {
		void setTooltip(ItemStack itemstack, MutableComponent tooltips, double baseDamage, float[] params);
	}
	
	public static class ExtraDamage {
		ExtraDamageFunction extraDamage;
		ExtraDamageTooltipFunction tooltip;
		
		public ExtraDamage(ExtraDamageFunction extraDamage, ExtraDamageTooltipFunction tooltip) {
			this.extraDamage = extraDamage;
			this.tooltip = tooltip;
		}

		public ExtraDamageInstance create(float... params) {
			return new ExtraDamageInstance(this, params);
		}
	}
}
