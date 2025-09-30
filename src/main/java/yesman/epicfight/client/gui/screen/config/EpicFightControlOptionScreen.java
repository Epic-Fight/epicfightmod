package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.gui.widgets.RewindableButton;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightControlOptionScreen extends EpicFightOptionSubScreen {
	private EpicFightOptionList optionsList;
	
	public EpicFightControlOptionScreen(Screen parentScreen) {
		super(parentScreen, Component.translatable("gui." + EpicFightMod.MODID + ".control_options"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		String modid = EpicFightMod.MODID;
		this.optionsList = new EpicFightOptionList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		int buttonHeight = -32;
		
		Button longPressCounterButton = new RewindableButton(this.width / 2 - 165, this.height / 4 + buttonHeight, 160, 20,
			Component.translatable("gui." + modid + ".long_press_counter", (ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter))),
			(button) -> {
				ClientConfig.longPressCounter++;
				button.setMessage(Component.translatable("gui." + modid + ".long_press_counter", (ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter))));
			},
			(button) -> {
				ClientConfig.longPressCounter--;
				button.setMessage(Component.translatable("gui." + modid + ".long_press_counter", (ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter))));
			}
		);
		
		longPressCounterButton.setTooltip(Tooltip.create(Component.translatable("gui." + modid + ".long_press_counter.tooltip")));
		
		Button cameraAutoSwitchButton = Button.builder(Component.translatable("gui." + modid + ".camera_auto_switch." + (ClientConfig.authSwitchCamera ? "on" : "off")), (button) -> {
			ClientConfig.authSwitchCamera = !ClientConfig.authSwitchCamera;
			button.setMessage(Component.translatable("gui." + modid + ".camera_auto_switch." + (ClientConfig.authSwitchCamera ? "on" : "off")));
		}).pos(this.width / 2 + 5, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".camera_auto_switch.tooltip"))).build();
		
		this.optionsList.addSmall(longPressCounterButton, cameraAutoSwitchButton);
		
		buttonHeight += 24;
		
		Button autoSwitchingItems = Button.builder(Component.translatable("gui." + modid + ".item_preferences"), (button) -> {
			this.minecraft.setScreen(new ItemsPreferenceScreen(this));
		}).pos(this.width / 2 - 165, this.height / 4 + buttonHeight).size(160, 20).tooltip(Tooltip.create(Component.translatable("gui." + modid + ".item_preferences.tooltip"))).build();
		
		this.optionsList.addSmall(autoSwitchingItems, null);
		
		this.addWidget(this.optionsList);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		ClientEngine.getInstance().renderEngine.versionNotifier.render(guiGraphics, false);
		this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
	}
}