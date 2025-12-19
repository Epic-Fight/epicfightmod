package yesman.epicfight.client.gui.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;

import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

public class TPSSettingScreen extends Screen {
    private static final ResourceLocation BUTTON_TEXTURE = EpicFightMod.identifier("textures/gui/widget/camera_settings.png");
	protected final Screen parentScreen;
	protected Button up;
	protected Button down;
	protected Button left;
	protected Button right;
	protected ZoomScroll zoomScroll;
	
	protected TPSSettingScreen(Screen parentScreen) {
		super(Component.translatable(String.format("gui.%s.configuration.tps_mode_setting", EpicFightMod.MODID)));
		
		this.parentScreen = parentScreen;
	}
	
	@Override
	public void init() {
		this.up = new CameraMoveButton(this.width / 2 - 7, this.height / 5 - 7, 14, 14, button -> {
			ClientConfig.cameraVerticalLocation = Math.min(5, ClientConfig.cameraVerticalLocation + 1);
			this.down.active = true;
			if (ClientConfig.cameraVerticalLocation >= 5) this.up.active = false;
		}, CameraMoveButton.Direction.UP);
		
		this.down = new CameraMoveButton(this.width / 2 - 7, this.height / 5 * 4 - 7, 14, 14, button -> {
			ClientConfig.cameraVerticalLocation = Math.max(-2, ClientConfig.cameraVerticalLocation - 1);
			this.up.active = true;
			if (ClientConfig.cameraVerticalLocation <= -2) this.down.active = false;
		}, CameraMoveButton.Direction.DOWN);
		
		this.left = new CameraMoveButton(this.width / 5 - 7, this.height / 2 - 7, 14, 14, button -> {
			ClientConfig.cameraHorizontalLocation = Math.min(10, ClientConfig.cameraHorizontalLocation + 1);
			this.right.active = true;
			if (ClientConfig.cameraHorizontalLocation >= 10) this.left.active = false;
		}, CameraMoveButton.Direction.LEFT);
		
		this.right = new CameraMoveButton(this.width / 5 * 4 - 7, this.height / 2 - 7, 14, 14, button -> {
			ClientConfig.cameraHorizontalLocation = Math.max(-10, ClientConfig.cameraHorizontalLocation - 1);
			this.left.active = true;
			if (ClientConfig.cameraHorizontalLocation <= -10) this.right.active = false;
		}, CameraMoveButton.Direction.RIGHT);
		
		this.zoomScroll = new ZoomScroll(this.width / 2 + 24, this.height / 2 - 26, ClientConfig.cameraZoom - 3);
		
		if (ClientConfig.cameraVerticalLocation >= 5) this.up.active = false;
		if (ClientConfig.cameraVerticalLocation <= -2) this.down.active = false;
		if (ClientConfig.cameraHorizontalLocation >= 10) this.left.active = false;
		if (ClientConfig.cameraHorizontalLocation <= -10) this.right.active = false;
		
		this.addRenderableWidget(this.up);
		this.addRenderableWidget(this.down);
		this.addRenderableWidget(this.left);
		this.addRenderableWidget(this.right);
		this.addRenderableWidget(this.zoomScroll);
		
		this.minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(guiGraphics);
		
		guiGraphics.drawString(this.font, this.title, 6, 6, 16777215);
		guiGraphics.drawString(this.font, Component.literal("Exit"), this.width - 24, 6, (mouseX > this.width - 24 && mouseY < 16) ? 7368816 : 16777215);
		
		super.render(guiGraphics, mouseX, mouseY, partialTick);
	}
	
	@Override
	public void onClose() {
		ClientConfig.saveChanges();
		this.minecraft.setScreen(this.parentScreen);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX > this.width - 24 && mouseY < 16) {
			ClientConfig.saveChanges();
			this.minecraft.setScreen(null);
			return true;
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (!super.mouseScrolled(pMouseX, pMouseY, pDelta)) {
			this.zoomScroll.setScrollPosition(this.zoomScroll.scrollPosition - pDelta);
			return false;
		}
		
		return true;
	}
	
	private class CameraMoveButton extends Button {
		private final Direction direction;
		
		protected CameraMoveButton(int x, int y, int width, int height, Button.OnPress onPress, Direction direction) {
			super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
			this.direction = direction;
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			guiGraphics.pose().pushPose();
			
			int u = this.active ? 0 : 16;
			int v = (this.active && this.isHovered()) ? 16 : 0;
			
			guiGraphics.pose().translate(this.getX(), this.getY(), 0.0F);
			
			guiGraphics.pose().translate(8.0F, 8.0F, 0.0F);
			
			switch (this.direction) {
			case UP -> { guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(90.0F)); }
			case DOWN -> { guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(270.0F)); }
			case RIGHT -> { guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(180.0F)); }
			}
			
			guiGraphics.pose().translate(-8.0F, -8.0F, 0.0F);
			
			guiGraphics.blit(BUTTON_TEXTURE, 0, 0, 0, u, v, 16, 16, 64, 64);
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			
			guiGraphics.pose().popPose();
		}
		
		public enum Direction {
			UP, DOWN, LEFT, RIGHT
		}
	}
	
	private class ZoomScroll extends AbstractWidget {
		private double scrollPosition;
		
		public ZoomScroll(int x, int y, int initPos) {
			super(x, y, 12, 64, Component.empty());
			this.scrollPosition = initPos;
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			guiGraphics.blit(BUTTON_TEXTURE, this.getX(), this.getY(), 0, 52, 0, this.width, this.height, 64, 64);
			int scrollCoord = (int)this.scrollPosition * 6;
			guiGraphics.fill(this.getX() + 3, this.getY() + scrollCoord + 3, this.getX() + 9, this.getY() + scrollCoord + 7, -1);
		}
		
		@Override
		public void onClick(double mouseX, double mouseY) {
			if (this.getX() + 3 <= mouseX && mouseX <= this.getX() + 9 && this.getY() + 1 <= mouseY && mouseY <= this.getY() + 63) {
				this.setScrollBaseOnYPressed(mouseY);
			}
		}
		
		@Override
		protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
			if (this.getX() + 3 <= mouseX && mouseX <= this.getX() + 9 && this.getY() + 1 <= mouseY + dragY && mouseY + dragY <= this.getY() + 63) {
				this.setScrollBaseOnYPressed(mouseY + dragY);
			}
		}
		
		private void setScrollBaseOnYPressed(double y) {
			double relativeY = y - this.getY()+1;
			this.setScrollPosition(relativeY / 6.0D);
		}
		
		private void setScrollPosition(double pos) {
			this.scrollPosition = (int)Mth.clamp(pos, 0.0D, 7.0D);
			ClientConfig.cameraZoom = (int)this.scrollPosition + 3;
		}
		
		@Override
		protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		}
	}
}
