package yesman.epicfight.api.client.forgeevent;

import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonElement;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;
import yesman.epicfight.client.renderer.patched.entity.PatchedEntityRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;

@SuppressWarnings("rawtypes")
public abstract class PatchedRenderersEvent extends Event implements IModBusEvent {
	public static class RegisterItemRenderer extends PatchedRenderersEvent {
		private final Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers;
		
		public RegisterItemRenderer(Map<ResourceLocation, Function<JsonElement, RenderItemBase>> itemRenderers) {
			this.itemRenderers = itemRenderers;
		}
		
		public void addItemRenderer(ResourceLocation rl, Function<JsonElement, RenderItemBase> provider) {
			if (this.itemRenderers.containsKey(rl)) {
				throw new IllegalArgumentException("Item renderer " + rl + " already registered.");
			}
			
			this.itemRenderers.put(rl, provider);
		}
	}
	
	public static class Add extends PatchedRenderersEvent {
		private final Map<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider;
		private final EntityRendererProvider.Context context;
		
		public Add(Map<EntityType<?>, Function<EntityType<?>, PatchedEntityRenderer>> entityRendererProvider, EntityRendererProvider.Context context) {
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
	
	public static class Modify extends PatchedRenderersEvent {
		private final Map<EntityType<?>, PatchedEntityRenderer> renderers;
		
		public Modify(Map<EntityType<?>, PatchedEntityRenderer> renderers) {
			this.renderers = renderers;
		}
		
		public PatchedEntityRenderer get(EntityType<?> entityType) {
			return this.renderers.get(entityType);
		}
	}
}