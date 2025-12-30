package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/// We're refactoring UI codes, use [yesman.epicfight.client.gui.widgets.AnchoredEditBox] instead
@Deprecated
public class ResizableEditBox extends EditBox implements DataBindingComponent<String, String> {
	public ResizableEditBox(Font font, int x1, int x2, int y1, int y2, Component title, HorizontalSizing horizontalSizingOption, VerticalSizing verticalSizingOption) {
		super(font, x1, y1, x2, y2, title);
		
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.horizontalSizingOption = horizontalSizingOption;
		this.verticalSizingOption = verticalSizingOption;
	}
	
	/*******************************************************************
	 * @ResizableComponent variables                                   *
	 *******************************************************************/
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private final HorizontalSizing horizontalSizingOption;
	private final VerticalSizing verticalSizingOption;
	
	@Override
	public void setX1(int x1) {
		this.x1 = x1;
	}

	@Override
	public void setX2(int x2) {
		this.x2 = x2;
	}

	@Override
	public void setY1(int y1) {
		this.y1 = y1;
	}

	@Override
	public void setY2(int y2) {
		this.y2 = y2;
	}
	
	@Override
	public int getX1() {
		return this.x1;
	}
	
	@Override
	public int getX2() {
		return this.x2;
	}
	
	@Override
	public int getY1() {
		return this.y1;
	}
	
	@Override
	public int getY2() {
		return this.y2;
	}
	
	@Override
	public HorizontalSizing getHorizontalSizingOption() {
		return this.horizontalSizingOption;
	}

	@Override
	public VerticalSizing getVerticalSizingOption() {
		return this.verticalSizingOption;
	}
	
	@Override
	public void _setActive(boolean active) {
		this.active = active;
	}

	@Override
	public void reset() {
		Consumer<String> responder = this.responder;
		
		this.setResponder(null);
		this.setValue("");
		this.setResponder(responder);
	}

	@Override
	public int _getX() {
		return this.getX();
	}

	@Override
	public int _getY() {
		return this.getY();
	}

	@Override
	public int _getWidth() {
		return this.getWidth();
	}

	@Override
	public int _getHeight() {
		return this.getHeight();
	}

	@Override
	public void _setX(int x) {
		this.setX(x);
	}

	@Override
	public void _setY(int y) {
		this.setY(y);
	}

	@Override
	public void _setWidth(int width) {
		this.setWidth(width);
	}

	@Override
	public void _setHeight(int height) {
		this.setHeight(height);
	}

	@Override
	public Component _getMessage() {
		return this.getMessage();
	}
	
	@Override
	public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public String _getValue() {
		return this.getValue();
	}

	@Override
	public void _setValue(String value) {
		if (value == null) {
			this.setValue("");
		} else {
			this.setValue(value);
		}
	}

	@Override
	public void _setResponder(Consumer<String> responder) {
		this.setResponder(responder);
	}
	
	@Override
	public Consumer<String> _getResponder() {
		return this.responder;
	}
}
