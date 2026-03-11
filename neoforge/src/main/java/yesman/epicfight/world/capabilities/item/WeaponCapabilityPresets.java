package yesman.epicfight.world.capabilities.item;

import com.google.common.collect.Lists;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.managers.DatasetManager;
import yesman.epicfight.api.ex_cap.modules.core.managers.ExCapManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import yesman.epicfight.EpicFight;

import java.util.List;
import java.util.Map;

public abstract class WeaponCapabilityPresets {
	public static int vanillaTierToLevel(Tier tier) {
		if (tier instanceof Tiers vanillaTier) {
			switch (vanillaTier) {
                case WOOD, GOLD -> {return 0;}
                case STONE -> {return 1;}
			    case IRON -> {return 2;}
			    case DIAMOND -> {return 3;}
                case NETHERITE -> {return 4;}
			}
		}
		double sqrt = Math.sqrt(tier.getUses());

		// Custom tier mapping
		return sqrt < 10.0D ? 0 : (int)Math.round(sqrt / 10.0D);
	}

    public static WeaponCapability.Builder exCapRegistration(Map.Entry<ResourceLocation, WeaponCapability.Builder> entry, Item item)
    {
        if (entry == null) return new WeaponCapability.Builder();
        List<ExCapData> data = Lists.newArrayList();
        ExCapManager.retrieveExCapData(entry.getKey()).forEach(exCapData -> data.add(exCapData.build()));
        WeaponCapability.Builder copy = entry.getValue().copy();
        handleTieredStats(copy, item);
        data.forEach(exCapData -> exCapData.apply(copy));
        EpicFight.LOGGER.debug(copy.toString());
        return copy;
    }

    private static void handleTieredStats(WeaponCapability.Builder builder, Item item)
    {
        if (item instanceof TieredItem tieredItem) {
            int tierLevel = vanillaTierToLevel(tieredItem.getTier());
            builder.modifyTierAttributes(tierLevel);
        }
    }
}
