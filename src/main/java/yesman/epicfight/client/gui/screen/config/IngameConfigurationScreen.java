package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.online.EpicFightServerConnectionHelper;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.datapack.screen.DatapackEditScreen;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.main.AuthenticationHelper;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

public class IngameConfigurationScreen extends Screen {
	protected final Screen parentScreen;
	
	public IngameConfigurationScreen(Screen screen) {
		super(Component.translatable(EpicFightMod.format("gui.%s.configurations")));
		this.parentScreen = screen;
	}
	
	@Override
	protected void init() {
		this.addRenderableWidget(Button.builder(Component.translatable(EpicFightMod.format("gui.%s.button.graphics")), (button) -> {
			Minecraft.getInstance().setScreen(new EpicFightGraphicOptionScreen(this));
		}).pos(this.width / 2 - 165, 42).size(160, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable(EpicFightMod.format("gui.%s.button.controls")), (button) -> {
			Minecraft.getInstance().setScreen(new EpicFightControlOptionScreen(this));
		}).pos(this.width / 2 + 5, 42).size(160, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.translatable(EpicFightMod.format("gui.%s.button.datapack_edit")), (button) -> {
			Minecraft.getInstance().setScreen(new DatapackEditScreen(this));
		}).pos(this.width / 2 - 165, 68).size(160, 20).build());
		
		Tooltip unsupportedReason = null;
		
		if (EpicFightSharedConstants.IS_DEV_ENV) {
			unsupportedReason = Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.tooltip.dev_environment")));
		} else if (!EpicFightServerConnectionHelper.supported()) {
			unsupportedReason = Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.tooltip.communication_module_error")));
		} else if (ClientEngine.getInstance().getAuthHelper().status() == AuthenticationHelper.Status.OFFLINE_MODE) {
			unsupportedReason = Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.tooltip.offline_mode")));
		}
		
		Button skinConfigScreen = Button.builder(Component.translatable("gui." + EpicFightMod.EPICSKINS_MODID + ".button.skin_configuration"), (button) -> {
			if (Minecraft.getInstance().level == null) {
				Minecraft.getInstance().setScreen(ClientEngine.getInstance().getAuthHelper().getAvatarEditorScreen(this));
			} else {
				Minecraft.getInstance().setScreen(new MessageScreen<> ("Warning", "You may not open avatar screen while in the world", this, (button2) -> Minecraft.getInstance().setScreen(this), 300, 70).autoCalculateHeight());
			}
		}).pos(this.width / 2 + 5, 68).size(160, 20).tooltip(unsupportedReason).build();
		
		skinConfigScreen.active = unsupportedReason == null;
		this.addRenderableWidget(skinConfigScreen);
		
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			this.minecraft.setScreen(this.parentScreen);
		}).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build());
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderDirtBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}
}