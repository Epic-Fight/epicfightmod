package yesman.epicfight.world.item;

import java.util.function.Supplier;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import yesman.epicfight.main.EpicFightMod;

@SuppressWarnings("deprecation")
public enum EpicFightArmorMaterials implements ArmorMaterial {
	STRAY_CLOTH("stray_cloth", 4, new int[]{1, 2, 3, 1}, 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> {
	      return Ingredient.of(Items.STRING);
	   })
	;
	
	private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
	private final String name;
	private final int enchantability;
	private final int durabilityMultiplier;
	private final int[] damageReductionAmountArray;
	private final SoundEvent soundEvent;
	private final float toughness;
	private final float knockbackResistance;
	private final LazyLoadedValue<Ingredient> repairMaterial;

	EpicFightArmorMaterials(String nameIn, int maxDamageFactorIn, int[] damageReductionAmountsIn, int enchantabilityIn,
			SoundEvent equipSoundIn, float toughness, float knockbackResistance, Supplier<Ingredient> repairMaterialSupplier) {
		this.name = nameIn;
	    this.durabilityMultiplier = maxDamageFactorIn;
	    this.damageReductionAmountArray = damageReductionAmountsIn;
	    this.enchantability = enchantabilityIn;
	    this.soundEvent = equipSoundIn;
	    this.toughness = toughness;
	    this.knockbackResistance = knockbackResistance;
	    this.repairMaterial = new LazyLoadedValue<>(repairMaterialSupplier);
	}
	
	@Override
	public String getName() {
		return  EpicFightMod.prefix(this.name);
	}

	@Override
	public float getToughness() {
		return this.toughness;
	}

	@Override
	public float getKnockbackResistance() {
		return knockbackResistance;
	}

	@Override
	public int getDurabilityForType(ArmorItem.Type type) {
		return HEALTH_PER_SLOT[type.getSlot().getIndex()] * this.durabilityMultiplier;
	}

	@Override
	public int getDefenseForType(ArmorItem.Type type) {
		return this.damageReductionAmountArray[type.getSlot().getIndex()];
	}

	@Override
	public int getEnchantmentValue() {
		return this.enchantability;
	}

	@Override
	public SoundEvent getEquipSound() {
		return this.soundEvent;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return this.repairMaterial.get();
	}
}