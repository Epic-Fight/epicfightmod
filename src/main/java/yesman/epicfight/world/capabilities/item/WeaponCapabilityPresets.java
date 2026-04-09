package yesman.epicfight.world.capabilities.item;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.ex_cap.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.core.managers.ExCapManager;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;

@SuppressWarnings("deprecation") // getLevel
public class WeaponCapabilityPresets {

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
        EpicFightMod.LOGGER.debug(copy.toString());
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