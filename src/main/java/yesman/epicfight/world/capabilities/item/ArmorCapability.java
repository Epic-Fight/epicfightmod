package yesman.epicfight.world.capabilities.item;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class ArmorCapability extends CapabilityItem {
	protected final double weight;
	protected final double stunArmor;
	protected final ArmorItem.Type armorType;
	
	protected ArmorCapability(ArmorCapability.Builder builder) {
		super(builder);
		
		this.armorType = builder.armorType;
		this.weight = builder.weight;
		this.stunArmor = builder.stunArmor;
	}
	
	@Override
	public void modifyItemTooltip(ItemStack stack, List<Component> itemTooltip, LivingEntityPatch<?> entitypatch) {
	}
	
	public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiersForArmor() {
		Multimap<Holder<Attribute>, AttributeModifier> map = HashMultimap.create();
		
		ResourceLocation modifierId = ResourceLocation.withDefaultNamespace("armor." + this.armorType.getName());
		map.put(EpicFightAttributes.WEIGHT, new AttributeModifier(modifierId, this.weight, Operation.ADD_VALUE));
		map.put(EpicFightAttributes.STUN_ARMOR, new AttributeModifier(modifierId, this.stunArmor, Operation.ADD_VALUE));
		
        return map;
    }
	
	public static ArmorCapability.Builder builder() {
		return new ArmorCapability.Builder();
	}
	
	public static class Builder extends CapabilityItem.Builder<Builder> {
		private ArmorItem.Type armorType;
		private double weight;
		private double stunArmor;
		
		protected Builder() {
			this.constructor = ArmorCapability::new;
			this.weight = -1.0D;
			this.stunArmor = -1.0D;
		}
		
		public Builder byItem(Item item) {
			if (item instanceof ArmorItem armorItem) {
				Holder<ArmorMaterial> armorMaterial = armorItem.getMaterial();
				this.armorType = armorItem.getType();
				
				if (this.weight < 0.0D) {
					this.weight = armorMaterial.value().defense().getOrDefault(this.armorType, 1) * 2.5F;
				}
				
				if (this.stunArmor < 0.0D) {
					this.stunArmor = armorMaterial.value().defense().getOrDefault(this.armorType, 1) * 0.375F;
				}
			}
			
			return this;
		}
		
		public Builder weight(double weight) {
			this.weight = weight;
			return this;
		}
		
		public Builder stunArmor(double stunArmor) {
			this.stunArmor = stunArmor;
			return this;
		}
	}
}