package yesman.epicfight.api.client.event.types.registry;

import com.google.gson.JsonElement;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;

import java.util.Map;
import java.util.function.Function;

/// Register
@SuppressWarnings("rawtypes")
public abstract class RegisterPatchedRenderersEvent extends Event {
	public static class Item extends RegisterPatchedRenderersEvent {
		private final Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers;
		
		public Item(Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers) {
			this.itemRenderers = itemRenderers;
		}
		
		public void addItemRenderer(ResourceLocation rl, Function<JsonElement, RenderItemBase> provider) {
			if (this.itemRenderers.containsKey(rl)) {
				throw new IllegalArgumentException("Item renderer " + rl + " already registered.");
			}
			
			this.itemRenderers.put(rl, provider);
		}
	}
	
	public static class AddEntity extends RegisterPatchedRenderersEvent {
		private final Map<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
		private final EntityRendererProvider.Context context;
		
		public AddEntity(Map<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider, EntityRendererProvider.Context context) {
			this.entityRendererProvider = entityRendererProvider;
			this.context = context;
		}
		
		public void addPatchedEntityRenderer(EntityType<?> entityType, Function<EntityType<?>, PatchedEntityRenderer> provider) {
			this.entityRendererProvider.put(entityType, provider);
		}
		
		public EntityRendererProvider.Context getContext() {
			return this.context;
		}
	}
	
	public static class ModifyEntity extends RegisterPatchedRenderersEvent {
		private final Map<EntityType<?>, PatchedEntityRenderer> renderers;
		
		public ModifyEntity(Map<EntityType<?>, PatchedEntityRenderer> renderers) {
			this.renderers = renderers;
		}
		
		public PatchedEntityRenderer get(EntityType<?> entityType) {
			return this.renderers.get(entityType);
		}
	}
}