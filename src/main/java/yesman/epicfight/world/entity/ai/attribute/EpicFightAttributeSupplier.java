package yesman.epicfight.world.entity.ai.attribute;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EpicFightAttributeSupplier extends AttributeSupplier {
	private static Map<Attribute, AttributeInstance> putEpicFightAttributes(Map<Attribute, AttributeInstance> originalMap) {
		AttributeSupplier supplier = AttributeSupplier.builder()
				.add(Attributes.ATTACK_DAMAGE)
				.add(EpicFightAttributes.WEIGHT.get())
				.add(EpicFightAttributes.IMPACT.get())
				.add(EpicFightAttributes.ARMOR_NEGATION.get())
				.add(EpicFightAttributes.MAX_STRIKES.get())
				.add(EpicFightAttributes.STUN_ARMOR.get())
				.add(EpicFightAttributes.EXECUTION_RESISTANCE.get())
				.add(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())
				.add(EpicFightAttributes.OFFHAND_IMPACT.get())
				.add(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())
				.add(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())
			.build();
		
		Map<Attribute, AttributeInstance> newMap = new HashMap<>(supplier.instances);
		newMap.putAll(originalMap);
		
		return ImmutableMap.copyOf(newMap);
	}
	
	public EpicFightAttributeSupplier(AttributeSupplier copy) {
		super(putEpicFightAttributes(copy.instances));
	}
}