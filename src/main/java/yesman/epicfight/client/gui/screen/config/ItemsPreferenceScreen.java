package yesman.epicfight.client.gui.screen.config;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.collect.Sets;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen.ItemList.ItemEntry;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.item.WeaponItem;

public class ItemsPreferenceScreen extends Screen {
	private static final Set<Class<? extends Item>> WEAPON_CATEGORIZED_ITEM_CLASSES = new HashSet<> ();
	private static final Set<Class<? extends Item>> TOOL_CATEGORIZED_ITEM_CLASSES = new HashSet<> ();
	
	static {
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(SwordItem.class);
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(WeaponItem.class);
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(BowItem.class);
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(CrossbowItem.class);
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(TridentItem.class);
		
		TOOL_CATEGORIZED_ITEM_CLASSES.add(AxeItem.class);
		TOOL_CATEGORIZED_ITEM_CLASSES.add(HoeItem.class);
		TOOL_CATEGORIZED_ITEM_CLASSES.add(PickaxeItem.class);
		TOOL_CATEGORIZED_ITEM_CLASSES.add(ShovelItem.class);
	}
	
	@SafeVarargs
	public static void registerWeaponCategorizedItemClasses(Class<? extends Item>... weaponClasses) {
		WEAPON_CATEGORIZED_ITEM_CLASSES.addAll(Set.of(weaponClasses));
	}
	
	@SafeVarargs
	public static void registerToolCategorizedItemClasses(Class<? extends Item>... toolClasses) {
		TOOL_CATEGORIZED_ITEM_CLASSES.addAll(Set.of(toolClasses));
	}
	
	private static boolean judgeItemPreference(Item item) {
		boolean isweapon = false;
		boolean foundMatchingClass = false;
		Class<?> itemClass = item.getClass();
		
		while (itemClass != Item.class) {
			if (WEAPON_CATEGORIZED_ITEM_CLASSES.contains(itemClass)) {
				isweapon = true;
				foundMatchingClass = true;
				break;
			}
			
			if (TOOL_CATEGORIZED_ITEM_CLASSES.contains(itemClass)) {
				isweapon = false;
				foundMatchingClass = true;
				break;
			}
			
			itemClass = itemClass.getSuperclass();
		}
		
		if (!foundMatchingClass) {
			CapabilityItem itemCapability = EpicFightCapabilities.getItemStackCapability(item.getDefaultInstance());
			
			if (itemCapability != null && itemCapability instanceof WeaponCapability) {
				isweapon = true;
			}
		}
		
		return isweapon;
	}
	
	public static void resetItems() {
		ClientConfig.combatPreferredItems.clear();
		ClientConfig.miningPreferredItems.clear();
		
		ForgeRegistries.ITEMS.getValues().forEach(item -> {
			if (judgeItemPreference(item)) {
				ClientConfig.combatPreferredItems.add(item);
			} else {
				ClientConfig.miningPreferredItems.add(item);
			}
		});
	}
	
	public static void addWeaponCategorizedItemClass(Class<? extends Item> cls) {
		WEAPON_CATEGORIZED_ITEM_CLASSES.add(cls);
	}
	
	private static final Component RESET_TOOLTIP = Component.translatable(EpicFightMod.format("gui.%s.item_preferences.tooltip.find_weapon"));
	private static final Component SWAP_ALL = Component.translatable(EpicFightMod.format("%s.gui.item_preferences.swap_all"));
	
	public static final Component GUI_FIND_WEAPONS = Component.translatable(EpicFightMod.format("gui.%s.find_weapon"));
	
	protected final Screen parentScreen;
	private ItemsPreferenceScreen.ItemList combatPreferredItems;
	private ItemsPreferenceScreen.ItemList miningPreferredItems;
	
	private Set<Item> combatItems = new HashSet<> ();
	private Set<Item> miningItems = new HashSet<> ();
	
