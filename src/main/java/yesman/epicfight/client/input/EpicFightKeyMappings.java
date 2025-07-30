package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.MOD)
public class EpicFightKeyMappings {
	public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP = new KeyMapping("key." + EpicFightMod.MODID + ".show_tooltip", InputConstants.KEY_LSHIFT, "key." + EpicFightMod.MODID + ".gui");
	public static final KeyMapping SWITCH_MODE = new KeyMapping("key." + EpicFightMod.MODID + ".switch_mode", InputConstants.KEY_R, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping DODGE = new KeyMapping("key." + EpicFightMod.MODID + ".dodge", InputConstants.KEY_LALT, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping GUARD = new KeyMapping("key." + EpicFightMod.MODID + ".guard", InputConstants.KEY_C, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping ATTACK = new  CombatKeyMapping("key." + EpicFightMod.MODID + ".attack", InputConstants.Type.MOUSE, 0, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping WEAPON_INNATE_SKILL = new CombatKeyMapping("key." + EpicFightMod.MODID + ".weapon_innate_skill", InputConstants.Type.MOUSE, 0, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping MOVER_SKILL = new  CombatKeyMapping("key." + EpicFightMod.MODID + ".mover_skill", InputConstants.Type.KEYSYM, InputConstants.KEY_SPACE, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping SKILL_EDIT = new KeyMapping("key." + EpicFightMod.MODID + ".skill_gui", InputConstants.KEY_K, "key." + EpicFightMod.MODID + ".gui");
	public static final KeyMapping LOCK_ON = new KeyMapping("key." + EpicFightMod.MODID + ".lock_on", InputConstants.KEY_G, "key." + EpicFightMod.MODID + ".combat");
	public static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping("key." + EpicFightMod.MODID + ".config", -1, "key." + EpicFightMod.MODID + ".gui");
	public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING = new KeyMapping("key." + EpicFightMod.MODID + ".switch_vanilla_model_debug", -1, "key." + EpicFightMod.MODID + ".system");
	
	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(WEAPON_INNATE_SKILL_TOOLTIP);
		event.register(SWITCH_MODE);
		event.register(DODGE);
		event.register(GUARD);
		event.register(ATTACK);
		event.register(WEAPON_INNATE_SKILL);
		event.register(MOVER_SKILL);
		event.register(SKILL_EDIT);
		event.register(LOCK_ON);
		event.register(OPEN_CONFIG_SCREEN);
		event.register(SWITCH_VANILLA_MODEL_DEBUGGING);
	}
}