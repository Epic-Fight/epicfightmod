package yesman.epicfight.client.gui.screen;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.client.forgeevent.AttributeIconRegisterEvent;
import yesman.epicfight.api.client.forgeevent.WeaponCategoryIconRegisterEvent;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPChangeSkill;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;

public class SkillBookScreen extends Screen {
	private static final Map<WeaponCategory, ItemStack> WEAPON_CATEGORY_ICONS = Maps.newHashMap();
	private static final Map<Attribute, TextureInfo> ATTRIBUTE_ICONS = Maps.newHashMap();
    private static final ResourceLocation SKILLBOOK_BACKGROUND = EpicFightMod.identifier("textures/gui/screen/skillbook.png");
	
	public static final TextureInfo HEALTH_TEXTURE_INFO = new TextureInfo(SKILLBOOK_BACKGROUND, 22, 205, 10, 10);
	public static final TextureInfo STAMINA_TEXTURE_INFO = new TextureInfo(SKILLBOOK_BACKGROUND, 32, 205, 10, 10);
	public static final TextureInfo COOLDOWN_TEXTURE_INFO = new TextureInfo(SKILLBOOK_BACKGROUND, 42, 205, 10, 10);
	
	public static void registerIconItems() {
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.NOT_WEAPON, new ItemStack(Items.AIR));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.AXE, new ItemStack(Items.IRON_AXE));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.FIST, new ItemStack(EpicFightItems.GLOVE.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.GREATSWORD, new ItemStack(EpicFightItems.IRON_GREATSWORD.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.HOE, new ItemStack(Items.IRON_HOE));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.PICKAXE, new ItemStack(Items.IRON_PICKAXE));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.SHOVEL, new ItemStack(Items.IRON_SHOVEL));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.SWORD, new ItemStack(Items.IRON_SWORD));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.UCHIGATANA, new ItemStack(EpicFightItems.UCHIGATANA.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.SPEAR, new ItemStack(EpicFightItems.IRON_SPEAR.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.TACHI, new ItemStack(EpicFightItems.IRON_TACHI.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.TRIDENT, new ItemStack(Items.TRIDENT));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.LONGSWORD, new ItemStack(EpicFightItems.IRON_LONGSWORD.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.DAGGER, new ItemStack(EpicFightItems.IRON_DAGGER.get()));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.SHIELD, new ItemStack(Items.SHIELD));
		WEAPON_CATEGORY_ICONS.put(WeaponCategories.BOW, new ItemStack(Items.BOW));
		
		ATTRIBUTE_ICONS.put(Attributes.MAX_HEALTH, new TextureInfo(SKILLBOOK_BACKGROUND, 22, 195, 10, 10));
		ATTRIBUTE_ICONS.put(EpicFightAttributes.MAX_STAMINA.get(), new TextureInfo(SKILLBOOK_BACKGROUND, 42, 195, 10, 10));
		ATTRIBUTE_ICONS.put(Attributes.ATTACK_DAMAGE, new TextureInfo(SKILLBOOK_BACKGROUND, 52, 195, 10, 10));
		ATTRIBUTE_ICONS.put(EpicFightAttributes.STAMINA_REGEN.get(), new TextureInfo(SKILLBOOK_BACKGROUND, 62, 195, 10, 10));
		ATTRIBUTE_ICONS.put(Attributes.ATTACK_SPEED, new TextureInfo(SKILLBOOK_BACKGROUND, 72, 195, 10, 10));
		
		WeaponCategoryIconRegisterEvent weaponCategoryIconRegisterEvent = new WeaponCategoryIconRegisterEvent(WEAPON_CATEGORY_ICONS);
		ModLoader.get().postEvent(weaponCategoryIconRegisterEvent);
		
		AttributeIconRegisterEvent attributeIconRegisterEvent = new AttributeIconRegisterEvent(ATTRIBUTE_ICONS);
		ModLoader.get().postEvent(attributeIconRegisterEvent);
	}
	
	protected final Player opener;
	protected final LocalPlayerPatch playerpatch;
	protected final Skill skill;
	protected final Screen parentScreen;
	protected final SkillTooltipList skillTooltipList;
	protected final AvailableItemsList availableWeaponCategoryList;
	protected final AttributeIconList consumptionList;
	protected final AttributeIconList providingAttributesList;
	protected final InteractionHand hand;
	
	private double customScale;

    private Button learnButton;

    public Button getLearnButton() {
        return learnButton;
    }
	
	public SkillBookScreen(Player opener, ItemStack stack, @Nullable InteractionHand hand) {
		this(opener, SkillBookItem.getContainSkill(stack), hand, null);
	}
	
	public SkillBookScreen(Player opener, Skill skill, @Nullable InteractionHand hand, @Nullable Screen parentScreen) {
		super(Component.empty());
		
		this.minecraft = Minecraft.getInstance();
		this.font = Minecraft.getInstance().font;
		
		this.opener = opener;
		this.playerpatch = EpicFightCapabilities.getEntityPatch(this.opener, LocalPlayerPatch.class);
		this.skill = skill;
		this.hand = hand;
		this.parentScreen = parentScreen;
		this.skillTooltipList = new SkillTooltipList(Minecraft.getInstance(), 0, 0, 0 ,0, Minecraft.getInstance().font.lineHeight);
		this.availableWeaponCategoryList = new AvailableItemsList(0, 0);
		this.consumptionList = new AttributeIconList(Minecraft.getInstance(), 0, 0, 100, 100, 16);
		this.providingAttributesList = new AttributeIconList(Minecraft.getInstance(), 0, 0, 100, 100, 16);
		
		List<FormattedCharSequence> list = Minecraft.getInstance().font.split(Component.translatable(this.skill.getTranslationKey() + ".tooltip", this.skill.getTooltipArgsOfScreen(Lists.newArrayList()).toArray(new Object[0])), 148);
		list.forEach(this.skillTooltipList::add);
		
		if (this.skill.getAvailableWeaponCategories() != null) {
			this.skill.getAvailableWeaponCategories().forEach(this.availableWeaponCategoryList::addWeaponCategory);
		}
		
		if (!this.skill.getCustomConsumptionTooltips(this.consumptionList)) {
			this.consumptionList.children().clear();
			
			switch (this.skill.getResourceType()) {
				case WEAPON_CHARGE -> {
					
				}
				case COOLDOWN -> {
					this.consumptionList.add(
						Component.translatable(EpicFightMod.format("attribute.name.%s.cooldown.consume.tooltip")),
						Component.translatable(
							EpicFightMod.format("attribute.name.%s.cooldown.consume"),
							String.format("%.1f", this.skill.getConsumption())
						),
						COOLDOWN_TEXTURE_INFO
					);
				}
				case STAMINA -> {
					this.consumptionList.add(
						Component.translatable(EpicFightMod.format("attribute.name.%s.stamina.consume.tooltip")),
						Component.translatable(
							EpicFightMod.format("attribute.name.%s.stamina.consume"),
							String.format("%.1f", this.skill.getConsumption())
						),
						STAMINA_TEXTURE_INFO
					);
				}
				case HEALTH -> {
					this.consumptionList.add(
						Component.translatable(EpicFightMod.format("attribute.name.%s.health.consume.tooltip")),
						Component.translatable(EpicFightMod.format("attribute.name.%s.health.consume")),
						HEALTH_TEXTURE_INFO
					);
				}
				default -> {
				}
			}
		}
		
		this.skill.getModfierEntry().forEach((entry) -> {
			this.providingAttributesList.add(entry.getKey(), entry.getValue(), ATTRIBUTE_ICONS.get(entry.getKey()));
		});
	}
	
	@Override
	protected void init() {
		Optional<SkillContainer> skillContainer = this.playerpatch.getSkillContainerFor(this.skill);
		Optional<SkillContainer> priorSkillContainer = this.skill == null ? null : this.playerpatch.getSkillContainerFor(this.skill.getPriorSkill());
		
		boolean isUsing = skillContainer.isPresent();
		boolean meetsCondition = this.skill == null ? false : this.skill.getPriorSkill() == null || priorSkillContainer.isPresent();
		Component tooltip = CommonComponents.EMPTY;
		
		if (!isUsing) {
			if (meetsCondition) {
				if (skillContainer.isPresent()) {
					tooltip = Component.translatable(EpicFightMod.format("gui.%s.replace"), Component.translatable(this.skill.getTranslationKey()).getString());
				}
			} else {
				tooltip = Component.translatable(EpicFightMod.format("gui.%s.require_to_learn"), Component.translatable(this.skill.getPriorSkill().getTranslationKey()).getString());
			}
		}
		
		Window window = Minecraft.getInstance().getWindow();
		
		if (window.getGuiScaledHeight() < 270 && window.getGuiScale() > 1.0F) {
			this.customScale = window.getGuiScale() - 1.0F;
			this.width = (int)(window.getWidth() / this.customScale);
			this.height = (int)(window.getHeight() / this.customScale);
		} else {
			this.customScale = window.getGuiScale();
		}
		
		learnButton =
			Button.builder(
				Component.translatable(EpicFightMod.format("gui.%s") + (isUsing ? ".applied" : meetsCondition ? ".learn" : ".unusable")),
				button -> {
					Set<SkillContainer> skillContainers = this.playerpatch.getSkillCapability().getSkillContainersFor(this.skill.getCategory());
					
					if (skillContainers.size() == 1) {
						this.acquireSkillTo(skillContainers.iterator().next());
					} else {
						SlotSelectScreen slotSelectScreen = new SlotSelectScreen(skillContainers, this);
						this.minecraft.setScreen(slotSelectScreen);
					}
				}
			)
			.bounds(this.width / 2 + 54, this.height / 2 + 90, 67, 21)
			.tooltip(Tooltip.create(tooltip, null))
			.build(LearnButton::new);
		
		if (isUsing || !meetsCondition) {
			learnButton.active = false;
		}
		
		if (this.hand == null) {
			learnButton.visible = false;
		}
		
		this.availableWeaponCategoryList.setX(this.width / 2 + 21);
		this.availableWeaponCategoryList.setY(this.height / 2 + 50);
		
		this.skillTooltipList.updateSize(210, 400, this.height / 2 - 100, (this.height + (this.availableWeaponCategoryList.availableCategories.size() == 0 ? 150 : 80)) / 2);
		this.skillTooltipList.setLeftPos(this.width / 2 - 40);
		
		int consumptionEndPos = this.height / 2 + 20 + (20 * Math.min(2, this.consumptionList.children().size()));
		
		this.consumptionList.updateSize(140, 300, this.height / 2 + 20, consumptionEndPos);
		this.consumptionList.setLeftPos(this.width / 2 - 160);
		
		this.providingAttributesList.updateSize(140, 300, consumptionEndPos, consumptionEndPos + 60);
		this.providingAttributesList.setLeftPos(this.width / 2 - 160);
		
		this.addRenderableWidget(learnButton);
		this.addRenderableWidget(this.skillTooltipList);
		this.addRenderableWidget(this.availableWeaponCategoryList);
		
		if (this.consumptionList.children().size() > 0) {
			this.addRenderableWidget(this.consumptionList);
		}
		
		if (this.providingAttributesList.children().size() > 0) {
			this.addRenderableWidget(this.providingAttributesList);
		}
	}
	
	protected void acquireSkillTo(SkillContainer skillContainer) {
		skillContainer.setSkill(this.skill);
		this.playerpatch.getSkillCapability().addLearnedSkill(this.skill);
		int i = this.hand == InteractionHand.MAIN_HAND ? this.opener.getInventory().selected : 40;
		EpicFightNetworkManager.sendToServer(new CPChangeSkill(skillContainer.getSlot(), i, this.skill));
		this.minecraft.setScreen(null);
	}
	
	protected boolean consumesItem() {
		return true;
	}
	
	@Override
	public void onClose() {
		if (this.parentScreen != null) {
			this.minecraft.setScreen(this.parentScreen);
		} else {
			super.onClose();
		}
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int button) {
		Window window = Minecraft.getInstance().getWindow();
		return super.mouseClicked((int)(x * window.getGuiScale() / this.customScale), (int)(y * window.getGuiScale() / this.customScale), button);
	}
	
	@Override
	public boolean mouseReleased(double x, double y, int button) {
		Window window = Minecraft.getInstance().getWindow();
		return super.mouseReleased((int)(x * window.getGuiScale() / this.customScale), (int)(y * window.getGuiScale() / this.customScale), button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		Window window = Minecraft.getInstance().getWindow();
		return super.mouseDragged((int)(mouseX * window.getGuiScale() / this.customScale), (int)(mouseY * window.getGuiScale() / this.customScale), button, dx, dy);
	}
	
	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		Window window = Minecraft.getInstance().getWindow();
		return super.mouseScrolled((int)(pMouseX * window.getGuiScale() / this.customScale), (int)(pMouseY * window.getGuiScale() / this.customScale), pDelta);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.render(guiGraphics, mouseX, mouseY, partialTicks, false);
	}
	
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, boolean asBackground) {
		guiGraphics.pose().pushPose();
		
		Window window = Minecraft.getInstance().getWindow();
		double originalScale = window.getGuiScale();
		
		if (originalScale != this.customScale) {
			window.setGuiScale(this.customScale);
			
			//Fix: expand extra far plane distance
			Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
			RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		}
		
		if (!asBackground) {
			this.renderBackground(guiGraphics);
		}
		
		int posX = (this.width - 284) / 2;
		int posY = (this.height - 165) / 2;
		
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		
		guiGraphics.blit(SKILLBOOK_BACKGROUND, this.width / 2 - 192, this.height / 2 - 140, 384, 279, 0, 0, 256, 186, 256, 256);
		
		int iconStartX = 106;
		int iconStartY = 211;
		
		if (this.skill.getCategory() == SkillCategories.DODGE) {
			iconStartX += 9;
		} else if (this.skill.getCategory() == SkillCategories.GUARD) {
			iconStartX += 18;
		} else if (this.skill.getCategory() == SkillCategories.IDENTITY) {
			iconStartX += 27;
		} else if (this.skill.getCategory() == SkillCategories.MOVER) {
			iconStartX += 36;
		} else if (this.skill.getCategory() == SkillCategories.PASSIVE) {
			iconStartX += 45;
		}
		
		// skill category icon left
		guiGraphics.blit(SKILLBOOK_BACKGROUND, this.width / 2 - 160, this.height / 2 - 73, 12, 12, iconStartX, iconStartY, 9, 9, 256, 256);
		
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(this.width / 2 - 16, this.height / 2 - 73, 0.0D);
		guiGraphics.pose().scale(-1.0F, 1.0F, 1.0F);
		RenderSystem.disableCull();
		// skill category icon right
		guiGraphics.blit(SKILLBOOK_BACKGROUND, 0, 0, 12, 12, iconStartX, iconStartY, 9, 9, 256, 256);
		RenderSystem.enableCull();
		guiGraphics.pose().popPose();
		
		RenderSystem.enableBlend();
		guiGraphics.blit(this.skill.getSkillTexture(), this.width / 2 - 122, this.height / 2 - 99, 68, 68, 0, 0, 128, 128, 128, 128);
		RenderSystem.disableBlend();
		
		String translationName = this.skill.getTranslationKey();
		String skillName = Component.translatable(translationName).getString();
		int width = this.font.width(skillName);
		guiGraphics.drawString(this.font, skillName, posX + 56 - width / 2, posY + 75, 0, false);
		
		String skillCategory = String.format("(%s)", Component.translatable(EpicFightMod.format("skill.%s") + "." + this.skill.getCategory().toString().toLowerCase() + ".category").getString());
		width = this.font.width(skillCategory);
		
		guiGraphics.drawString(this.font, skillCategory, posX + 56 - width / 2, posY + 90, 0, false);
		
		super.render(guiGraphics, (int)(mouseX * originalScale / this.customScale), (int)(mouseY * originalScale / this.customScale), partialTicks);
		
		if (asBackground) {
			this.renderBackground(guiGraphics);
		}
		
		guiGraphics.pose().popPose();
		
		// Recover the original projection matrix
		if (originalScale != this.customScale) {
			window.setGuiScale(originalScale);
			Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getWidth() / window.getGuiScale()), (float)((double)window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, net.minecraftforge.client.ForgeHooksClient.getGuiFarPlane());
	        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		}
	}
	
	private class SkillTooltipList extends ObjectSelectionList<SkillTooltipList.TooltipLine> {
		public SkillTooltipList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
			super(minecraft, width, height, y0, y1, itemHeight);
			
			this.setRenderBackground(false);
			this.setRenderHeader(false, 0);
			this.setRenderTopAndBottom(false);
		}
		
		public void add(FormattedCharSequence tooltip) {
			this.addEntry(new TooltipLine(tooltip));
		}
		
		@Override
		protected int getScrollbarPosition() {
			return this.x1 - 6;
		}
		
		private class TooltipLine extends ObjectSelectionList.Entry<SkillTooltipList.TooltipLine> {
			private final FormattedCharSequence tooltip;
			
			private TooltipLine(FormattedCharSequence string) {
				this.tooltip = string;
			}
			
			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
				guiGraphics.drawString(SkillBookScreen.this.font, this.tooltip, left + 59, top, 0, false);
			}
			
			@Override
			public Component getNarration() {
				return Component.empty();
			}
		}
	}
	
	private class AvailableItemsList extends AbstractWidget {
		private static final float ICON_LENGTH = 21.25F;
		private final List<WeaponCategory> availableCategories = Lists.newArrayList();
		private int startIdx;
		private int size;
		
		private AvailableItemsList(int x, int y) {
			super(x, y, 0, 0, Component.translatable(EpicFightMod.format("gui.%s.available_weapon_types")));
			
			this.width = 0;
			this.height = 28;
		}
		
		public void addWeaponCategory(WeaponCategory weaopnCategory) {
			this.availableCategories.add(weaopnCategory);
			this.width += ICON_LENGTH;
			this.size++;
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			if (this.availableCategories.size() == 0) {
				return;
			}
			
			float x = this.getX() + 3;
			int y = this.getY() + 13;
			boolean updatedTooltip = false;
			
			guiGraphics.blit(SKILLBOOK_BACKGROUND, SkillBookScreen.this.width / 2 + 20, SkillBookScreen.this.height / 2 + 60, 130, 22, 0, 223, 104, 17, 256, 256);
			
			int displayedCount = 0;
			
			for (int i = this.startIdx; i < this.size; i++) {
				if (displayedCount > 5) {
					break;
				}
				
				WeaponCategory category = this.availableCategories.get(i);
				
				if (WEAPON_CATEGORY_ICONS.containsKey(category)) {
					guiGraphics.renderItem(WEAPON_CATEGORY_ICONS.get(category), (int)x, y);
				}
				
				if (mouseX >= x && mouseX <= x + ICON_LENGTH && mouseY >= y && mouseY <= y + ICON_LENGTH) {
					this.setTooltip(Tooltip.create(Component.translatable(EpicFightMod.format("%s.weapon_category.") + category.toString().toLowerCase(Locale.ROOT))));
					updatedTooltip = true;
				}
				
				x += ICON_LENGTH;
				displayedCount++;
			}
			
			if (!updatedTooltip) {
				this.setTooltip(null);
			}
			
			if (this.availableCategories.size() > 5) {
				int x1 = SkillBookScreen.this.width / 2 + 20;
				int y1 = SkillBookScreen.this.height / 2 + 83;
				int scrollSize = (int)(130 * (6.0D / this.size));
				int scrollStart = this.startIdx * (130 - scrollSize) + 1;
				
				guiGraphics.fill(x1, y1, x1 + 130, y1 + 4, 0xFFE3D6B6);
				guiGraphics.fill(x1 + scrollStart, y1 + 1, x1 + scrollStart + scrollSize - 2, y1 + 3, 0xFFC2B79C);
			}
			
			guiGraphics.drawString(SkillBookScreen.this.font, this.getMessage(), this.getX(), this.getY(), 0, false);
		}
		
		@Override
		public boolean mouseScrolled(double x, double y, double direction) {
			if (this.isMouseOver(x, y) && this.size > 6) {
				this.startIdx = Mth.clamp((int)(this.startIdx - direction), 0, this.size - 6);
				return true;
			}
			
			return false;
		}
		
		@Override
		protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
		}
	}
	
	public class AttributeIconList extends ContainerObjectSelectionList<AttributeIconList.ProvidingAttributeEntry> {
		public AttributeIconList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight) {
			super(minecraft, width, height, y0, y1, itemHeight);
			
			this.setRenderBackground(false);
			this.setRenderHeader(false, 0);
			this.setRenderTopAndBottom(false);
		}
		
		public void add(Attribute attribute, AttributeModifier ability, TextureInfo textureInfo) {
			this.addEntry(new ProvidingAttributeEntry(attribute, ability, textureInfo));
		}
		
		public void add(Component tooltip, Component descriptor, TextureInfo textureInfo) {
			this.addEntry(new ProvidingAttributeEntry(tooltip, descriptor, textureInfo));
		}
		
		@Override
		protected int getScrollbarPosition() {
			return this.x1 - 6;
		}
		
		@Override
		public int getRowLeft() {
			return this.x0 + 2;
		}
		
		private class ProvidingAttributeEntry extends ContainerObjectSelectionList.Entry<AttributeIconList.ProvidingAttributeEntry> {
			private List<AbstractWidget> icons = Lists.newArrayList();
			
			private ProvidingAttributeEntry(Attribute attribute, AttributeModifier ability, TextureInfo textureInfo) {
				String amountString = "";
				String operator = "+";
				double amount = ability.getAmount();
				
				if (amount < 0) {
					operator = "-";
					amount = Math.abs(amount);
				}
				
				switch (ability.getOperation()) {
				case ADDITION -> amountString = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount);
				case MULTIPLY_BASE, MULTIPLY_TOTAL -> amountString = ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(amount * 100.0D) + "%";
				}
				
				this.icons.add(new AttributeIcon(0, 0, 12, 12, attribute, textureInfo));
				
				Static abilityString = new Static(SkillBookScreen.this, 0, 140, 0, 15, null, null, Component.literal(operator + amountString + " " + Component.translatable(attribute.getDescriptionId()).getString()));
				abilityString.setColor(0, 0, 0);
				
				this.icons.add(abilityString);
			}
			
			private ProvidingAttributeEntry(Component customTooltip, Component customDescription, TextureInfo textureInfo) {
				this.icons.add(new AttributeIcon(0, 0, 12, 12, customTooltip, textureInfo));
				
				Static abilityString = new Static(SkillBookScreen.this, 0, 140, 0, 15, null, null, customDescription);
				abilityString.setColor(0, 0, 0);
				
				this.icons.add(abilityString);
			}
			
			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
				int widgetLeft = left;
				
				for (AbstractWidget widget : this.icons) {
					widget.setPosition(widgetLeft, top);
					widget.render(guiGraphics, mouseX, mouseY, partialTicks);
					
					widgetLeft += widget.getWidth() + 4;
				}
			}
			
			@Override
			public List<? extends GuiEventListener> children() {
				return this.icons;
			}
			
			@Override
			public List<? extends NarratableEntry> narratables() {
				return this.icons;
			}
		}
	}
	
	private static class AttributeIcon extends AbstractWidget {
		private final TextureInfo textureInfo;
		
		public AttributeIcon(int x, int y, int width, int height, Attribute attribute, TextureInfo textureInfo) {
			super(x, y, width, height, Component.translatable(attribute.getDescriptionId() + ".skillbook_tooltip"));
			this.textureInfo = textureInfo;
		}
		
		public AttributeIcon(int x, int y, int width, int height, Component customTooltip, TextureInfo textureInfo) {
			super(x, y, width, height, customTooltip);
			this.textureInfo = textureInfo;
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			if (this.textureInfo != null) {
				guiGraphics.blit(this.textureInfo.resourceLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.textureInfo.u, this.textureInfo.v, this.textureInfo.width, this.textureInfo.height, 256, 256);
			}
			
			if (this.isHovered()) {
				this.setTooltip(this.getMessage() == null ? null : Tooltip.create(this.getMessage()));
			} else {
				this.setTooltip(null);
			}
		}
		
		@Override
		protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
		}
	}
	
	public static class TextureInfo {
		final ResourceLocation resourceLocation;
		final int u;
		final int v;
		final int width;
		final int height;
		
		public TextureInfo(ResourceLocation resourceLocation, int u, int v, int width, int height) {
			this.resourceLocation = resourceLocation;
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}
	}
	
	private class LearnButton extends Button {
		protected LearnButton(Builder builder) {
			super(builder);
		}
		
		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			Minecraft minecraft = Minecraft.getInstance();
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			
			int texX = 106;
			
			if (this.isHoveredOrFocused() || !this.isActive()) {
			   texX = 156;
			}
			
			guiGraphics.pose().pushPose();
			guiGraphics.blitNineSliced(SKILLBOOK_BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 45, 15, texX, 193);
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.pose().popPose();
			
			int i = this.getFGColor();
			this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
		}
	}
}