	public ItemsPreferenceScreen(Screen parentScreen) {
		super(Component.translatable(EpicFightMod.MODID + ".gui.configuration.item_preferences"));
		
		this.parentScreen = parentScreen;
		
		ForgeRegistries.ITEMS.forEach(item -> {
			if (ClientConfig.combatPreferredItems.contains(item)) {
				this.combatItems.add(item);
			} else if (ClientConfig.miningPreferredItems.contains(item)) {
				this.miningItems.add(item);
			} else {
				if (judgeItemPreference(item)) {
					this.combatItems.add(item);
				} else {
					this.miningItems.add(item);
				}
			}
		});
	}
	
	@Override
	protected void init() {
		if (this.combatPreferredItems == null) {
			this.combatPreferredItems = new ItemsPreferenceScreen.ItemList(200, this.height, true, Component.translatable(EpicFightMod.MODID + ".gui.combat_perferred"), Component.translatable(EpicFightMod.MODID + ".gui.combat_perferred.tooltip"), () -> this.miningPreferredItems);
			this.combatItems.stream().sorted(ItemEntry::compare).forEach((item) -> this.combatPreferredItems.addEntry(item));
		} else {
			this.combatPreferredItems.resize(200, this.height);
			this.combatPreferredItems.setScrollAmount(this.combatPreferredItems.getScrollAmount());
		}
		
		if (this.miningPreferredItems == null) {
			this.miningPreferredItems = new ItemsPreferenceScreen.ItemList(200, this.height, false, Component.translatable(EpicFightMod.MODID + ".gui.mining_preferred"), Component.translatable(EpicFightMod.MODID + ".gui.mining_preferred.tooltip"), () -> this.combatPreferredItems);
			this.miningItems.stream().sorted(ItemEntry::compare).forEach((item) -> this.miningPreferredItems.addEntry(item));
		} else {
			this.miningPreferredItems.resize(200, this.height);
			this.combatPreferredItems.setScrollAmount(this.combatPreferredItems.getScrollAmount());
		}
		
		this.combatPreferredItems.setLeftPos(this.width / 2 - 204);
		this.miningPreferredItems.setLeftPos(this.width / 2 + 4);
		this.addRenderableWidget(this.combatPreferredItems);
		this.addRenderableWidget(this.miningPreferredItems);
		
		EditBox keywordEditBox = new EditBox(this.minecraft.font, this.width / 2 - 75, this.height - 50, 150, 15, Component.empty());
		
		keywordEditBox.setResponder(keyword -> {
			this.combatPreferredItems.filter(keyword);
			this.miningPreferredItems.filter(keyword);
			this.combatPreferredItems.setScrollAmount(this.combatPreferredItems.getScrollAmount());
			this.miningPreferredItems.setScrollAmount(this.miningPreferredItems.getScrollAmount());
		});
		
		this.addRenderableWidget(keywordEditBox);
		
		this.addRenderableWidget(
			Button.builder(
				Component.literal("<"),
				(button) -> {
					this.miningPreferredItems.children().removeIf(itemEntry -> {
						String name = Component.translatable(itemEntry.itemStack.getItem().getDescriptionId()).getString();
						
						if (ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keywordEditBox.getValue()))) {
							this.miningItems.remove(itemEntry.itemStack.getItem());
							this.combatItems.add(itemEntry.itemStack.getItem());
							this.combatPreferredItems.createOpponent(itemEntry);
							return true;
						}
						
						return false;
					});
					
					this.combatPreferredItems.setScrollAmount(this.combatPreferredItems.getScrollAmount());
					this.miningPreferredItems.setScrollAmount(this.miningPreferredItems.getScrollAmount());
				}
			)
			.bounds(this.width / 2 - 92, this.height - 52, 15, 19)
			.build()
		);
		
		this.addRenderableWidget(
			Button.builder(
				Component.literal(">"),
				(button) -> {
					this.combatPreferredItems.children().removeIf(itemEntry -> {
						String name = Component.translatable(itemEntry.itemStack.getItem().getDescriptionId()).getString();
						
						if (ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keywordEditBox.getValue()))) {
							this.combatItems.remove(itemEntry.itemStack.getItem());
							this.miningItems.add(itemEntry.itemStack.getItem());
							this.miningPreferredItems.createOpponent(itemEntry);
							return true;
						}
						
						return false;
					});
					
					this.combatPreferredItems.setScrollAmount(this.combatPreferredItems.getScrollAmount());
					this.miningPreferredItems.setScrollAmount(this.miningPreferredItems.getScrollAmount());
				}
			)
			.bounds(this.width / 2 + 77, this.height - 52, 15, 19)
			.build()
		);
		
		this.addRenderableWidget(
			Button.builder(
				CommonComponents.GUI_DONE,
				(button) -> {
					ClientConfig.combatPreferredItems.clear();
					ClientConfig.miningPreferredItems.clear();
					ClientConfig.combatPreferredItems.addAll(this.combatItems);
					ClientConfig.miningPreferredItems.addAll(this.miningItems);
					ClientConfig.saveChanges();
					this.onClose();
				}
			)
			.bounds(this.width / 2 - 102, this.height - 28, 100, 20)
			.build()
		);
		
		this.addRenderableWidget(
			Button.builder(
				ItemsPreferenceScreen.GUI_FIND_WEAPONS,
				(button) -> {
					resetItems();
					
					this.combatPreferredItems.children().clear();
					this.miningPreferredItems.children().clear();
					this.combatItems.clear();
					this.miningItems.clear();
					
					ClientConfig.combatPreferredItems.stream()
						.sorted(ItemEntry::compare)
						.forEach(item -> {
							this.combatItems.add(item);
							this.combatPreferredItems.addEntry(item);
						});
					
					ClientConfig.miningPreferredItems.stream()
						.sorted(ItemEntry::compare)
						.forEach(item -> {
							this.miningItems.add(item);
							this.miningPreferredItems.addEntry(item);
						});
					
					this.combatPreferredItems.setScrollAmount(0.0F);
					this.miningPreferredItems.setScrollAmount(0.0F);
				}
			)
			.bounds(this.width / 2 + 2, this.height - 28, 100, 20)
			.tooltip(Tooltip.create(RESET_TOOLTIP))
			.build()
		);
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderDirtBackground(guiGraphics);
		this.combatPreferredItems.render(guiGraphics, mouseX, mouseY, partialTick);
		this.miningPreferredItems.render(guiGraphics, mouseX, mouseY, partialTick);
		
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);
		guiGraphics.drawCenteredString(this.font, SWAP_ALL, this.width / 2 - 120, this.height - 45, 16777215);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}

	class ItemList extends ObjectSelectionList<ItemsPreferenceScreen.ItemList.ItemEntry> {
		private final Component title;
		private final Component tooltip;
		private final Supplier<ItemList> opponent;
		private final boolean left;
		
		public ItemList(int width, int height, boolean left, MutableComponent title, Component tooltip, Supplier<ItemList> opponent) {
			super(ItemsPreferenceScreen.this.minecraft, width, height, 32, height - 55, 22);
			
			this.title = title.withStyle(ChatFormatting.UNDERLINE);
			this.tooltip = tooltip;
			this.opponent = opponent;
			this.left = left;
			this.setRenderHeader(true, (int)(9.0F * 1.5F));
			
			if (this.getSelected() != null) {
				this.centerScrollOn(this.getSelected());
			}
		}
		
		public void resize(int width, int height) {
			this.width = width;
			this.height = height;
			this.y0 = 32;
			this.y1 = height - 55;
			this.x0 = 0;
			this.x1 = width;
		}
		
		@Override
		protected void renderHeader(GuiGraphics guiGraphics, int x, int y) {
			guiGraphics.drawString(this.minecraft.font, this.title, x + this.width / 2 - this.minecraft.font.width(this.title) / 2, Math.min(this.y0 + 3, y), 16777215, false);
		}
		
		@Override
		public int getRowWidth() {
			return this.width;
		}
		
		@Override
		protected int getScrollbarPosition() {
			return this.x1 - 6;
		}
		
		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			super.render(guiGraphics, mouseX, mouseY, partialTick);
			
			int width = this.minecraft.font.width(this.title);
			int x = this.getRowLeft();
			int y = this.y0 + 4 - (int)this.getScrollAmount();
			x = x + this.width / 2 - width / 2;
			y = Math.min(this.y0 + 3, y);
			
			if (x < mouseX && y < mouseY && mouseX < (x + width) && mouseY < (y + 15)) {
				ItemsPreferenceScreen.this.setTooltipForNextRenderPass(this.tooltip);
			}
		}
		
		protected void addEntry(Item item) {
			this.children().add(new ItemEntry(item.getDefaultInstance(), this.left));
		}
		
		protected void removeEntryHaving(Item item) {
			this.children().removeIf(entry -> entry.itemStack.getItem().equals(item));
		}
		
		protected void createOpponent(ItemEntry itemEntry) {
			ItemEntry newEntry = new ItemEntry(itemEntry.itemStack, this.left);
			this.addEntry(newEntry);
			this.centerScrollOn(newEntry);
			this.setFocused(newEntry);
		}
		
		public void filter(String keyword) {
			this.children().clear();
			
			Set<Item> list = this.left ? ItemsPreferenceScreen.this.combatItems : ItemsPreferenceScreen.this.miningItems;
			
			list.stream().filter(item -> {
				String name = Component.translatable(item.getDescriptionId()).getString();
				return ParseUtil.toLowerCase(name).contains(ParseUtil.toLowerCase(keyword));
			}).sorted(ItemEntry::compare).forEach(this::addEntry);
		}
		
		class ItemEntry extends ObjectSelectionList.Entry<ItemsPreferenceScreen.ItemList.ItemEntry> {
			private static final Set<Item> UNRENDERABLE_ITEMS = Sets.newHashSet();
			
			public static int compare(Item e1, Item e2) {
				String e1DisplayName = Component.translatable(e1.getDescriptionId()).getString();
				String e2DisplayName = Component.translatable(e2.getDescriptionId()).getString();
				return e1DisplayName.compareTo(e2DisplayName);
			}
			
			private final ItemStack itemStack;
			private final Button switchButton;
			
			public ItemEntry(ItemStack itemStack, boolean left) {
				this.itemStack = itemStack;
				
				this.switchButton = Button.builder(
					Component.literal(left ? ">" : "<"),
					(button) -> {
						ItemList.this.removeEntry(this);
						ItemList.this.setScrollAmount(ItemList.this.getScrollAmount());
						ItemList.this.opponent.get().createOpponent(this);
						
						if (left) {
							ItemsPreferenceScreen.this.combatItems.remove(itemStack.getItem());
							ItemsPreferenceScreen.this.miningItems.add(itemStack.getItem());
						} else {
							ItemsPreferenceScreen.this.miningItems.remove(itemStack.getItem());
							ItemsPreferenceScreen.this.combatItems.add(itemStack.getItem());
						}
					}
				)
				.bounds(0, 0, 20, 20)
				.build();
			}
			
			@Override
			public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
				try {
					if (!UNRENDERABLE_ITEMS.contains(this.itemStack.getItem())) {
						guiGraphics.renderItem(this.itemStack, left + 4, top + 1);
					}
				} catch (Exception e) {
					UNRENDERABLE_ITEMS.add(this.itemStack.getItem());
				}
				
				this.switchButton.setX(left + 170);
				this.switchButton.setY(top - 1);
				this.switchButton.render(guiGraphics, mouseX, mouseY, partialTick);
				
				Component component = this.itemStack.getHoverName();
				guiGraphics.drawString(ItemList.this.minecraft.font, component, left + 30, top + 5, 16777215, false);
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				if (button == 0) {
					if (this.switchButton.isMouseOver(mouseX, mouseY)) {
						this.switchButton.playDownSound(ItemsPreferenceScreen.this.minecraft.getSoundManager());
						this.switchButton.onPress();
					}
					
					ItemList.this.setSelected(this);
					return true;
				} else {
					return false;
				}
			}
			
			@Override
			public boolean equals(Object obj) {
				if (obj instanceof ItemEntry itemEntry) {
					return this.itemStack.equals(itemEntry.itemStack);
				} else {
					return super.equals(obj);
				}
			}
			
			@Override
			public Component getNarration() {
				return  Component.translatable("narrator.select", this.itemStack.getHoverName());
			}
		}
	}
}