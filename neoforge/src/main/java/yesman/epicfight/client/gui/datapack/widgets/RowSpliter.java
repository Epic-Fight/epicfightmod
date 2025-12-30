package yesman.epicfight.client.gui.datapack.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RowSpliter implements ResizableComponent {
	private int x;
	private int y;
	private int width;
	private int height;
	private int x1;
	private int x2;
	private int y1;
	private int y2;
	private HorizontalSizing horizontalSizing;
	private VerticalSizing verticalSizing;
	
	public RowSpliter(int x1, int x2, int y1, int y2, HorizontalSizing horizontalSizing, VerticalSizing verticalSizing) {
		this.x1 = x1;
		this.x2 = x2;
		this.x1 = x1;
		this.x1 = x1;
		this.horizontalSizing = horizontalSizing;
		this.verticalSizing = verticalSizing;
	}
	
	@Override
	public void _renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		guiGraphics.blit(Screen.HEADER_SEPARATOR, this.x, this.y + this.height / 2, 0.0F, 0.0F, this.width, 2, 32, 2);
	}
	
	@Override
	public void resize(ScreenRectangle screenRectangle) {
		if (this.getHorizontalSizingOption() != null) {
			this.getHorizontalSizingOption().resizeFunction.resize(this, screenRectangle, this.getX1(), this.getX2());
		}
		
		if (this.getVerticalSizingOption() != null) {
			this.getVerticalSizingOption().resizeFunction.resize(this, screenRectangle, this.getY1(), this.getY2());
		}
	}
	
	@Override
	public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
		return false;
	}
	
	@Override
	public boolean mouseReleased(double p_93684_, double p_93685_, int p_93686_) {
		return false;
	}
	
	public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_) {
		return false;
	}
	
	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput p_169152_) {
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
	public HorizontalSizing getHorizontalSizingOption() {
		return this.horizontalSizing;
	}

	@Override
	public VerticalSizing getVerticalSizingOption() {
		return this.verticalSizing;
	}

	@Override
	public void _setActive(boolean active) {
	}
	
	@Override
	public int _getX() {
		return this.x;
	}
	
	@Override
	public int _getY() {
		return this.y;
	}

	@Override
	public int _getWidth() {
		return this.width;
	}

	@Override
	public int _getHeight() {
		return this.height;
	}

	@Override
	public void _setX(int x) {
		this.x = x;
	}

	@Override
	public void _setY(int y) {
		this.y = y;
	}

	@Override
	public void _setWidth(int width) {
		this.width = width;
	}
	
	@Override
	public void _setHeight(int height) {
		this.height = height;
	}
	
	@Override
	public Component _getMessage() {
		return Component.literal("");
	}
}
