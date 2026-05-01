package yesman.epicfight.world.capabilities.provider;

import net.minecraft.world.item.Item;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.function.Function;

/**
 * Used to get extra entries created in a runtime via datapack editor
 *
 * Might be implementation more future-proofing method in the next version.
 * Just remaining for backward compatibility
 */
@Deprecated(forRemoval = true, since = "26.1")
public interface ExtraEntryProvider {
    <T extends StaticAnimation> AnimationAccessor<T> getExtraOrBuiltInAnimation(String path);

    AssetAccessor<? extends SkinnedMesh> getExtraOrBuiltInMesh(String path);

    AssetAccessor<? extends Armature> getExtraOrBuiltInArmature(String path);

    Function<Item, ? extends CapabilityItem.Builder<?>> getExtraOrBuiltInWeaponType(String typeName);
}