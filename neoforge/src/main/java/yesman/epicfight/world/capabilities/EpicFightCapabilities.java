package yesman.epicfight.world.capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightAttachmentTypes;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.CommonItemCapabilityProvider;

import java.util.Optional;

@SuppressWarnings("unchecked")
public class EpicFightCapabilities {
	public static final ItemCapability<CapabilityItem, Void> CAPABILITY_ITEM =
		ItemCapability.createVoid(
            EpicFightMod.identifier("item_capability"),
            CapabilityItem.class
        );

	public static final CommonEntityPatchProvider ENTITY_PATCH_PROVIDER = CommonEntityPatchProvider.INSTANCE;
	public static final CommonItemCapabilityProvider ITEM_CAPABILITY_PROVIDER = CommonItemCapabilityProvider.INSTANCE;

	/// Returns [CapabilityItem] from [ItemStack] instance
	public static @NotNull CapabilityItem getItemStackCapability(ItemStack stack) {
		return getItemCapability(stack).orElse(CapabilityItem.EMPTY);
	}

    /// Returns [CapabilityItem] from [ItemStack] instance wrapped by [Optional]
	public static Optional<CapabilityItem> getItemCapability(ItemStack stack) {
		return Optional.ofNullable(stack.getCapability(CAPABILITY_ITEM));
	}

	/// This method should remain as the secondary option, especially when you can't fix local variables inside lambda expression.
    ///
	/// @param entity An entity object to extract an entity patch
	/// @param type A class type to cast
	public static <T extends EntityPatch<?>> T getEntityPatch(@Nullable Entity entity, Class<T> type) {
		if (entity != null) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return (T)entitypatch;
			}
		}

		return null;
	}

    /// A compact version of [#getEntityPatch(Entity, Class)] to extract [PlayerPatch] from [Player]
    /// Conducts null checking
    public static @Nullable PlayerPatch<?> getPlayerPatch(@Nullable Player player) {
        if (player != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = player.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch != null && PlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
                return (PlayerPatch<?>)entitypatch;
            }
        }

        return null;
    }

    /// A compact version of [#getEntityPatch(Entity, Class)] to extract [ServerPlayerPatch] from [ServerPlayer]
    /// Conducts null checking
    public static @Nullable ServerPlayerPatch getServerPlayerPatch(@Nullable ServerPlayer serverPlayer) {
        if (serverPlayer != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = serverPlayer.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch != null && ServerPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
                return (ServerPlayerPatch)entitypatch;
            }
        }

        return null;
    }

    /// A compact version of [#getEntityPatch(Entity, Class)] to extract [LocalPlayerPatch] from [LocalPlayer]
    /// Conducts null checking
    public static @Nullable LocalPlayerPatch getLocalPlayerPatch(@Nullable LocalPlayer localPlayer) {
        if (localPlayer != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = localPlayer.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch != null && LocalPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
                return (LocalPlayerPatch)entitypatch;
            }
        }

        return null;
    }

    /// Returns [LocalPlayerPatch] from cached player in [Minecraft]
    /// Warning: developers must check physical & logical side before calling this method
    @ClientOnly
    public static @Nullable LocalPlayerPatch getCachedLocalPlayerPatch() {
        if (Minecraft.getInstance().player == null) {
            return null;
        }

        AttachmentEntityPatchProvider attachmentEntitypatchProvider = Minecraft.getInstance().player.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
        EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

        if (entitypatch instanceof LocalPlayerPatch localplayerpatch) {
            return localplayerpatch;
        }

        return null;
    }

    /// Returns [LocalPlayerPatch] as an optional object from cached player in [Minecraft]
    /// Warning: developers must check physical & logical side before calling this method
    @ClientOnly
    public static Optional<LocalPlayerPatch> getCachedLocalPlayerPatchAsOptional() {
        if (Minecraft.getInstance().player == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(getLocalPlayerPatch(Minecraft.getInstance().player));
    }

	/// Returns entity patch with unparameterized original entity
	/// This is useful to reduce the amount of code when type-casting for [EntityPatch#getOriginal] is unnecessary.
    ///
	/// @param entity An entity object to extract an entity patch
	/// @param type A class type to cast
	public static <T extends EntityPatch<?>> Optional<T> getUnparameterizedEntityPatch(@Nullable Entity entity, Class<T> type) {
		if (entity != null) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();
			
			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
		
	}

	/// Returns entity patch with parameterized original entity
	/// This method is used when you need parameterized return value of [EntityPatch#getOriginal].
    ///
	/// @param entity       An entity object to extract an entity patch
	/// @param entitytype   An entity type to cast
	/// @param patchtype    A class type to cast
	public static <E extends Entity, T extends EntityPatch<E>> Optional<T> getParameterizedEntityPatch(@Nullable Entity entity, Class<E> entitytype, Class<?> patchtype) {
		if (entity != null && entitytype.isAssignableFrom(entity.getClass())) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();
			
			if (entitypatch != null && patchtype.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
	}

    /// Returns [PlayerPatch] from a player
    ///
    /// @param entity A player to extract the entity patch
    public static Optional<PlayerPatch<?>> getPlayerPatchAsOptional(@Nullable Entity entity) {
        if (entity != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch instanceof PlayerPatch<?> playerpatch) {
                return Optional.of(playerpatch);
            }
        }

        return Optional.empty();
    }

    /// Returns [LocalPlayerPatch] from a local player
    /// Warning: developers must check physical & logical side before calling this method
    ///
    /// @param entity A player to extract the entity patch
    public static Optional<LocalPlayerPatch> getLocalPlayerPatchAsOptional(@Nullable Entity entity) {
        if (entity != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch instanceof LocalPlayerPatch localplayerpatch) {
                return Optional.of(localplayerpatch);
            }
        }

        return Optional.empty();
    }

    /// Returns [ServerPlayerPatch] from a server player
    /// Warning: developers must check logical side before calling this method
    ///
    /// @param entity A player to extract the entity patch
    public static Optional<ServerPlayerPatch> getServerPlayerPatchAsOptional(@Nullable Entity entity) {
        if (entity != null) {
            AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
            EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();

            if (entitypatch instanceof ServerPlayerPatch serverplayerpatch) {
                return Optional.of(serverplayerpatch);
            }
        }

        return Optional.empty();
    }
}