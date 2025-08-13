package yesman.epicfight.client.gui.screen.config;

import java.io.File;
import java.io.IOException;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.renderer.shader.compute_boost.loader.ShaderRegistries;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightGraphicOptionScreen extends EpicFightOptionSubScreen {
	private EpicFightOptionList optionsList;
	
	public EpicFightGraphicOptionScreen(Screen parentScreen) {
		super(parentScreen, Component.translatable("gui." + EpicFightMod.MODID + ".graphic_options"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		String modid = EpicFightMod.MODID;
		
		this.optionsList = new EpicFightOptionList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		
		int buttonHeight = -32;
		
		Button showTargetIndicatorButton = Button.builder(Component.translatable("gui." + modid + ".target_indicator." + (ClientConfig.showTargetIndicator ? "on" : "off")), (button) -> {
			ClientConfig.showTargetIndicator = !ClientConfig.showTargetIndicator;
			button.setMessage(Component.translatable("gui." + modid + ".target_indicator." + (ClientConfig.showTargetIndicator ? "on" : "off")));
		}).pos(this.width / 2 + 5, this.height / 4 - 8).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".target_indicator.tooltip"))).build();
		
		Button healthBarVisibilityOptionButton = Button.builder(Component.translatable("gui." + modid + ".health_bar_show_option." + ClientConfig.healthBarVisibility.toString()), (button) -> {
			ClientConfig.healthBarVisibility = ClientConfig.healthBarVisibility.nextEnum();
			button.setMessage(Component.translatable("gui." + modid + ".health_bar_show_option." + ClientConfig.healthBarVisibility.toString()));
		}).pos(this.width / 2 - 165, this.height / 4 - 8).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".filter_animation.tooltip"))).build();
		
		this.optionsList.addSmall(showTargetIndicatorButton, healthBarVisibilityOptionButton);
		
		buttonHeight += 24;
		
		Button aimingCorrectionButton = Button.builder(Component.translatable("gui." + modid + ".aiming_correction." + (ClientConfig.aimingPovCorrection ? "on" : "off")), (button) -> {
			ClientConfig.aimingPovCorrection = !ClientConfig.aimingPovCorrection;
			button.setMessage(Component.translatable("gui." + modid + ".aiming_correction." + (ClientConfig.aimingPovCorrection ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".aiming_correction.tooltip"))).build();
		
		Button enableAimHelperButton = Button.builder(Component.translatable("gui." + modid + ".aim_helper." + (ClientConfig.enableAimHelper ? "on" : "off")), (button) -> {
			ClientConfig.enableAimHelper = !ClientConfig.enableAimHelper;
			button.setMessage(Component.translatable("gui." + modid + ".aim_helper." + (ClientConfig.enableAimHelper ? "on" : "off")));
		}).pos(this.width / 2 + 5, this.height / 4 - 8).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".aim_helper.tooltip"))).build();
		
		this.optionsList.addSmall(aimingCorrectionButton, enableAimHelperButton);
		
		buttonHeight += 24;
		
		Button bloodEffectsButton = Button.builder(Component.translatable("gui." + modid + ".blood_effects." + (ClientConfig.bloodEffects ? "on" : "off")), (button) -> {
			ClientConfig.bloodEffects = !ClientConfig.bloodEffects;
			button.setMessage(Component.translatable("gui." + modid + ".blood_effects." + (ClientConfig.bloodEffects ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 - 8).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".blood_effects.tooltip"))).build();
		
		Button exportCustomArmors = Button.builder(Component.translatable("gui." + modid + ".export_custom_armor"), (button) -> {
			File resourcePackDirectory = Minecraft.getInstance().getResourcePackDirectory().toFile();
			try {
				HumanoidModelBaker.exportModels(resourcePackDirectory);
				Util.getPlatform().openFile(resourcePackDirectory);
			} catch (IOException e) {
				EpicFightMod.LOGGER.info("Failed to export custom armor models");
				e.printStackTrace();
			}
		}).pos(this.width / 2 + 5, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".export_custom_armor.tooltip"))).build();
		
		this.optionsList.addSmall(bloodEffectsButton, exportCustomArmors);
		
		buttonHeight += 24;
		
		Button enablePovAction = Button.builder(Component.translatable("gui." + modid + ".enable_pov_action." + (ClientConfig.enablePovAction ? "on" : "off")), (button) -> {
			ClientConfig.enablePovAction = !ClientConfig.enablePovAction;
			button.setMessage(Component.translatable("gui." + modid + ".enable_pov_action." + (ClientConfig.enablePovAction ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".enable_pov_action.tooltip"))).build();
		
		Button uiSetupButton = Button.builder(Component.translatable("gui." + modid + ".ui_setup"), (button) -> {
			this.minecraft.setScreen(new UISetupScreen(this));
		}).pos(this.width / 2 + 5, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".ui_setup.tooltip"))).build();
		
		this.optionsList.addSmall(enablePovAction, uiSetupButton);
		
		buttonHeight += 24;
		
		Button showEpicfightAttributesButton = Button.builder(Component.translatable("gui." + modid + ".show_attributes." + (ClientConfig.showEpicFightAttributesInTooltip ? "on" : "off")), (button) -> {
			ClientConfig.showEpicFightAttributesInTooltip = !ClientConfig.showEpicFightAttributesInTooltip;
			button.setMessage(Component.translatable("gui." + modid + ".show_attributes." + (ClientConfig.showEpicFightAttributesInTooltip ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".show_attributes.tooltip"))).build();
		
		Button maxHitProjectilesButton = Button.builder(Component.translatable("gui." + modid + ".max_stuck_projectiles", String.valueOf(ClientConfig.maxStuckProjectiles)), (button) -> {
			ClientConfig.maxStuckProjectiles = (ClientConfig.maxStuckProjectiles + 1) % 30;
			button.setMessage(Component.translatable("gui." + modid + ".max_stuck_projectiles", String.valueOf(ClientConfig.maxStuckProjectiles)));
		}).pos(this.width / 2 + 5, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".max_stuck_projectiles.tooltip"))).build();
		
		this.optionsList.addSmall(showEpicfightAttributesButton, maxHitProjectilesButton);
		
		buttonHeight += 24;
		
		Button firstPersonModelButton = Button.builder(Component.translatable("gui." + modid + ".first_person_model." + (ClientConfig.enableAnimatedFirstPersonModel ? "on" : "off")), (button) -> {
			ClientConfig.enableAnimatedFirstPersonModel = !ClientConfig.enableAnimatedFirstPersonModel;
			button.setMessage(Component.translatable("gui." + modid + ".first_person_model." + (ClientConfig.enableAnimatedFirstPersonModel ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".first_person_model.tooltip"))).build();
		
		Button useAnimationShaderButton = Button.builder(Component.translatable("gui." + modid + ".use_animation_shader." + (ClientConfig.activateAnimationShader ? "on" : "off")), (button) -> {
			ClientConfig.activateAnimationShader = !ClientConfig.activateAnimationShader;
			button.setMessage(Component.translatable("gui." + modid + ".use_animation_shader." + (ClientConfig.activateAnimationShader ? "on" : "off")));
		}).pos(this.width / 2 + 5, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".use_animation_shader.tooltip"))).build();
		
		if (!ShaderRegistries.isComputeShaderSupport()) {
			useAnimationShaderButton.active = false;
			useAnimationShaderButton.setTooltip(Tooltip.create(Component.translatable("gui." + EpicFightMod.MODID + ".use_animation_shader.locked.tooltip")));
		}
		
		this.optionsList.addSmall(firstPersonModelButton, useAnimationShaderButton);
		
		buttonHeight += 24;
		
		Button enableCosmetics = Button.builder(Component.translatable("gui." + modid + ".enable_cosmetics." + (ClientConfig.enableCosmetics ? "on" : "off")), (button) -> {
			ClientConfig.enableCosmetics = !ClientConfig.enableCosmetics;
			button.setMessage(Component.translatable("gui." + modid + ".enable_cosmetics." + (ClientConfig.enableCosmetics ? "on" : "off")));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".enable_cosmetics.tooltip"))).build();
		
		this.optionsList.addSmall(enableCosmetics, null);
		
		buttonHeight += 30;
		
		this.optionsList.addBig(new ColorSlider(this.font, this.width / 2 - 150, this.height / 4 + buttonHeight, 300, 20, Component.translatable("gui." + modid + ".aim_helper_color"),
												ColorSlider.Style.CLASSIC, ClientConfig.aimHelperColor, (position, color) -> ClientConfig.aimHelperColor = position));
		
		this.addWidget(this.optionsList);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		ClientEngine.getInstance().renderEngine.versionNotifier.render(guiGraphics, false);
		this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
	}
}