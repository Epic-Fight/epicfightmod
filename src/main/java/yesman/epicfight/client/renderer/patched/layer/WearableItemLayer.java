package yesman.epicfight.client.renderer.patched.layer;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.client.forgeevent.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Mesh.DrawingFunction;
import yesman.epicfight.api.client.model.MeshProvider;
import yesman.epicfight.api.client.model.transformer.CustomModelBakery;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.model.JsonModelLoader;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@OnlyIn(Dist.CLIENT)
public class WearableItemLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends HumanoidModel<E>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, HumanoidArmorLayer<E, M, M>, AM> {
	private static final Map<ResourceLocation, AnimatedMesh> ARMOR_MODELS = Maps.newHashMap();
	private static final Map<String, ResourceLocation> EPICFIGHT_OVERRIDING_TEXTURES = Maps.newHashMap();
	
	public static void clearModels() {
		ARMOR_MODELS.values().forEach(AnimatedMesh::destroy);
		ARMOR_MODELS.clear();
		EPICFIGHT_OVERRIDING_TEXTURES.clear();
	}
	
	public static void putModel(ResourceLocation rl, AnimatedMesh animatedMesh) {
		if (ARMOR_MODELS.containsKey(rl)) {
			AnimatedMesh oldModel = ARMOR_MODELS.get(rl);
			
			if (oldModel != animatedMesh) {
				ARMOR_MODELS.get(rl).destroy();
			}
		}
		
		ARMOR_MODELS.put(rl, animatedMesh);
	}
	
	private final boolean firstPersonModel;
	private final TextureAtlas armorTrimAtlas;
	
	public WearableItemLayer(MeshProvider<AM> meshProvider, boolean firstPersonModel, ModelManager modelManager) {
		super(meshProvider);
		
		this.firstPersonModel = firstPersonModel;
		this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
	}
	
