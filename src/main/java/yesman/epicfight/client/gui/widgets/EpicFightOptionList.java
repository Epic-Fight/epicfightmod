package yesman.epicfight.client.gui.widgets;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class EpicFightOptionList extends ContainerObjectSelectionList<EpicFightOptionList.OptionEntry> {
	public EpicFightOptionList(Minecraft minecraft, int p_94466_, int p_94467_, int p_94468_, int p_94469_, int p_94470_) {
		super(minecraft, p_94466_, p_94467_, p_94468_, p_94469_, p_94470_);
		this.centerListVertically = false;
	}
	
	public int addBig(AbstractWidget button1) {
		return this.addEntry(EpicFightOptionList.OptionEntry.big(this.width, button1));
	}
	
	public void addSmall(AbstractWidget button1, @Nullable AbstractWidget button2) {
		this.addEntry(EpicFightOptionList.OptionEntry.small(this.width, button1, button2));
	}
	
	@Override
	public int getRowWidth() {
		return 400;
	}
	
	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 46;
	}
	
	protected static class OptionEntry extends ContainerObjectSelectionList.Entry<EpicFightOptionList.OptionEntry> {
		final List<AbstractWidget> children;
		
		private OptionEntry(List<AbstractWidget> p_169047_) {
			this.children = ImmutableList.copyOf(p_169047_);
		}
		
		public static EpicFightOptionList.OptionEntry big(int width, AbstractWidget widget) {
			return new EpicFightOptionList.OptionEntry(List.of(widget));
		}
		
		public static EpicFightOptionList.OptionEntry small(int width, AbstractWidget button1, @Nullable AbstractWidget button2) {
			return button2 == null ? new EpicFightOptionList.OptionEntry(List.of(button1)) : new EpicFightOptionList.OptionEntry(List.of(button1, button2));
		}
		
		public void render(GuiGraphics guiGraphics, int x, int y, int p_94499_, int p_94500_, int p_94501_, int mouseX, int mouseY, boolean p_94504_, float partialTicks) {
			this.children.forEach((widget) -> {
				widget.setY(y);
				widget.render(guiGraphics, mouseX, mouseY, partialTicks);
			});
		}
		
		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}
		
		@Override
		public List<? extends NarratableEntry> narratables() {
			return this.children;
		}
	}
}