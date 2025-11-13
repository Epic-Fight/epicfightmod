package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.gui.widgets.RewindableButton;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class EpicFightControlOptionScreen extends EpicFightOptionSubScreen {
	private EpicFightOptionList optionsList;
	
	public EpicFightControlOptionScreen(Screen parentScreen) {
		super(parentScreen, Component.translatable(EpicFightMod.format("gui.%s.control_options")));
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.optionsList = new EpicFightOptionList(this.minecraft, this.width, this.height - 64, 32, 25);
		int buttonHeight = -32;
		
		Button longPressCounterButton =
			new RewindableButton(
				this.width / 2 - 165,
				this.height / 4 + buttonHeight,
				160,
				20,
				Component.translatable(
					EpicFightMod.format("gui.%s.long_press_counter"),
					ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
				),
				button -> {
					ClientConfig.longPressCounter++;
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.long_press_counter"),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
						)
					);
				},
				button -> {
					ClientConfig.longPressCounter--;
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.long_press_counter"),
							ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
						)
					);
				}
			);
		
		longPressCounterButton.setTooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.long_press_counter.tooltip"))));
		
		Button cameraAutoSwitchButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.authSwitchCamera ? "on" : "off"))),
				button -> {
					ClientConfig.authSwitchCamera = !ClientConfig.authSwitchCamera;
					button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.authSwitchCamera ? "on" : "off"))));
				}
			)
			.pos(this.width / 2 + 5, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch.tooltip"))))
			.build();
		
		this.optionsList.addSmall(longPressCounterButton, cameraAutoSwitchButton);
		buttonHeight += 24;
		
		Button itemPreferenceButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.item_preferences")),
				button -> {
					this.minecraft.setScreen(new ItemsPreferenceScreen(this));
				}
			)
			.pos(this.width / 2 + 5, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.item_preferences.tooltip"))))
			.build();
		
		Button preferenceWorkButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.preference_work." + ClientConfig.preferenceWork.getSerializedName())),
				button -> {
					ClientConfig.preferenceWork = ClientConfig.preferenceWork.nextEnum();
					button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.preference_work." + ClientConfig.preferenceWork.getSerializedName())));
				}
			)
			.pos(this.width / 2 - 165, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.preference_work.tooltip"))))
			.build();
		
		this.optionsList.addSmall(itemPreferenceButton, preferenceWorkButton);
		buttonHeight += 24;
		
		Button resolveKeyConflictsButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.key_conflict_resolve_scope." + ClientConfig.keyConflictResolveScope.getSerializedName())),
				button -> {
					ClientConfig.keyConflictResolveScope = ClientConfig.keyConflictResolveScope.nextEnum();
					button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.key_conflict_resolve_scope." + ClientConfig.keyConflictResolveScope.getSerializedName())));
				}
			)
			.pos(this.width / 2 - 165, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.key_conflict_resolve_scope.tooltip"))))
			.build();

        Button cameraPerspectiveToggleMode =
                Button.builder(
                                Component.translatable(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode." + ClientConfig.cameraPerspectiveToggleMode.getSerializedName())),
                                button -> {
                                    ClientConfig.cameraPerspectiveToggleMode = ClientConfig.cameraPerspectiveToggleMode.nextEnum();
                                    button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode." + ClientConfig.cameraPerspectiveToggleMode.getSerializedName())));
                                }
                        )
                        .pos(this.width / 2 + 5, this.height / 4 + buttonHeight)
                        .size(160, 20)
                        .tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.camera_perspective_toggle_mode.tooltip"))))
                        .build();
		
		this.optionsList.addSmall(resolveKeyConflictsButton, cameraPerspectiveToggleMode);
		buttonHeight += 24;
		
		this.addWidget(this.optionsList);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		RenderEngine.getInstance().versionNotifier.render(guiGraphics, false);
		this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
	}
}