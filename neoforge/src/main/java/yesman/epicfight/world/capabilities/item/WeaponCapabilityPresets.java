package yesman.epicfight.world.capabilities.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;

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

    public static CapabilityItem.Builder<?> exCapRegistration(CapabilityItem.Builder<?> builder, Item item)
    {
        if (builder instanceof WeaponCapability.Builder weaponBuilder)
        {
            WeaponCapability.Builder copy = weaponBuilder.copy();
            handleTieredStats(copy, item);
            return copy;
        }

        return CapabilityItem.builder();
    }

    private static void handleTieredStats(WeaponCapability.Builder builder, Item item)
    {
        if (item instanceof TieredItem tieredItem) {
            int tierLevel = vanillaTierToLevel(tieredItem.getTier());
            builder.modifyTierAttributes(tierLevel);
        }
    }
}
