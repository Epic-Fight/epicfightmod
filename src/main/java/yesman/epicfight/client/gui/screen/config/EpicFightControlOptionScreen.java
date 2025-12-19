package yesman.epicfight.client.gui.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.gui.widgets.RewindableButton;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightControlOptionScreen extends EpicFightOptionSubScreen {
	private EpicFightOptionList optionsList;
	
	public EpicFightControlOptionScreen(Screen parentScreen) {
		super(parentScreen, Component.translatable(EpicFightMod.format("gui.%s.control_options")));
	}
	
	@Override
	protected void init() {
		super.init();
		
		this.optionsList = new EpicFightOptionList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
		int buttonHeight = -32;
		
		Button longPressCounterButton =
			new RewindableButton(
				this.width / 2 - 165,
				this.height / 4 + buttonHeight,
				160,
				20,
				Component.translatable(
					EpicFightMod.format("gui.%s.long_press_counter"),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
				),
				button -> {
					ClientConfig.longPressCounter = MathUtils.wrapClamp(++ClientConfig.longPressCounter, 1, 10);
					
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.long_press_counter"),
							ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
						)
					);
				},
				button -> {
					ClientConfig.longPressCounter = MathUtils.wrapClamp(--ClientConfig.longPressCounter, 1, 10);
					
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.long_press_counter"),
							ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.longPressCounter)
						)
					);
				}
			);
		
		longPressCounterButton.setTooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.long_press_counter.tooltip"))));
		
		Button cameraAutoSwitchButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.autoSwitchCamera ? "on" : "off"))),
				button -> {
					ClientConfig.autoSwitchCamera = !ClientConfig.autoSwitchCamera;
					button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch." + (ClientConfig.autoSwitchCamera ? "on" : "off"))));
				}
			)
			.pos(this.width / 2 + 5, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.camera_auto_switch.tooltip"))))
			.build();
		
		this.optionsList.addSmall(longPressCounterButton, cameraAutoSwitchButton);
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
		
		Button enableLockOnQuickShiftButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.lock_on_quick_shift." + (ClientConfig.lockOnQuickShift ? "on" : "off"))),
				button -> {
					ClientConfig.lockOnQuickShift = !ClientConfig.lockOnQuickShift;
					button.setMessage(Component.translatable(EpicFightMod.format("gui.%s.lock_on_quick_shift." + (ClientConfig.lockOnQuickShift ? "on" : "off"))));
				}
			)
			.pos(this.width / 2 - 165, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.lock_on_quick_shift.tooltip"))))
			.build();
		
		Button lockOnRangeButton =
			new RewindableButton(
				this.width / 2 + 5,
				this.height / 4 + buttonHeight,
				160,
				20,
				Component.translatable(
					EpicFightMod.format("gui.%s.lock_on_range"),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.lockOnRange)
				),
				button -> {
					ClientConfig.lockOnRange = MathUtils.wrapClamp(++ClientConfig.lockOnRange, 5, 25);
					
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.lock_on_range"),
							ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.lockOnRange)
						)
					);
				},
				button -> {
					ClientConfig.lockOnRange = MathUtils.wrapClamp(--ClientConfig.lockOnRange, 5, 25);
					
					button.setMessage(
						Component.translatable(
							EpicFightMod.format("gui.%s.lock_on_range"),
							ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ClientConfig.lockOnRange)
						)
					);
				}
			);
		
		lockOnRangeButton.setTooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.lock_on_range.tooltip"))));
		
		this.optionsList.addSmall(enableLockOnQuickShiftButton, lockOnRangeButton);
		buttonHeight += 24;
		
		Button itemPreferenceButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s.item_preferences")),
				button -> {
					this.minecraft.setScreen(new ItemsPreferenceScreen(this));
				}
			)
			.pos(this.width / 2 - 165, this.height / 4 + buttonHeight)
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
			.pos(this.width / 2 + 5, this.height / 4 + buttonHeight)
			.size(160, 20)
			.tooltip(Tooltip.create(Component.translatable(EpicFightMod.format("gui.%s.preference_work.tooltip"))))
			.build();
		
		this.optionsList.addSmall(itemPreferenceButton, preferenceWorkButton);
		buttonHeight += 24;
		
		this.addWidget(this.optionsList);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		ClientEngine.getInstance().renderEngine.versionNotifier.render(guiGraphics, false);
		this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
	}
}