package yesman.epicfight.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RewindableButton extends Button {
	protected final Button.OnPress onRewindPress;
	
	public RewindableButton(int x, int y, int width, int height, Component title, OnPress pressedAction, OnPress rewindPressedAction) {
		super(x, y, width, height, title, pressedAction, Button.DEFAULT_NARRATION);
		this.onRewindPress = rewindPressedAction;
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return button == 0 || button == 1;
	}
	
	public void onClick(double mouseX, double mouseY, int button) {
		if (button == 0) {
			super.onClick(mouseX, mouseY);
		} else {
			this.onRewindPress.onPress(this);
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.active && this.visible) {
			if (this.isValidClickButton(button)) {
				boolean flag = this.clicked(mouseX, mouseY);
				if (flag) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					this.onClick(mouseX, mouseY, button);
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
}