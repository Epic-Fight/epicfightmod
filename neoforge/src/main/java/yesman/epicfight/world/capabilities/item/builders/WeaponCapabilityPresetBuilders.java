package yesman.epicfight.world.capabilities.item.builders;

import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.Movesets;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCapability;
import yesman.epicfight.world.capabilities.item.builders.providers.MainConditionals;

public class WeaponCapabilityPresetBuilders
{
    public static WeaponCapability.Builder AXE = WeaponCapability.builder()
            .category(CapabilityItem.WeaponCategories.AXE)
            .hitSound(EpicFightSounds.BLADE_HIT.get())
            .collider(ColliderPreset.TOOLS)
            .addConditionals(MainConditionals.default1HWieldStyle)
            .addMoveSet(CapabilityItem.Styles.ONE_HAND, Movesets.axeOneHandMS);
}
