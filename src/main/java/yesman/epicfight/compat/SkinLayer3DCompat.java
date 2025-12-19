package yesman.epicfight.compat;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.skinlayers.SkinLayersModBase;
import dev.tr7zw.skinlayers.SkinUtil;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.api.Mesh;
import dev.tr7zw.skinlayers.render.CustomizableCubeListBuilder;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import dev.tr7zw.skinlayers.renderlayers.CustomLayerFeatureRenderer;
import dev.tr7zw.skinlayers.util.NMSWrapper.WrappedNativeImage;
import dev.tr7zw.skinlayers.versionless.util.wrapper.SolidPixelWrapper;
import dev.tr7zw.transition.mc.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.SkinLayer3DTransformer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.ModelRenderLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.main.EpicFightMod;

public class SkinLayer3DCompat implements ICompatModule {
	private static Capability<SkinLayer3DMeshes> SKIN_LAYER_3D_CAPABILITY;
	
	@Override
	public void onModEventBus(IEventBus eventBus) {
		
	}

	@Override
	public void onForgeEventBus(IEventBus eventBus) {
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		SKIN_LAYER_3D_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
		
		eventBus.<PatchedRenderersEvent.Modify>addListener((event) -> {
			if (event.get(EntityType.PLAYER) instanceof PPlayerRenderer playerrenderer) {
				playerrenderer.addPatchedLayerAlways(CustomLayerFeatureRenderer.class, new EpicFight3DSkinLayerRenderer());
			}
		});
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onForgeEventBusClient(IEventBus eventBus) {
		eventBus.addGenericListener(Entity.class, this::onCapabilityRegister);
	}
	
	@OnlyIn(Dist.CLIENT)
	public void onCapabilityRegister(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject().level().isClientSide() && event.getObject().getType() == EntityType.PLAYER) {
			event.addCapability(EpicFightMod.identifier("animated_3d_skinlayer_mesh"), new ICapabilityProvider() {
                final SkinLayer3DMeshes epicFight3dSkinLayerCapability = new SkinLayer3DMeshes();

                @Override
                public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
                    return cap == SKIN_LAYER_3D_CAPABILITY ? LazyOptional.of(() -> this.epicFight3dSkinLayerCapability).cast() : LazyOptional.empty();
                }
            });
			
			event.addListener(() -> {
				event.getObject().getCapability(SKIN_LAYER_3D_CAPABILITY).ifPresent((skinlayers3dMeshes) -> {
					skinlayers3dMeshes.partMeshes.forEach((k, v) -> v.destroy());
					skinlayers3dMeshes.partMeshes.clear();
				});
			});
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class SkinLayer3DMeshes {
		private final Map<PlayerModelPart, SkinnedMesh> partMeshes = Maps.newHashMap();
		
		public void put(PlayerModelPart playerModelPart, SkinnedMesh animatedMesh) {
			if (this.partMeshes.containsKey(playerModelPart)) {
				SkinnedMesh oldMesh = this.partMeshes.get(playerModelPart);
				
				if (oldMesh != animatedMesh) {
					oldMesh.destroy();
				}
			}
			
			this.partMeshes.put(playerModelPart, animatedMesh);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class EpicFight3DSkinLayerRenderer extends ModelRenderLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, CustomLayerFeatureRenderer, HumanoidMesh> {
		private final Map<PlayerModelPart, Function<Player, Boolean>> partVisibilities = Maps.newHashMap();
		
		public EpicFight3DSkinLayerRenderer() {
			super(null);
			
			this.partVisibilities.put(PlayerModelPart.HAT, (player) -> {
				Item item = player.getItemBySlot(EquipmentSlot.HEAD).getItem();
				return !(item instanceof BlockItem && ((BlockItem)item).getBlock() instanceof AbstractSkullBlock) && SkinLayersModBase.config.enableHat;
			});
			this.partVisibilities.put(PlayerModelPart.LEFT_PANTS_LEG, (player) -> SkinLayersModBase.config.enableLeftPants);
			this.partVisibilities.put(PlayerModelPart.RIGHT_PANTS_LEG, (player) -> SkinLayersModBase.config.enableRightPants);
			this.partVisibilities.put(PlayerModelPart.LEFT_SLEEVE, (player) -> SkinLayersModBase.config.enableLeftSleeve);
			this.partVisibilities.put(PlayerModelPart.RIGHT_SLEEVE, (player) -> SkinLayersModBase.config.enableRightSleeve);
			this.partVisibilities.put(PlayerModelPart.JACKET, (player) -> SkinLayersModBase.config.enableJacket);
		}
		
		@Override
		protected void renderLayer(AbstractClientPlayerPatch<AbstractClientPlayer> entitypatch, AbstractClientPlayer player, CustomLayerFeatureRenderer vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
			if (!player.isSkinLoaded() || player.isInvisible()) {
				return;
	        }
			
			if (Minecraft.getInstance().player.distanceToSqr(player) > SkinLayersModBase.config.renderDistanceLOD * SkinLayersModBase.config.renderDistanceLOD) {
	            return;
			}
			
			SkinLayer3DMeshes skin3dlayerMeshes = player.getCapability(SkinLayer3DCompat.SKIN_LAYER_3D_CAPABILITY, null).orElse(null);
			
			if (skin3dlayerMeshes == null) {
				return;
			}
			
			int overlay = LivingEntityRenderer.getOverlayCoords(player, 0.0f);
			
			for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
				if (playerModelPart == PlayerModelPart.CAPE) {
					continue;
				}
				
				boolean noModel = !skin3dlayerMeshes.partMeshes.containsKey(playerModelPart);
				
				if (noModel || ClientEngine.getInstance().renderEngine.shouldRenderVanillaModel()) {
					if (player instanceof PlayerSettings playerSettings) {
						switch (playerModelPart) {
						case JACKET -> {
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getTorsoMesh(), vanillaLayer.getParentModel().body, 8, 12, 4, 16, 32, true, 0));
						}
						case LEFT_SLEEVE -> {
							int armWidth = ((PlayerEntityModelAccessor)vanillaLayer.getParentModel()).hasThinArms() ? 3 : 4;
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getLeftArmMesh(), vanillaLayer.getParentModel().leftArm, armWidth, 12, 4, 48, 48, true, -2f));
						}
						case RIGHT_SLEEVE -> {
							int armWidth = ((PlayerEntityModelAccessor)vanillaLayer.getParentModel()).hasThinArms() ? 3 : 4;
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getRightArmMesh(), vanillaLayer.getParentModel().rightArm, armWidth, 12, 4, 40, 32, true, -2F));
						}
						case LEFT_PANTS_LEG -> {
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getLeftLegMesh(), vanillaLayer.getParentModel().leftLeg, 4, 12, 4, 0, 48, true, 0f));
						}
						case RIGHT_PANTS_LEG -> {
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getRightLegMesh(), vanillaLayer.getParentModel().rightLeg, 4, 12, 4, 0, 32, true, 0f));
						}
						case HAT -> {
							skin3dlayerMeshes.put(playerModelPart, createEpicFight3DSkinLayer(player, playerModelPart, playerSettings.getHeadMesh(), vanillaLayer.getParentModel().head, 8, 8, 8, 32, 0, false, 0.6F));
						}
						default -> {}
						}
					}
					
					//Initialize model
					if (noModel) {
						ClientEngine.getInstance().renderEngine.setModelInitializerTimer(60);
					}
				}
				
				if (this.partVisibilities.get(playerModelPart).apply(player)) {
					SkinnedMesh mesh = skin3dlayerMeshes.partMeshes.get(playerModelPart);
					
					if (mesh != null) {
						mesh.draw(poseStack, buffer, RenderType.entityTranslucent(player.getSkinTextureLocation(), true), packedLight, 1.0F, 1.0F, 1.0F, 1.0F, overlay, entitypatch.getArmature(), poses);
					}
				}
			}
		}
		
		private static SkinnedMesh createEpicFight3DSkinLayer(AbstractClientPlayer player, PlayerModelPart playerModelPart, Mesh skinlayerModelPart, ModelPart vanillaModelPart, int width, int height, int depth, int textureU, int textureV, boolean topPivot, float rotationOffset) {
            CustomizableCubeListBuilder builder = new CustomizableCubeListBuilder();
            ResourceLocation skinLocation = PlayerUtil.getPlayerSkin(player);
			NativeImage skinImage = SkinUtil.getTexture(skinLocation, null);
            
            if (SolidPixelWrapper.wrapBox(builder, new WrappedNativeImage(skinImage), width, height, depth, textureU, textureV, topPivot, rotationOffset) != null) {
                return SkinLayer3DTransformer.transformMesh(
	                			player
	                		 , (skinlayerModelPart == null) ? new CustomizableModelPart(builder.getVanillaCubes(), builder.getCubes(), Collections.emptyMap()) : (CustomizableModelPart)skinlayerModelPart
	                		 , vanillaModelPart
	                		 , playerModelPart
	                		 , builder.getVanillaCubes()
	                		 , builder.getCubes()
                	   );
            }
            
            return null;
        }
	}
}