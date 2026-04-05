package yesman.epicfight.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public class CommonConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec.IntValue SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER = BUILDER.defineInRange("loot.skill_book_mob_drop_chance_modifier", 0, -100, 100);
	public static final ModConfigSpec.IntValue SKILL_BOOK_CHEST_LOOT_MODIFIER = BUILDER.defineInRange("loot.skill_book_chest_drop_chance_modifier", 0, -100, 100);

	public static final ModConfigSpec.DoubleValue REACH_HITBOX_BONUS_SCALE = BUILDER
		.comment(
			"Multiplier that determines how much your extra reach buffs affect the swing distance of Epic Fight weapons"
		)
		.defineInRange("reach.bonus", 0.5D, 0.0D, 10.0D);

	public static final ModConfigSpec.DoubleValue REACH_HITBOX_MAX_BONUS = BUILDER
		.defineInRange("reach.max_bonus", 8.0D, 0.0D, 128.0D);

	public static final ModConfigSpec SPEC;
	
	public static int skillBookMobDropChanceModifier;
	public static int skillBookChestLootModifier;

	public static double reachHitboxBonusToColliderScale = 0.5D;
	public static double reachHitboxMaxBonus = 8.0D;
	
	static {
		EpicFightGameRules.GAME_RULES.values().forEach(configurableGameRule -> configurableGameRule.defineConfig(BUILDER));
		SPEC = BUILDER.build();
	}
	
	@SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
		if (event.getConfig().getType() != ModConfig.Type.COMMON) {
			return;
		}
		
		skillBookMobDropChanceModifier = SKILL_BOOK_MOB_DROP_CHANCE_MODIFIER.get();
		skillBookChestLootModifier = SKILL_BOOK_CHEST_LOOT_MODIFIER.get();
		reachHitboxBonusToColliderScale = REACH_HITBOX_BONUS_SCALE.get();
		reachHitboxMaxBonus = REACH_HITBOX_MAX_BONUS.get();
	}
}
