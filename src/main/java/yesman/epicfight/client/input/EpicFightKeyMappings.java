package yesman.epicfight.client.input;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(value = Dist.CLIENT)
public class EpicFightKeyMappings {
	
	// GUI
	public static final KeyMapping WEAPON_INNATE_SKILL_TOOLTIP = new KeyMapping(EpicFightMod.format("key.%s.show_tooltip"), InputConstants.KEY_LSHIFT, EpicFightMod.format("key.%s.gui"));
	public static final KeyMapping SKILL_EDIT = new KeyMapping(EpicFightMod.format("key.%s.skill_gui"), InputConstants.KEY_K, EpicFightMod.format("key.%s.gui"));
	public static final KeyMapping OPEN_CONFIG_SCREEN = new KeyMapping(EpicFightMod.format("key.%s.config"), -1, EpicFightMod.format("key.%s.gui"));
	
	// Ingmae
	public static final KeyMapping SWITCH_MODE = new KeyMapping(EpicFightMod.format("key.%s.switch_mode"), InputConstants.KEY_R, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping DODGE = new KeyMapping(EpicFightMod.format("key.%s.dodge"), InputConstants.KEY_LALT, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping GUARD = new KeyMapping(EpicFightMod.format("key.%s.guard"), InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping ATTACK = new  CombatKeyMapping(EpicFightMod.format("key.%s.attack"), InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping WEAPON_INNATE_SKILL = new CombatKeyMapping(EpicFightMod.format("key.%s.weapon_innate_skill"), InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping MOVER_SKILL = new  CombatKeyMapping(EpicFightMod.format("key.%s.mover_skill"), InputConstants.Type.KEYSYM, InputConstants.KEY_SPACE, EpicFightMod.format("key.%s.combat"));
	public static final KeyMapping LOCK_ON = new KeyMapping(EpicFightMod.format("key.%s.lock_on"), InputConstants.KEY_G, EpicFightMod.format("key.%s.combat"));
	
	// System
	public static final KeyMapping SWITCH_VANILLA_MODEL_DEBUGGING = new KeyMapping(EpicFightMod.format("key.%s.switch_vanilla_model_debug"), -1, EpicFightMod.format("key.%s.system"));
	
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