package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/// We're refactoring UI codes, use [yesman.epicfight.client.gui.widgets.CheckBox] instead
@Deprecated
public class CheckBox extends AbstractWidget implements DataBindingComponent<Boolean, Boolean> {
	private final Font font;
	private final boolean defaultVal;
	private Consumer<Boolean> responder;
	private Boolean value;
	
	public CheckBox(Font font, int x1, int x2, int y1, int y2, HorizontalSizing horizontal, VerticalSizing vertical, Boolean defaultVal, Component title, Consumer<Boolean> responder) {
		super(x1, y1, x2, y2, title);
		
		this.font = font;
		this.defaultVal = defaultVal == null ? false : defaultVal;
		this.responder = responder;
		
		if (defaultVal != null) {
			this._setValue(defaultVal);
		} else {
			this.value = defaultVal;
		}
		
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.horizontalSizingOption = horizontal;
		this.verticalSizingOption = vertical;
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (this.active && this.visible) {
			if (this.isValidClickButton(button)) {
				boolean flag = this.clicked(x, y);
				
				if (flag) {
					this.onClick(x, y);
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	protected boolean clicked(double x, double y) {
		return this.active && this.visible && x >= (double)this._getX() && y >= (double) this._getY() && x < (double) (this._getX() + this.width) && y < (double) (this._getY() + this.height);
	}
	
	@Override
	public void onClick(double x, double y) {
		this._setValue(this.value == null ? !this.defaultVal : !this.value.booleanValue());
	}
	
	@Override
	public boolean isMouseOver(double x, double y) {
		int rectangleLength = Math.min(this._getWidth(), this._getHeight());
		return this.active && this.visible && x >= (double)this._getX() && y >= (double)this._getY() && x < (double)(this._getX() + rectangleLength) && y < (double)(this._getY() + rectangleLength);
	}
	
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int rectangleLength = Math.min(this._getWidth(), this._getHeight());
		int outlineColor = this.isFocused() ? -1 : this.isActive() ? -6250336 : -12566463;
		
		guiGraphics.fill(this._getX(), this._getY(), this._getX() + rectangleLength, this._getY() + rectangleLength, outlineColor);
		guiGraphics.fill(this._getX() + 1, this._getY() + 1, this._getX() + rectangleLength - 1, this._getY() + rectangleLength - 1, -16777216);
		
		if (this.value == null ? this.defaultVal : this.value.booleanValue()) {
			guiGraphics.fill(this._getX() + 2, this._getY() + 2, this._getX() + rectangleLength - 2, this._getY() + rectangleLength - 2, -1);
		}
		
		int fontColor = this.isActive() ? 16777215 : 4210752;
		
		guiGraphics.drawString(this.font, this._getMessage(), this._getX() + rectangleLength + 4, this._getY() + this.height / 2 - this.font.lineHeight / 2 + 1, fontColor, false);
	}
	
	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementInput) {
		narrationElementInput.add(NarratedElementType.TITLE, this.createNarrationMessage());
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
	public void _setResponder(Consumer<Boolean> responder) {
		this.responder = responder;
	}
	
	@Override
	public Consumer<Boolean> _getResponder() {
		return this.responder;
	}
	
	@Override
	public void _setValue(Boolean value) {
		this.value = value;
		
		if (this.responder != null) {
			this.responder.accept(value == null ? this.defaultVal : value.booleanValue());
		}
	}
	
	@Override
	public Boolean _getValue() {
		return this.value;
	}
	
	@Override
	public void reset() {
		this.value = this.defaultVal;
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
}