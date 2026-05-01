package yesman.epicfight.main;

import java.util.function.UnaryOperator;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class EpicFightExtensibleEnums {
	
	// Rarities
	public static final EnumProxy<Rarity> UNIQUE_EMUM_PROXLY = new EnumProxy<> (
		  Rarity.class
		, -1
		, EpicFightMod.MODID + ":unique"
		, (UnaryOperator<Style>)style -> style.withColor(ChatFormatting.GREEN)
    );
	
	public static void initExtensibleEnums() {
		EpicFightRairity.init();
	}
	
	public static class EpicFightRairity {
		public static Rarity UNIQUE;
		
		private static void init() {
			UNIQUE = UNIQUE_EMUM_PROXLY.getValue();
		}
	}
}
