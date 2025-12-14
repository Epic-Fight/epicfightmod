package yesman.epicfight.world.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EpicFightMod.MODID);
	
	public static final RegistryObject<Item> UCHIGATANA = ITEMS.register("uchigatana", () -> new UchigatanaItem(new Item.Properties().rarity(Rarity.RARE)));
	public static final RegistryObject<Item> UCHIGATANA_SHEATH = ITEMS.register("uchigatana_sheath", () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));
	
	public static final RegistryObject<Item> WOODEN_GREATSWORD = ITEMS.register("wooden_greatsword", () -> new GreatswordItem(new Item.Properties(), Tiers.WOOD));
	public static final RegistryObject<Item> STONE_GREATSWORD = ITEMS.register("stone_greatsword", () -> new GreatswordItem(new Item.Properties(), Tiers.STONE));
	public static final RegistryObject<Item> IRON_GREATSWORD = ITEMS.register("iron_greatsword", () -> new GreatswordItem(new Item.Properties(), Tiers.IRON));
	public static final RegistryObject<Item> GOLDEN_GREATSWORD = ITEMS.register("golden_greatsword", () -> new GreatswordItem(new Item.Properties(), Tiers.GOLD));
	public static final RegistryObject<Item> DIAMOND_GREATSWORD = ITEMS.register("diamond_greatsword", () -> new GreatswordItem(new Item.Properties(), Tiers.DIAMOND));
	public static final RegistryObject<Item> NETHERITE_GREATSWORD = ITEMS.register("netherite_greatsword", () -> new GreatswordItem(new Item.Properties().fireResistant(), Tiers.NETHERITE));
	
	public static final RegistryObject<Item> WOODEN_SPEAR = ITEMS.register("wooden_spear", () -> new SpearItem(new Item.Properties(), Tiers.WOOD));
	public static final RegistryObject<Item> STONE_SPEAR = ITEMS.register("stone_spear", () -> new SpearItem(new Item.Properties(), Tiers.STONE));
	public static final RegistryObject<Item> IRON_SPEAR = ITEMS.register("iron_spear", () -> new SpearItem(new Item.Properties(), Tiers.IRON));
	public static final RegistryObject<Item> GOLDEN_SPEAR = ITEMS.register("golden_spear", () -> new SpearItem(new Item.Properties(), Tiers.GOLD));
	public static final RegistryObject<Item> DIAMOND_SPEAR = ITEMS.register("diamond_spear", () -> new SpearItem(new Item.Properties(), Tiers.DIAMOND));
	public static final RegistryObject<Item> NETHERITE_SPEAR = ITEMS.register("netherite_spear", () -> new SpearItem(new Item.Properties().fireResistant(), Tiers.NETHERITE));
	
	public static final RegistryObject<Item> WOODEN_TACHI = ITEMS.register("wooden_tachi", () -> new TachiItem(new Item.Properties(), Tiers.WOOD));
	public static final RegistryObject<Item> STONE_TACHI = ITEMS.register("stone_tachi", () -> new TachiItem(new Item.Properties(), Tiers.STONE));
	public static final RegistryObject<Item> IRON_TACHI = ITEMS.register("iron_tachi", () -> new TachiItem(new Item.Properties(), Tiers.IRON));
	public static final RegistryObject<Item> GOLDEN_TACHI = ITEMS.register("golden_tachi", () -> new TachiItem(new Item.Properties(), Tiers.GOLD));
	public static final RegistryObject<Item> DIAMOND_TACHI = ITEMS.register("diamond_tachi", () -> new TachiItem(new Item.Properties(), Tiers.DIAMOND));
	public static final RegistryObject<Item> NETHERITE_TACHI = ITEMS.register("netherite_tachi", () -> new TachiItem(new Item.Properties().fireResistant(), Tiers.NETHERITE));
	
	public static final RegistryObject<Item> WOODEN_LONGSWORD = ITEMS.register("wooden_longsword", () -> new LongswordItem(new Item.Properties(), Tiers.WOOD));
	public static final RegistryObject<Item> STONE_LONGSWORD = ITEMS.register("stone_longsword", () -> new LongswordItem(new Item.Properties(), Tiers.STONE));
	public static final RegistryObject<Item> IRON_LONGSWORD = ITEMS.register("iron_longsword", () -> new LongswordItem(new Item.Properties(), Tiers.IRON));
	public static final RegistryObject<Item> GOLDEN_LONGSWORD = ITEMS.register("golden_longsword", () -> new LongswordItem(new Item.Properties(), Tiers.GOLD));
	public static final RegistryObject<Item> DIAMOND_LONGSWORD = ITEMS.register("diamond_longsword", () -> new LongswordItem(new Item.Properties(), Tiers.DIAMOND));
	public static final RegistryObject<Item> NETHERITE_LONGSWORD = ITEMS.register("netherite_longsword", () -> new LongswordItem(new Item.Properties().fireResistant(), Tiers.NETHERITE));
	
	public static final RegistryObject<Item> WOODEN_DAGGER = ITEMS.register("wooden_dagger", () -> new DaggerItem(new Item.Properties(), Tiers.WOOD));
	public static final RegistryObject<Item> STONE_DAGGER = ITEMS.register("stone_dagger", () -> new DaggerItem(new Item.Properties(), Tiers.STONE));
	public static final RegistryObject<Item> IRON_DAGGER = ITEMS.register("iron_dagger", () -> new DaggerItem(new Item.Properties(), Tiers.IRON));
	public static final RegistryObject<Item> GOLDEN_DAGGER = ITEMS.register("golden_dagger", () -> new DaggerItem(new Item.Properties(), Tiers.GOLD));
	public static final RegistryObject<Item> DIAMOND_DAGGER = ITEMS.register("diamond_dagger", () -> new DaggerItem(new Item.Properties(), Tiers.DIAMOND));
	public static final RegistryObject<Item> NETHERITE_DAGGER = ITEMS.register("netherite_dagger", () -> new DaggerItem(new Item.Properties().fireResistant(), Tiers.NETHERITE));
	
	public static final RegistryObject<Item> GLOVE = ITEMS.register("glove", () -> new GloveItem(new Item.Properties(), EpicFightItemTier.GLOVE));
	public static final RegistryObject<Item> BOKKEN = ITEMS.register("bokken", () -> new SwordItem(Tiers.WOOD, 3, -2.4F, new Item.Properties()));
	
	public static final RegistryObject<Item> STRAY_HAT = ITEMS.register("stray_hat", () -> new ArmorItem(EpicFightArmorMaterials.STRAY_CLOTH, ArmorItem.Type.HELMET, new Item.Properties()));
	public static final RegistryObject<Item> STRAY_ROBE = ITEMS.register("stray_robe", () -> new ArmorItem(EpicFightArmorMaterials.STRAY_CLOTH, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
	public static final RegistryObject<Item> STRAY_PANTS = ITEMS.register("stray_pants", () -> new ArmorItem(EpicFightArmorMaterials.STRAY_CLOTH, ArmorItem.Type.LEGGINGS, new Item.Properties()));
	
	public static final RegistryObject<Item> SKILLBOOK = ITEMS.register("skillbook", () -> new SkillBookItem(new Item.Properties().rarity(Rarity.RARE).stacksTo(1)));
}