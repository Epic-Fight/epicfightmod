package yesman.epicfight.epicskins.client.screen;

import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;

public class AwaitIconMessageScreen extends MessageScreen<Object> {
	int tickCount;
	
	public AwaitIconMessageScreen(String title, String message, Screen parentScreen, int width, int height) {
		super(title, message, parentScreen, null, width, height);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100);
		
		guiGraphics.pose().translate(this.width / 2, this.height / 2 + 8, 100);
		
		for (int i = 0; i < 12; i++) {
			int mod = (i * 21 + this.tickCount) % 256;
			int color = ((mod << 16) | (mod << 8) | (mod << 0)) | 254 << 24;
			
			guiGraphics.pose().pushPose();
			guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-30.0F * i));
			guiGraphics.pose().translate(-1, 0, 0);
			guiGraphics.fill(0, 11, 2, 18, color);
			guiGraphics.pose().popPose();
		}
		
		guiGraphics.pose().popPose();
		this.tickCount+=3;
	}
}
