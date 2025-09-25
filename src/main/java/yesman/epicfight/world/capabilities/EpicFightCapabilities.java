package yesman.epicfight.world.capabilities;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightAttachmentTypes;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;
import yesman.epicfight.world.capabilities.provider.CommonItemCapabilityProvider;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public class EpicFightCapabilities {
	public static final ItemCapability<CapabilityItem, Void> CAPABILITY_ITEM =
		ItemCapability.createVoid(
			ResourceLocation.fromNamespaceAndPath(EpicFightMod.MODID, "item_capability"),
			CapabilityItem.class
		);
	
	public static final CommonEntityPatchProvider ENTITY_PATCH_PROVIDER = CommonEntityPatchProvider.INSTANCE;
	public static final CommonItemCapabilityProvider ITEM_CAPABILITY_PROVIDER = CommonItemCapabilityProvider.INSTANCE;
	
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		BuiltInRegistries.ITEM.forEach(item -> {
			event.registerItem(CAPABILITY_ITEM, ITEM_CAPABILITY_PROVIDER, item);
		});
	}
	
	/**
	 * This method should remain as the secondary option, especially when you can't fix local variables inside lambda expression.
	 * 
	 * @param stack
	 * @return
	 */
	public static @Nullable CapabilityItem getItemStackCapability(ItemStack stack) {
		return getItemCapability(stack).orElse(CapabilityItem.EMPTY);
	}
	
	/**
	 * Return an optional for a given item stack
	 * 
	 * @param stack
	 * @return
	 */
	public static Optional<CapabilityItem> getItemCapability(ItemStack stack) {
		return Optional.ofNullable(stack.getCapability(CAPABILITY_ITEM));
	}
	
	/**
	 * This method should remain as the secondary option, especially when you can't fix local variables inside lambda expression.
	 * 
	 * @param entity An entity object to extract an entity patch
	 * @param type A class type to cast
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityPatch<?>> T getEntityPatch(Entity entity, Class<T> type) {
		if (entity != null) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();
			
			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return (T)entitypatch;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns entity patch with unparameterized original entity
	 * This is useful to reduce the amount of code when type-casting for {@link EntityPatch#getOriginal} is unnecessary.
	 * 
	 * @param entity An entity object to extract an entity patch
	 * @param type A class type to cast
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityPatch<?>> Optional<T> getUnparameterizedEntityPatch(Entity entity, Class<T> type) {
		if (entity != null) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();
			
			if (entitypatch != null && type.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
		
	}
	
	/**
	 * Returns entity patch with parameterized original entity
	 * This method is used when you need parameterized return value of {@link EntityPatch#getOriginal}.
	 * 
	 * @param entity An entity object to extract an entity patch
	 * @param entitytype An entity type to cast
	 * @param patchtype A class type to cast
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Entity, T extends EntityPatch<E>> Optional<T> getParameterizedEntityPatch(Entity entity, Class<E> entitytype, Class<?> patchtype) {
		if (entity != null && entitytype.isAssignableFrom(entity.getClass())) {
			AttachmentEntityPatchProvider attachmentEntitypatchProvider = entity.getData(EpicFightAttachmentTypes.ENTITY_PATCH);
			EntityPatch<?> entitypatch = attachmentEntitypatchProvider.getCapability();
			
			if (entitypatch != null && patchtype.isAssignableFrom(entitypatch.getClass())) {
				return Optional.of((T)entitypatch);
			}
		}
		
		return Optional.empty();
	}
}