	private void renderArmor(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AnimatedMesh model, Armature armature, float r, float g, float b, ResourceLocation armorTexture, OpenMatrix4f[] poses) {
		model.draw(poseStack, multiBufferSource, RenderType.armorCutoutNoCull(armorTexture), packedLight, r, g, b, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}
	
	private void renderGlint(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AnimatedMesh model, Armature armature, OpenMatrix4f[] poses) {
		model.draw(poseStack, multiBufferSource, RenderType.armorEntityGlint(), packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}
	
	private void renderTrim(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, AnimatedMesh model, Armature armature, ArmorMaterial armorMaterial, ArmorTrim armorTrim, EquipmentSlot slot, OpenMatrix4f[] poses) {
		TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(innerModel(slot) ? armorTrim.innerTexture(armorMaterial) : armorTrim.outerTexture(armorMaterial));
		VertexConsumer vertexConsumer = textureatlassprite.wrap(multiBufferSource.getBuffer(EpicFightRenderTypes.getTriangulated(Sheets.armorTrimsSheet())));
		model.drawToBuffer(poseStack, vertexConsumer, DrawingFunction.ENTITY_TEXTURED, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void renderLayer(T entitypatch, E entityliving, HumanoidArmorLayer<E, M, M> vanillaLayer, PoseStack poseStack, MultiBufferSource buf, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.ARMOR) {
				continue;
			}
			
			boolean firstPersonChest = false;
			
			if (entitypatch.isFirstPerson() && this.firstPersonModel) {
				if (slot != EquipmentSlot.CHEST) {
					continue;
				} else {
					firstPersonChest = true;
				}
			}
			
			if (slot == EquipmentSlot.HEAD && this.firstPersonModel) {
				continue;
			}
			
			ItemStack itemstack = entityliving.getItemBySlot(slot);
			Item item = itemstack.getItem();
			
			if (item instanceof ArmorItem armorItem) {
				if (slot != armorItem.getEquipmentSlot()) {
					return;
				}
				
				poseStack.pushPose();
				float head = 0.0F;
				
				if (slot == EquipmentSlot.HEAD) {
					poseStack.translate(0.0D, head * 0.055D, 0.0D);
				}
				
				M defaultModel = vanillaLayer.getArmorModel(slot);
				Model armorModel = ForgeHooksClient.getArmorModel(entityliving, itemstack, slot, defaultModel);
				AnimatedMesh armorMesh = this.getArmorModel(vanillaLayer, defaultModel, armorModel, entityliving, armorItem, itemstack, slot);
				
				if (armorMesh == null) {
					poseStack.popPose();
					return;
				}

				if (armorModel instanceof HumanoidModel humanoidModel) {
					boolean isArmorStandModel = armorModel instanceof net.minecraft.client.model.ArmorStandArmorModel;
					boolean isEntityArmorStand = entityliving instanceof net.minecraft.world.entity.decoration.ArmorStand;

					if (!(isArmorStandModel && !isEntityArmorStand)) {
						boolean shouldSit = entityliving.isPassenger() && (entityliving.getVehicle() != null && entityliving.getVehicle().shouldRiderSit());
						float f8 = 0.0F;
						float f5 = 0.0F;

						if (!shouldSit && entityliving.isAlive()) {
							f8 = entityliving.walkAnimation.speed(partialTicks);
							f5 = entityliving.walkAnimation.position(partialTicks);

							if (entityliving.isBaby()) {
								f5 *= 3.0F;
							}

							if (f8 > 1.0F) {
								f8 = 1.0F;
							}
						}

						humanoidModel.setupAnim(entityliving, f8, f5, bob, yRot, xRot);
						humanoidModel.head.loadPose(humanoidModel.head.getInitialPose());
						humanoidModel.hat.loadPose(humanoidModel.hat.getInitialPose());
						humanoidModel.body.loadPose(humanoidModel.body.getInitialPose());
						humanoidModel.leftArm.loadPose(humanoidModel.leftArm.getInitialPose());
						humanoidModel.rightArm.loadPose(humanoidModel.rightArm.getInitialPose());
						humanoidModel.leftLeg.loadPose(humanoidModel.leftLeg.getInitialPose());
						humanoidModel.rightLeg.loadPose(humanoidModel.rightLeg.getInitialPose());
					}
				}

				armorMesh.initialize();
				
				if (firstPersonChest) {
					armorMesh.getAllParts().forEach(part -> part.setHidden(true));
					
					if (armorMesh.hasPart("leftArm")) {
						armorMesh.getPart("leftArm").setHidden(false);
					}
					
					if (armorMesh.hasPart("rightArm")) {
						armorMesh.getPart("rightArm").setHidden(false);
					}
				}
				
				if (armorItem instanceof DyeableLeatherItem dyeableItem) {
					int i = dyeableItem.getColor(itemstack);
					float r = (float) (i >> 16 & 255) / 255.0F;
					float g = (float) (i >> 8 & 255) / 255.0F;
					float b = (float) (i & 255) / 255.0F;
					
					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), r, g, b, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, null, defaultModel), poses);
					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, "overlay", defaultModel), poses);
				} else {
					this.renderArmor(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), 1.0F, 1.0F, 1.0F, this.getArmorTexture(itemstack, entityliving, armorMesh, slot, null, defaultModel), poses);
				}

				ArmorTrim.getTrim(entityliving.level().registryAccess(), itemstack).ifPresent((armorTrim) -> {
					this.renderTrim(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), armorItem.getMaterial(), armorTrim, slot, poses);
				});
				
				if (itemstack.hasFoil()) {
					this.renderGlint(poseStack, buf, packedLight, armorMesh, entitypatch.getArmature(), poses);
				}
				
				poseStack.popPose();
			}
		}
	}
	
	private AnimatedMesh getArmorModel(HumanoidArmorLayer<E, M, M> originalRenderer, M originalModel, Model forgeHooksArmorModel, E entityliving, ArmorItem armorItem, ItemStack itemstack, EquipmentSlot slot) {
		ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(armorItem);
		
		if (ARMOR_MODELS.containsKey(registryName) && !ClientEngine.getInstance().renderEngine.shouldRenderVanillaModel()) {
			return ARMOR_MODELS.get(registryName);
		} else {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			ResourceLocation rl = new ResourceLocation(ForgeRegistries.ITEMS.getKey(armorItem).getNamespace(), "animmodels/armor/" + ForgeRegistries.ITEMS.getKey(armorItem).getPath() + ".json");
			AnimatedMesh animatedMesh = null;
			
			if (resourceManager.getResource(rl).isPresent()){
				JsonModelLoader modelLoader = new JsonModelLoader(resourceManager, rl);
				animatedMesh = modelLoader.loadAnimatedMesh(AnimatedMesh::new);
			} else {
				Iterable<ItemStack> armorItems = entityliving.getArmorSlots();
				ItemStack head = entityliving.getItemBySlot(EquipmentSlot.HEAD);
				ItemStack chest = entityliving.getItemBySlot(EquipmentSlot.CHEST);
				ItemStack legs = entityliving.getItemBySlot(EquipmentSlot.LEGS);
				ItemStack feet = entityliving.getItemBySlot(EquipmentSlot.FEET);
				
				if (armorItems instanceof List<ItemStack> armorItemList) {
					armorItemList.set(0, ItemStack.EMPTY);
					armorItemList.set(1, ItemStack.EMPTY);
					armorItemList.set(2, ItemStack.EMPTY);
					armorItemList.set(3, ItemStack.EMPTY);
					armorItemList.set(slot.getIndex(), itemstack);
				}

				PoseStack ps = new PoseStack();
				ps.translate(0, 0, 10000);
				
				if (forgeHooksArmorModel instanceof HumanoidModel<?> humanoidModel) {
					//Setup default visibility
					switch (slot) {
					case FEET -> {
						humanoidModel.rightLeg.visible = true;
						humanoidModel.leftLeg.visible = true;
					}
					case LEGS -> {
						humanoidModel.body.visible = true;
						humanoidModel.rightLeg.visible = true;
						humanoidModel.leftLeg.visible = true;
					}
					case CHEST -> {
						humanoidModel.body.visible = true;
						humanoidModel.rightArm.visible = true;
						humanoidModel.leftArm.visible = true;
					}
					case HEAD -> {
						humanoidModel.head.visible = true;
						humanoidModel.hat.visible = true;
					}
					default -> {}
					}
				}
				
				//Render armor to get the visibility of each part
				originalRenderer.render(ps, Minecraft.getInstance().renderBuffers().bufferSource(), 0, entityliving, 0, 0, 0, 0, 0, 0);
				
				if (armorItems instanceof List<ItemStack> armorItemList) {
					armorItemList.set(0, feet);
					armorItemList.set(1, legs);
					armorItemList.set(2, chest);
					armorItemList.set(3, head);
				}
				
				animatedMesh = CustomModelBakery.bakeArmor(entityliving, itemstack, armorItem, slot, originalModel, forgeHooksArmorModel, originalRenderer.getParentModel(), this.mesh.get());
			}
			
			putModel(registryName, animatedMesh);
			
			return animatedMesh;
		}
	}
	
	private ResourceLocation getArmorTexture(ItemStack itemstack, LivingEntity entity, AnimatedMesh armorMesh, EquipmentSlot slot, String type, M originalModel) {
		ArmorItem item = (ArmorItem) itemstack.getItem();
		String texture = item.getMaterial().getName();
		String domain = "minecraft";
		int idx = texture.indexOf(':');
		
		if (idx != -1) {
			domain = texture.substring(0, idx);
			texture = texture.substring(idx + 1);
		}
		
		String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (innerModel(slot) ? 2 : 1), type == null ? "" : String.format("_%s", type));
		s1 = ForgeHooksClient.getArmorTexture(entity, itemstack, s1, slot, type);
		int idx2 = s1.lastIndexOf('/');
		String s2 = String.format("%s/epicfight/%s", s1.substring(0, idx2), s1.substring(idx2 + 1));
		ResourceLocation resourcelocation2 = EPICFIGHT_OVERRIDING_TEXTURES.get(s2);
		
		if (resourcelocation2 != null) {
			return resourcelocation2;
		} else if (!EPICFIGHT_OVERRIDING_TEXTURES.containsKey(s2)) {
			resourcelocation2 = new ResourceLocation(s2);
			ResourceManager rm = Minecraft.getInstance().getResourceManager();
			if (rm.getResource(resourcelocation2).isPresent()){
				EPICFIGHT_OVERRIDING_TEXTURES.put(s2, resourcelocation2);
				return resourcelocation2;
			} else {
				EPICFIGHT_OVERRIDING_TEXTURES.put(s2, null);
			}
		}
		
		AnimatedArmorTextureEvent animatedArmorTextureEvent = new AnimatedArmorTextureEvent(entity, itemstack, slot, originalModel);
		MinecraftForge.EVENT_BUS.post(animatedArmorTextureEvent);
		ResourceLocation extensionTexturePath = animatedArmorTextureEvent.getResultLocation();
		
		if (armorMesh.getRenderProperty() != null && armorMesh.getRenderProperty().getCustomTexturePath() != null) {
			s1 = armorMesh.getRenderProperty().getCustomTexturePath();
			extensionTexturePath = null;
		}
		
		if (extensionTexturePath != null) {
			return extensionTexturePath;
		}
		
		ResourceLocation resourcelocation = HumanoidArmorLayer.ARMOR_LOCATION_CACHE.get(s1);
		
		if (resourcelocation == null) {
			resourcelocation = new ResourceLocation(s1);
			HumanoidArmorLayer.ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
		}
		
		return resourcelocation;
	}
	
	private static boolean innerModel(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS;
	}
}
