package yesman.epicfight.main;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.world.item.EpicFightCreativeTabs;

/**
 * @Param skillBookCreativeTab : decides which creative tab will display the skills that belong to the mod {@link EpicFightCreativeTabs}}
 */
public record EpicFightExtensions(RegistryObject<CreativeModeTab> skillBookCreativeTab) implements IExtensionPoint<EpicFightExtensions> {
}