package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.config.ClientConfig;

public class EpicFightOptionSubScreen extends Screen {
	protected final Screen lastScreen;

	public EpicFightOptionSubScreen(Screen parentScreen, Component title) {
		super(title);
		this.lastScreen = parentScreen;
	}
	
	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			ClientConfig.saveChanges();
			this.onClose();
		}).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
	}
	
	@Override
	public void removed() {
		ClientConfig.saveChanges();
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}
	
	protected void basicListRender(GuiGraphics guiGraphics, EpicFightOptionList optionList, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		optionList.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
}