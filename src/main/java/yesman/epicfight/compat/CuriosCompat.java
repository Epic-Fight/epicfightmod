package yesman.epicfight.compat;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer.HumanoidRender;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;
import top.theillusivec4.curios.client.render.CuriosLayer;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.Mesh.DrawingFunction;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class CuriosCompat implements ICompatModule {
	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<PatchedRenderersEvent.Modify>addListener((event) -> {
			if (event.get(EntityType.PLAYER) instanceof PatchedLivingEntityRenderer patchedlivingrenderer) {
				patchedlivingrenderer.addPatchedLayerAlways(CuriosLayer.class, new PatchedCuriosLayerRenderer());
			}
		});
		
		eventBus.<EntityRenderersEvent.AddLayers>addListener((event) -> {
			PatchedCuriosLayerRenderer.CURIO_MESHES.values().forEach(SkinnedMesh::destroy);
			PatchedCuriosLayerRenderer.CURIO_MESHES.clear();
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
	}
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
	}
	
	@Override
	public void onForgeEventBus(IEventBus eventBus) {
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class PatchedCuriosLayerRenderer extends ModelRenderLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<LivingEntity>, CuriosLayer<LivingEntity, EntityModel<LivingEntity>>, SkinnedMesh> {
		private static final Map<HumanoidModel<LivingEntity>, SkinnedMesh> CURIO_MESHES = Maps.newHashMap();
		
		public PatchedCuriosLayerRenderer() {
			super(null);
		}
		
		@Override
		protected void renderLayer(
			  LivingEntityPatch<LivingEntity> entitypatch
			, LivingEntity livingEntity
			, CuriosLayer<LivingEntity, EntityModel<LivingEntity>> vanillaLayer
			, PoseStack poseStack
			, MultiBufferSource buffer
			, int packedLight
			, OpenMatrix4f[] poses
			, float bob
			, float yRot
			, float xRot
			, float partialTicks
		) {
			CuriosApi.getCuriosInventory(livingEntity).ifPresent((handler) -> {
				handler.getCurios().forEach((id, stacksHandler) -> {
					IDynamicStackHandler stackHandler = stacksHandler.getStacks();
					IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();
					
					for (int i = 0; i < stackHandler.getSlots(); i++) {
						ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
						boolean cosmetic = true;
						NonNullList<Boolean> renderStates = stacksHandler.getRenders();
						boolean renderable = renderStates.size() > i && renderStates.get(i);
						
						if (stack.isEmpty() && renderable) {
							stack = stackHandler.getStackInSlot(i);
							cosmetic = false;
						}
						
						if (!stack.isEmpty()) {
							final ItemStack finalStack = stack;
							SlotContext slotContext = new SlotContext(id, livingEntity, i, cosmetic, renderable);
							
							CuriosRendererRegistry.getRenderer(stack.getItem()).ifPresent(curioRenderer -> {
								if (curioRenderer instanceof HumanoidRender humanoidRenderer) {
									HumanoidModel<LivingEntity> curioModel = humanoidRenderer.getModel(finalStack, slotContext);
									SkinnedMesh skinnedMesh;
									
									if (ClientEngine.getInstance().isVanillaModelDebuggingMode() || !CURIO_MESHES.containsKey(curioModel)) {
										poseStack.pushPose();
										poseStack.translate(10000.0D, 0.0D, 0.0D);
										LivingEntityRenderer<?, ?> parentRenderer;
										
										if (livingEntity instanceof AbstractClientPlayer abstractClientPlayer) {
											parentRenderer = (LivingEntityRenderer<?, ?>)Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get(abstractClientPlayer.getModelName());
										} else {
											parentRenderer = (LivingEntityRenderer<?, ?>)Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().get("default");
										}
										
										curioRenderer.render(finalStack, slotContext, poseStack, parentRenderer, buffer, 0, 0, 0, 0, 0, 0, 0);
										poseStack.popPose();
										
										skinnedMesh = HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(curioModel);
										CURIO_MESHES.put(curioModel, skinnedMesh);
									} else {
										skinnedMesh = CURIO_MESHES.get(curioModel);
									}
									
									skinnedMesh.drawPosed(
										  poseStack
										, buffer.getBuffer(EpicFightRenderTypes.getTriangulated(RenderType.entityCutoutNoCull(humanoidRenderer.getModelTexture(finalStack, slotContext))))
										, DrawingFunction.NEW_ENTITY
										, packedLight
										, 1.0F
										, 1.0F
										, 1.0F
										, 1.0F
										, OverlayTexture.NO_OVERLAY
										, entitypatch.getArmature()
										, poses
									);
								} else if (curioRenderer instanceof EpicFightCurioRenderer epicfightRenderer) {
									epicfightRenderer.draw(finalStack, slotContext, entitypatch, livingEntity, vanillaLayer, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
								}
							});
						}
					}
				});
			});
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public interface EpicFightCurioRenderer {
		void draw(
			  ItemStack itemstack
			, SlotContext slotContext
			, LivingEntityPatch<LivingEntity> entitypatch
			, LivingEntity livingEntity
			, CuriosLayer<LivingEntity, EntityModel<LivingEntity>> vanillaLayer
			, PoseStack poseStack
			, MultiBufferSource buffer
			, int packedLight
			, OpenMatrix4f[] poses
			, float bob
			, float yRot
			, float xRot
			, float partialTicks
		);
	}
}