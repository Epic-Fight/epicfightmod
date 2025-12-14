package yesman.epicfight.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec.IntValue SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER = BUILDER.defineInRange("loot.skill_book_mob_drop_chance_modifier", 0, -100, 100);
	public static final ForgeConfigSpec.IntValue SKILL_BOOK_CHEST_LOOT_MODIFIER = BUILDER.defineInRange("loot.skill_book_chest_drop_chance_modifier", 0, -100, 100);
	public static final ForgeConfigSpec SPEC;
	
	public static int skillBookMobDropChanceModifier;
	public static int skillBookChestLootModifier;
	
	static {
		EpicFightGameRules.GAME_RULES.values().forEach(configurableGameRule -> configurableGameRule.defineConfig(BUILDER));
		SPEC = BUILDER.build();
	}
	
	@SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getType() != ModConfig.Type.COMMON) {
			return;
		}
		
		skillBookMobDropChanceModifier = SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER.get();
		skillBookChestLootModifier = SKILL_BOOK_CHEST_LOOT_MODIFIER.get();
	}
}
