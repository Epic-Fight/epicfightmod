package yesman.epicfight.registry.entries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightCreativeTabs {
	private EpicFightCreativeTabs() {}
	
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EpicFightMod.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = REGISTRY.register("items", () ->
		CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.epicfight.items"))
			.icon(() -> new ItemStack(EpicFightItems.SKILLBOOK.get()))
			.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
			.backgroundTexture(EpicFightMod.identifier("textures/gui/container/epicfight_creative_tab.png"))
			.hideTitle()
			.displayItems((params, output) -> {
				EpicFightItems.REGISTRY.getEntries().forEach(item -> {
					// FIXME: bad implement, maybe based protocol better yet.
					// ignore UCHIGATANA_SHEATH
					if (item == EpicFightItems.UCHIGATANA_SHEATH || item == EpicFightItems.SKILLBOOK) {
						return;
					}
					
					output.accept(item.get());
				});
			})
			.build()
		);
}