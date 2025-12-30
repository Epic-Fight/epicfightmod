package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

import java.util.function.Supplier;

/// We're refactoring UI codes, use [yesman.epicfight.client.gui.widgets.SubscreenButton] instead
@Deprecated
public class SubScreenOpenButton extends ResizableButton {
	protected final Supplier<Screen> subScreenProvider;
	
	public SubScreenOpenButton(Builder builder) {
		super(builder);
		
		this.subScreenProvider = builder.subScreenProvider;
	}
	
	@Override
	public void onPress() {
		Minecraft.getInstance().setScreen(this.subScreenProvider.get());
	}
	
	public static SubScreenOpenButton.Builder builder() {
		return new SubScreenOpenButton.Builder();
	}
	
	public static class Builder extends ResizableButton.Builder {
		Supplier<Screen> subScreenProvider;
		
		public Builder() {
			super(CommonComponents.ELLIPSIS, null);
		}
		
		public SubScreenOpenButton.Builder subScreen(Supplier<Screen> subScreenProvider) {
			this.subScreenProvider = subScreenProvider;
			return this;
		}
		
		@Override
		public SubScreenOpenButton build() {
			return new SubScreenOpenButton (this);
		}
	}
}
