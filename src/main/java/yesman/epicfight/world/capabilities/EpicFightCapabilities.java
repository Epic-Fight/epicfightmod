package yesman.epicfight.world.capabilities;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.projectile.ProjectilePatch;
import yesman.epicfight.world.capabilities.skill.CapabilitySkill;

@SuppressWarnings("rawtypes")
public class EpicFightCapabilities {
	public static final Capability<EntityPatch> CAPABILITY_ENTITY = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<CapabilityItem> CAPABILITY_ITEM = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<ProjectilePatch> CAPABILITY_PROJECTILE = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<CapabilitySkill> CAPABILITY_SKILL = CapabilityManager.get(new CapabilityToken<>(){});
    
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(CapabilityItem.class);
		event.register(EntityPatch.class);
		event.register(ProjectilePatch.class);
		event.register(CapabilitySkill.class);
	}
	
	public static CapabilityItem getItemStackCapability(ItemStack stack) {
		return stack.isEmpty() ? CapabilityItem.EMPTY : stack.getCapability(CAPABILITY_ITEM).orElse(CapabilityItem.EMPTY);
	}
	
	public static CapabilityItem getItemStackCapabilityOr(ItemStack stack, @Nullable CapabilityItem defaultCap) {
		return stack.isEmpty() ? defaultCap : stack.getCapability(CAPABILITY_ITEM).orElse(defaultCap);
	}
	
	public static Optional<CapabilityItem> getItemCapability(ItemStack stack) {
		return stack.isEmpty() ? Optional.empty() : stack.getCapability(CAPABILITY_ITEM).resolve();
	}
	
	/**
	 * Extracts {@link EntityPatch} from an entity object, conducting both null-checking and type-checking
	 * @return stored {@link EntityPatch} capability or {@link null} if the given @param type is incompatible with actual {@link EntityPatch} object
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public static <T extends EntityPatch> T getEntityPatch(@Nullable Entity entity, Class<T> type) {
		if (entity != null) {
			EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return (T)entitypatch;
			}
		}
		
		return null;
	}
	
	/**
	 * A compact version of {@link #getEntityPatch(Entity, Class)} to extract {@link PlayerPatch} from {@link Player}
	 * Conducts null checking
	 */
	@Nullable
	public static PlayerPatch getPlayerPatch(@Nullable Player player) {
		if (player != null) {
			EntityPatch<?> entitypatch = player.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && PlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
				return (PlayerPatch<?>)entitypatch;
			}
		}
		
		return null;
	}
	
	/**
	 * A compact version of {@link #getEntityPatch(Entity, Class)} to extract {@link ServerPlayerPatch} from {@link ServerPlayer}
	 * Conducts null checking
	 */
	@Nullable
	public static ServerPlayerPatch getServerPlayerPatch(@Nullable ServerPlayer serverPlayer) {
		if (serverPlayer != null) {
			EntityPatch<?> entitypatch = serverPlayer.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && ServerPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
				return (ServerPlayerPatch)entitypatch;
			}
		}
			
		return null;
	}
	
	/**
	 * A compact version of {@link #getEntityPatch(Entity, Class)} to extract {@link LocalPlayerPatch} from {@link LocalPlayer}
	 * Conducts null checking
	 */
	@Nullable
	public static LocalPlayerPatch getLocalPlayerPatch(@Nullable LocalPlayer localPlayer) {
		if (localPlayer != null) {
			EntityPatch<?> entitypatch = localPlayer.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && LocalPlayerPatch.class.isAssignableFrom(entitypatch.getClass())) {
				return (LocalPlayerPatch)entitypatch;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns {@link EntityPatch} with unparameterized original {@link Entity} type
	 * This method always return {@link Entity} by {@link EntityPatch#getOriginal()}, so it is considerable to use
	 * when you don't need parameterized original entity type to save parameters
	 * 
	 * @param entity 	An entity object to extract {@link EntityPatch}
	 * @param type 		A subclasses type of {@link EntityPatch} to cast
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityPatch<?>> Optional<T> getUnparameterizedEntityPatch(@Nullable Entity entity, Class<T> type) {
		if (entity != null) {
			EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Returns {@link EntityPatch} with parameterized original {@link Entity} type
	 * This method will return type-cased original entity object specified by @param entitytype by {@link EntityPatch#getOriginal()},
	 * so it is considerable to use when you need to access original methods from @param entitytype
	 * 
	 * @param entity 		An entity object to extract {@link EntityPatch}
	 * @param entitytype 	A subclasses type of {@link Entity} to cast the original entity
	 * @param patchtype 	A subclasses type of {@link EntityPatch} to cast
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity, T extends EntityPatch<E>> Optional<T> getParameterizedEntityPatch(@Nullable Entity entity, Class<E> entitytype, Class<?> patchtype) {
		if (entity != null && entitytype.isAssignableFrom(entity.getClass())) {
			EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
			
			if (entitypatch != null && patchtype.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
	}
	
	/**
	 * A compact version of entity patch getter to get {@link PlayerPatch} for @param entity
	 * It conducts both null-checking and type-checking
	 */
	public static Optional<PlayerPatch<?>> getPlayerPatchAsOptional(@Nullable Entity entity) {
		if (entity == null) {
			return Optional.empty();
		}
		
		EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (entitypatch instanceof PlayerPatch<?> playerpatch) {
			return Optional.of(playerpatch);
		}
		
		return Optional.empty();
	}
	
	/**
	 * A compact version of entity patch getter to get {@link ServerPlayerPatch} from @param entity
	 * It conducts both null-checking and type-checking
	 */
	public static Optional<ServerPlayerPatch> getServerPlayerPatchAsOptional(@Nullable Entity entity) {
		if (entity == null) {
			return Optional.empty();
		}
		
		EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (entitypatch instanceof ServerPlayerPatch serverplayerpatch) {
			return Optional.of(serverplayerpatch);
		}
		
		return Optional.empty();
	}
	
	/**
	 * A compact version of entity patch getter to get {@link LocalPlayerPatch} for @param entity
	 * It conducts both null-checking and type-checking
	 */
	public static Optional<LocalPlayerPatch> getLocalPlayerPatchAsOptional(@Nullable Entity entity) {
		if (entity == null) {
			return Optional.empty();
		}
		
		EntityPatch<?> entitypatch = entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).orElse(null);
		
		if (entitypatch instanceof LocalPlayerPatch localplayerpatch) {
			return Optional.of(localplayerpatch);
		}
		
		return Optional.empty();
	}
}