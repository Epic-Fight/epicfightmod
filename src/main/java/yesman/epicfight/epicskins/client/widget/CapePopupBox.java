package yesman.epicfight.epicskins.client.widget;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.epicskins.user.Cosmetic;

import java.util.function.Consumer;
import java.util.function.Function;

public class CapePopupBox extends PopupBox<Cosmetic> {
	final Runnable onPress;
	
	public CapePopupBox(Screen owner, Font font, int x1, int x2, int y1, int y2, HorizontalSizing horizontal, VerticalSizing vertical, Component title, Function<Cosmetic, String> displayStringMapper, Runnable onPress, Consumer<Pair<String, Cosmetic>> responder) {
		super(owner, font, x1, x2, y1, y2, horizontal, vertical, title, displayStringMapper, responder);
		
		this.onPress = onPress;
	}
	
	@Override
	public void onClick(double x, double y) {
		if (this.clickedPopupButton(x, y)) {
			this.onPress.run();
		}
	}
}
