package yesman.epicfight.world.effect;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightPotions {
	public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, EpicFightMod.MODID);
	
	//public static final RegistryObject<Potion> BLOOMING = POTIONS.register("blooming", () -> new Potion(new MobEffectInstance(EpicFightMobEffects.BLOOMING.get(), 1200)));
	
	public static void addRecipes() {
		//BrewingRecipeRegistry.addRecipe(Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION)), Ingredient.of(Items.AMETHYST_SHARD), PotionUtils.setPotion(new ItemStack(Items.POTION), BLOOMING.get()));
	}
}