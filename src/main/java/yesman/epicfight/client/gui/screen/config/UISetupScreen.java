package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.client.gui.ScreenCalculations.AlignDirection;
import yesman.epicfight.client.gui.ScreenCalculations.HorizontalBasis;
import yesman.epicfight.client.gui.ScreenCalculations.VerticalBasis;
import yesman.epicfight.client.gui.widgets.UIComponent;
import yesman.epicfight.client.gui.widgets.UIComponent.PassiveUIComponent;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.config.OptionHandler;
import yesman.epicfight.main.EpicFightMod;

public class UISetupScreen extends Screen {
	protected final Screen parentScreen;
	private UIComponent draggingButton;

	public UISetupScreen(Screen parentScreen) {
		super(Component.literal(EpicFightMod.format("%s.gui.configuration.ui_setup")));
		
		this.parentScreen = parentScreen;
	}

	@Override
	public void init() {
		this.renderables.clear();
		
		int weaponInnateX = ClientConfig.weaponInnateBaseX.positionGetter.apply(this.width, ClientConfig.weaponInnateX);
		int weaponInnateY = ClientConfig.weaponInnateBaseY.positionGetter.apply(this.height, ClientConfig.weaponInnateY);
		
		OptionHandler<Integer> weaponInnateXHandler = OptionHandler.of(ClientConfig.weaponInnateX, (val) -> ClientConfig.weaponInnateX = val);
		OptionHandler<Integer> weaponInnateYHandler = OptionHandler.of(ClientConfig.weaponInnateY, (val) -> ClientConfig.weaponInnateY = val);
		OptionHandler<HorizontalBasis> weaponInnateBaseXHandler = OptionHandler.of(ClientConfig.weaponInnateBaseX, (val) -> ClientConfig.weaponInnateBaseX = val);
		OptionHandler<VerticalBasis> weaponInnateBaseYHandler = OptionHandler.of(ClientConfig.weaponInnateBaseY, (val) -> ClientConfig.weaponInnateBaseY = val);
		
		// Weapon innate icon
		this.addRenderableWidget(new UIComponent(weaponInnateX, weaponInnateY, weaponInnateXHandler, weaponInnateYHandler, weaponInnateBaseXHandler, weaponInnateBaseYHandler,
                32, 32, 0, 0, 1, 1, 1, 1, 0, 163, 184, this, EpicFightMod.identifier("textures/gui/skills/weapon_innate/sweeping_edge.png")
        ));
		
		int staminaX = ClientConfig.staminaBarBaseX.positionGetter.apply(this.width, ClientConfig.staminaBarX);
		int staminaY = ClientConfig.staminaBarBaseY.positionGetter.apply(this.height, ClientConfig.staminaBarY);
		OptionHandler<Integer> staminaBarXHandler = OptionHandler.of(ClientConfig.staminaBarX, (val) -> ClientConfig.staminaBarX = val);
		OptionHandler<Integer> staminaBarYHandler = OptionHandler.of(ClientConfig.staminaBarY, (val) -> ClientConfig.staminaBarY = val);
		OptionHandler<HorizontalBasis> staminaBarBaseXHandler = OptionHandler.of(ClientConfig.staminaBarBaseX, (val) -> ClientConfig.staminaBarBaseX = val);
		OptionHandler<VerticalBasis> staminaBarBaseYHandler = OptionHandler.of(ClientConfig.staminaBarBaseY, (val) -> ClientConfig.staminaBarBaseY = val);
		
		// Stamina bar
		this.addRenderableWidget(new UIComponent(staminaX, staminaY, staminaBarXHandler, staminaBarYHandler, staminaBarBaseXHandler, staminaBarBaseYHandler,
                118, 4, 2, 38, 237, 9, 256, 256, 255, 128, 64, this, EpicFightMod.identifier("textures/gui/battle_icons.png")
        ));
		
		int chargingBarX = ClientConfig.chargingBarBaseX.positionGetter.apply(this.width, ClientConfig.chargingBarX);
		int chargingBarY = ClientConfig.chargingBarBaseY.positionGetter.apply(this.height, ClientConfig.chargingBarY);
		OptionHandler<Integer> chargingBarXHandler = OptionHandler.of(ClientConfig.chargingBarX, (val) -> ClientConfig.chargingBarX = val);
		OptionHandler<Integer> chargingBarYHandler = OptionHandler.of(ClientConfig.chargingBarY, (val) -> ClientConfig.chargingBarY = val);
		OptionHandler<HorizontalBasis> chargingBarBaseXHandler = OptionHandler.of(ClientConfig.chargingBarBaseX, (val) -> ClientConfig.chargingBarBaseX = val);
		OptionHandler<VerticalBasis> chargingBarBaseYHandler = OptionHandler.of(ClientConfig.chargingBarBaseY, (val) -> ClientConfig.chargingBarBaseY = val);
		
		// Charging bar
		this.addRenderableWidget(new UIComponent(chargingBarX, chargingBarY, chargingBarXHandler, chargingBarYHandler, chargingBarBaseXHandler, chargingBarBaseYHandler,
                238, 13, 1, 71, 237, 13, 256, 256, 255, 255, 255, this, EpicFightMod.identifier("textures/gui/battle_icons.png")
        ));
		
		int passiveX = ClientConfig.passiveBaseX.positionGetter.apply(this.width, ClientConfig.passiveX);
		int passiveY = ClientConfig.passiveBaseY.positionGetter.apply(this.height, ClientConfig.passiveY);
		OptionHandler<Integer> passiveXHandler = OptionHandler.of(ClientConfig.passiveX, (val) -> ClientConfig.passiveX = val);
		OptionHandler<Integer> passiveYHandler = OptionHandler.of(ClientConfig.passiveY, (val) -> ClientConfig.passiveY = val);
		OptionHandler<HorizontalBasis> passiveBaseXHandler = OptionHandler.of(ClientConfig.passiveBaseX, (val) -> ClientConfig.passiveBaseX = val);
		OptionHandler<VerticalBasis> passiveBaseYHandler = OptionHandler.of(ClientConfig.passiveBaseY, (val) -> ClientConfig.passiveBaseY = val);
		OptionHandler<AlignDirection> passiveAlignDirectionHandler = OptionHandler.of(ClientConfig.passiveAlignDirection, (val) -> ClientConfig.passiveAlignDirection = val);
		
		// Passive skill icons
		this.addRenderableWidget(new PassiveUIComponent(passiveX, passiveY, passiveXHandler, passiveYHandler, passiveBaseXHandler, passiveBaseYHandler, passiveAlignDirectionHandler
                , 24, 24, 0, 0, 1, 1, 1, 1, 255, 255, 255, this, EpicFightMod.identifier("textures/gui/skills/guard/guard.png"), EpicFightMod.identifier("textures/gui/skills/passive/berserker.png")
        ));
		
		this.addRenderableWidget(
			Button.builder(Component.literal("⟳"), button -> {
				ClientConfig.weaponInnateX = ClientConfig.WEAPON_INNATE_X.getDefault();
				ClientConfig.weaponInnateY = ClientConfig.WEAPON_INNATE_Y.getDefault();
				ClientConfig.weaponInnateBaseX = ClientConfig.WEAPON_INNATE_BASE_X.getDefault();
				ClientConfig.weaponInnateBaseY = ClientConfig.WEAPON_INNATE_BASE_Y.getDefault();
				ClientConfig.staminaBarX = ClientConfig.STAMINA_BAR_X.getDefault();
				ClientConfig.staminaBarY = ClientConfig.STAMINA_BAR_Y.getDefault();
				ClientConfig.staminaBarBaseX = ClientConfig.STAMINA_BAR_BASE_X.getDefault();
				ClientConfig.staminaBarBaseY = ClientConfig.STAMINA_BAR_BASE_Y.getDefault();
				ClientConfig.chargingBarX = ClientConfig.CHARGING_BAR_X.getDefault();
				ClientConfig.chargingBarY = ClientConfig.CHARGING_BAR_Y.getDefault();
				ClientConfig.chargingBarBaseX = ClientConfig.CHARGING_BAR_BASE_X.getDefault();
				ClientConfig.chargingBarBaseY = ClientConfig.CHARGING_BAR_BASE_Y.getDefault();
				ClientConfig.passiveX = ClientConfig.PASSIVE_X.getDefault();
				ClientConfig.passiveY = ClientConfig.PASSIVE_Y.getDefault();
				ClientConfig.passiveBaseX = ClientConfig.PASSIVE_BASE_X.getDefault();
				ClientConfig.passiveBaseY = ClientConfig.PASSIVE_BASE_Y.getDefault();
				ClientConfig.passiveAlignDirection = ClientConfig.PASSIVE_ALIGN_DIRECTION.getDefault();
				this.init();
			}).bounds(this.width-14, 0, 14, 14).build()
		);
	}

	@Override
	public boolean mouseClicked(double x, double y, int pressType) {
		for (GuiEventListener guieventlistener : this.children()) {
			if (guieventlistener instanceof UIComponent uiComponent) {
				if (uiComponent.popupScreen.isOpen() && uiComponent.popupScreen.mouseClicked(x, y, pressType)) {
					this.setFocused(guieventlistener);

					if (pressType == 0) {
						this.setDragging(true);
					}

					return true;
				}
			}

			if (guieventlistener.mouseClicked(x, y, pressType)) {
				this.setFocused(guieventlistener);

				if (pressType == 0) {
					this.setDragging(true);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.minecraft.level == null) {
			this.renderDirtBackground(guiGraphics);
		} else {
			this.renderBackground(guiGraphics);
		}
		
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}

	public void beginToDrag(UIComponent button) {
		this.draggingButton = button;
	}

	public void endDragging() {
		this.draggingButton = null;
	}

	public boolean isDraggingComponent(UIComponent button) {
		return this.draggingButton == button;
	}
}