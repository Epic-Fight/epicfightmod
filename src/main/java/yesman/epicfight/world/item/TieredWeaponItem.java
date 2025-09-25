package yesman.epicfight.world.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

public abstract class TieredWeaponItem extends SwordItem {
    public static ItemAttributeModifiers createAttributes(Tier tier, float attackDamage, float attackSpeed, float attackSpeedModifier) {
        return createAttributes(attackDamage + tier.getAttackDamageBonus(), attackSpeed + WeaponCapabilityPresets.vanillaTierToLevel(tier) * attackSpeedModifier);
    }
	
    public static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE
                , new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)attackDamage, AttributeModifier.Operation.ADD_VALUE)
                , EquipmentSlotGroup.MAINHAND
            )
            .add(
                  Attributes.ATTACK_SPEED
                , new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)attackSpeed, AttributeModifier.Operation.ADD_VALUE)
                , EquipmentSlotGroup.MAINHAND
            )
            .build();
    }
    
	public TieredWeaponItem(Tier tier, Item.Properties properties) {
		super(tier, properties);
	}
}