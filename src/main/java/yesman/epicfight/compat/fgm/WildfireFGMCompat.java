package yesman.epicfight.compat.fgm;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wildfire.api.IGenderArmor;
import com.wildfire.main.Breasts;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.WildfireHelper;
import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.physics.BreastPhysics;
import com.wildfire.render.GenderLayer;
import com.wildfire.render.WildfireModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.client.forgeevent.PatchedRenderersEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.entity.PPlayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.model.armature.HumanoidArmature;

import java.util.Locale;

public class WildfireFGMCompat implements ICompatModule {
    @Override
    public void onModEventBus(IEventBus eventBus) {

    }

    @Override
    public void onForgeEventBus(IEventBus eventBus) {

    }

    @Override
    public void onModEventBusClient(IEventBus eventBus) {
        eventBus.<PatchedRenderersEvent.Modify>addListener((event) -> {
            if (event.get(EntityType.PLAYER) instanceof PPlayerRenderer playerrenderer) {
                playerrenderer.addPatchedLayerAlways(GenderLayer.class, new EpicFightWildfireRenderLayer());
            }
        });
    }

    @Override
    public void onForgeEventBusClient(IEventBus eventBus) {

    }

    public static class EpicFightWildfireRenderLayer extends PatchedLayer<AbstractClientPlayer, AbstractClientPlayerPatch<AbstractClientPlayer>, PlayerModel<AbstractClientPlayer>, GenderLayer> {
        private static final WildfireModelRenderer.OverlayModelBox lBreastWear = new WildfireModelRenderer.OverlayModelBox(true, 64, 64, 17, 34, -4.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
        private static final WildfireModelRenderer.OverlayModelBox rBreastWear = new WildfireModelRenderer.OverlayModelBox(false, 64, 64, 21, 34, 0.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
        private static final WildfireModelRenderer.BreastModelBox lBoobArmor = new WildfireModelRenderer.BreastModelBox(64, 32, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);
        private static final WildfireModelRenderer.BreastModelBox rBoobArmor = new WildfireModelRenderer.BreastModelBox(64, 32, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, 3, 0.0F, false);

        WildfireModelRenderer.BreastModelBox lB;
        WildfireModelRenderer.BreastModelBox rB;
        float preSize;


        @Override
        protected void renderLayer(AbstractClientPlayerPatch<AbstractClientPlayer> entityPatch, AbstractClientPlayer entity, GenderLayer vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {

                if (!(Boolean) GeneralClientConfig.INSTANCE.disableRendering.get() && !entity.isSpectator()) {
                    try {
                        GenderPlayer plr = WildfireGender.getPlayerById(entity.getUUID());
                        if (plr == null) {
                            return;
                        }

                        ItemStack armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
                        IGenderArmor genderArmor = WildfireHelper.getArmorConfig(armorStack);
                        boolean isChestplateOccupied = genderArmor.coversBreasts();
                        if (genderArmor.alwaysHidesBreasts() || !plr.showBreastsInArmor() && isChestplateOccupied) {
                            return;
                        }

                        Minecraft minecraft = Minecraft.getInstance();
                        PlayerRenderer rend = (PlayerRenderer)minecraft.getEntityRenderDispatcher().getRenderer(entity);
                        PlayerModel<?> model = rend.getModel();
                        Breasts breasts = plr.getBreasts();
                        float breastOffsetX = (float)Math.round((float)Math.round(breasts.getXOffset() * 100.0F) / 100.0F * 10.0F) / 10.0F;
                        float breastOffsetY = (float)(-Math.round((float)Math.round(breasts.getYOffset() * 100.0F) / 100.0F * 10.0F)) / 10.0F;
                        float breastOffsetZ = (float)(-Math.round((float)Math.round(breasts.getZOffset() * 100.0F) / 100.0F * 10.0F)) / 10.0F;
                        BreastPhysics leftBreastPhysics = plr.getLeftBreastPhysics();
                        float bSize = leftBreastPhysics.getBreastSize(partialTicks);
                        float outwardAngle = (float)Math.round(breasts.getCleavage() * 100.0F) / 100.0F * 100.0F;
                        outwardAngle = Math.min(outwardAngle, 10.0F);
                        float reducer = 0.0F;
                        if (bSize < 0.84F) {
                            ++reducer;
                        }

                        if (bSize < 0.72F) {
                            ++reducer;
                        }

                        if (this.preSize != bSize) {
                            this.lB = new WildfireModelRenderer.BreastModelBox(64, 64, 16, 17, -4.0F, 0.0F, 0.0F, 4, 5, (int)(4.0F - breastOffsetZ - reducer), 0.0F, false);
                            this.rB = new WildfireModelRenderer.BreastModelBox(64, 64, 20, 17, 0.0F, 0.0F, 0.0F, 4, 5, (int)(4.0F - breastOffsetZ - reducer), 0.0F, false);
                            this.preSize = bSize;
                        }

                        float overlayAlpha = entity.isInvisible() ? 0.15F : 1.0F;
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                        float lTotal = Mth.lerp(partialTicks, leftBreastPhysics.getPreBounceY(), leftBreastPhysics.getBounceY());
                        float lTotalX = Mth.lerp(partialTicks, leftBreastPhysics.getPreBounceX(), leftBreastPhysics.getBounceX());
                        float leftBounceRotation = Mth.lerp(partialTicks, leftBreastPhysics.getPreBounceRotation(), leftBreastPhysics.getBounceRotation());
                        float rTotal;
                        float rTotalX;
                        float rightBounceRotation;
                        if (breasts.isUniboob()) {
                            rTotal = lTotal;
                            rTotalX = lTotalX;
                            rightBounceRotation = leftBounceRotation;
                        } else {
                            BreastPhysics rightBreastPhysics = plr.getRightBreastPhysics();
                            rTotal = Mth.lerp(partialTicks, rightBreastPhysics.getPreBounceY(), rightBreastPhysics.getBounceY());
                            rTotalX = Mth.lerp(partialTicks, rightBreastPhysics.getPreBounceX(), rightBreastPhysics.getBounceX());
                            rightBounceRotation = Mth.lerp(partialTicks, rightBreastPhysics.getPreBounceRotation(), rightBreastPhysics.getBounceRotation());
                        }

                        float breastSize = bSize * 1.5F;
                        if (breastSize > 0.7F) {
                            breastSize = 0.7F;
                        }

                        if (bSize > 0.7F) {
                            breastSize = bSize;
                        }

                        if (breastSize < 0.02F) {
                            return;
                        }

                        float zOff = 0.0625F - bSize * 0.0625F;
                        breastSize = bSize + 0.5F * Math.abs(bSize - 0.7F) * 2.0F;
                        float resistance = plr.getArmorPhysicsOverride() ? 0.0F : Mth.clamp(genderArmor.physicsResistance(), 0.0F, 1.0F);
                        boolean breathingAnimation = resistance <= 0.5F && (!entity.isUnderWater() || MobEffectUtil.hasWaterBreathing(entity) || entity.level().getBlockState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ())).is(Blocks.BUBBLE_COLUMN));
                        boolean bounceEnabled = plr.hasBreastPhysics() && (!isChestplateOccupied || resistance < 1.0F);
                        int combineTex = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
                        ResourceLocation entityTexture = entity.getSkinTextureLocation();
                        boolean bodyVisible = !entity.isInvisible();
                        boolean translucent = !bodyVisible && minecraft.player != null && !entity.isInvisibleTo(minecraft.player);
                        RenderType type;
                        if (translucent) {
                            type = RenderType.itemEntityTranslucentCull(entityTexture);
                        } else if (bodyVisible) {
                            type = RenderType.entityTranslucent(entityTexture);
                        } else if (minecraft.shouldEntityAppearGlowing(entity)) {
                            type = RenderType.outline(entityTexture);
                        } else {
                            if (!isChestplateOccupied) {
                                return;
                            }

                            type = null;
                        }

                        this.renderBreastWithTransforms(entityPatch, model.body, armorStack, poseStack, buffer, type, packedLight, combineTex, overlayAlpha, bounceEnabled, lTotalX, lTotal, leftBounceRotation, breastSize, breastOffsetX, breastOffsetY, breastOffsetZ, zOff, outwardAngle, breasts.isUniboob(), isChestplateOccupied, breathingAnimation, true, partialTicks);
                        this.renderBreastWithTransforms(entityPatch, model.body, armorStack, poseStack, buffer, type, packedLight, combineTex, overlayAlpha, bounceEnabled, rTotalX, rTotal, rightBounceRotation, breastSize, -breastOffsetX, breastOffsetY, breastOffsetZ, zOff, -outwardAngle, breasts.isUniboob(), isChestplateOccupied, breathingAnimation, false, partialTicks);
                        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    } catch (Exception e) {
                    }
                }
        }

        private void renderBreastWithTransforms(AbstractClientPlayerPatch<?> entity, ModelPart body, ItemStack armorStack, PoseStack poseStack, MultiBufferSource bufferSource, @javax.annotation.Nullable RenderType breastRenderType, int packedLightIn, int combineTex, float alpha, boolean bounceEnabled, float totalX, float total, float bounceRotation, float breastSize, float breastOffsetX, float breastOffsetY, float breastOffsetZ, float zOff, float outwardAngle, boolean uniboob, boolean isChestplateOccupied, boolean breathingAnimation, boolean left, float partialTicks) {
            poseStack.pushPose();
            if (entity.getArmature() instanceof HumanoidArmature armature)
            {
                try {
                    Joint chest = armature.chest;
                    Matrix4f transform = OpenMatrix4f.exportToMojangMatrix(armature.getBoundTransformFor(entity.getAnimator().getPose(partialTicks), chest));
                    Vector3f translationVector = transform.getTranslation(new Vector3f());
                    poseStack.translate(translationVector.x, translationVector.y, translationVector.z);
                    poseStack.mulPose(transform.getNormalizedRotation(new Quaternionf()).rotateXYZ(Mth.PI, Mth.PI, 0));
                    matrixStack.translate(0, -0.35, 0.00125);

                    Vector3f scaleVector = transform.getScale(new Vector3f());
                    matrixStack.scale(scaleVector.x, scaleVector.y, scaleVector.z - 0.0125f);

                    if (bounceEnabled) {
                        poseStack.translate(totalX / 32.0F, 0.0F, 0.0F);
                        poseStack.translate(0.0F, total / 32.0F, 0.0F);
                    }

                    poseStack.translate(breastOffsetX * 0.0625F, 0.05625F + breastOffsetY * 0.0625F, zOff - 0.125F + breastOffsetZ * 0.0625F);
                    if (!uniboob) {
                        poseStack.translate(-0.125F * (float)(left ? 1 : -1), 0.0F, 0.0F);
                    }

                    if (bounceEnabled) {
                        poseStack.mulPose((new Quaternionf()).rotationXYZ(0.0F, (float)((double)bounceRotation * (Math.PI / 180D)), 0.0F));
                    }

                    if (!uniboob) {
                        poseStack.translate(0.125F * (float)(left ? 1 : -1), 0.0F, 0.0F);
                    }

                    float rotationMultiplier = 0.0F;
                    if (bounceEnabled) {
                        poseStack.translate(0.0F, -0.035F * breastSize, 0.0F);
                        rotationMultiplier = -total / 12.0F;
                    }

                    float totalRotation = breastSize + rotationMultiplier;
                    if (!bounceEnabled) {
                        totalRotation = breastSize;
                    }

                    if (totalRotation > breastSize + 0.2F) {
                        totalRotation = breastSize + 0.2F;
                    }

                    totalRotation = Math.min(totalRotation, 1.0F);
                    if (isChestplateOccupied) {
                        poseStack.translate(0.0F, 0.0F, 0.01F);
                    }

                    poseStack.mulPose((new Quaternionf()).rotationXYZ(0.0F, (float)((double)outwardAngle * (Math.PI / 180D)), 0.0F));
                    poseStack.mulPose((new Quaternionf()).rotationXYZ((float)((double)(-35.0F * totalRotation) * (Math.PI / 180D)), 0.0F, 0.0F));
                    if (breathingAnimation) {
                        float f5 = -Mth.cos((float)entity.getOriginal().tickCount * 0.09F) * 0.45F + 0.45F;
                        poseStack.mulPose((new Quaternionf()).rotationXYZ((float)((double)f5 * (Math.PI / 180D)), 0.0F, 0.0F));
                    }

                    poseStack.scale(0.9995F, 1.0F, 1.0F);
                    this.renderBreast(entity, armorStack, poseStack, bufferSource, breastRenderType, packedLightIn, combineTex, alpha, left);
                } catch (Exception e) {
                    
                }
            }
            poseStack.popPose();
        }

        private void shiftForJacket(PoseStack poseStack) {
            poseStack.translate(0.0F, 0.0F, -0.015F);
            poseStack.scale(1.05F, 1.05F, 1.05F);
        }

        public ResourceLocation getArmorResource(AbstractClientPlayer entity, ItemStack stack, EquipmentSlot slot, @javax.annotation.Nullable String type) {
            ArmorItem item = (ArmorItem)stack.getItem();
            String texture = item.getMaterial().getName();
            String domain = "minecraft";
            int idx = texture.indexOf(58);
            if (idx != -1) {
                domain = texture.substring(0, idx);
                texture = texture.substring(idx + 1);
            }

            String s1 = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, slot == EquipmentSlot.LEGS ? 2 : 1, type == null ? "" : String.format(Locale.ROOT, "_%s", type));
            s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
            return ResourceLocation.parse(s1);
        }

        private void renderBreast(AbstractClientPlayerPatch<?> entity, ItemStack armorStack, PoseStack poseStack, MultiBufferSource bufferSource, @Nullable RenderType breastRenderType, int light, int overlay, float alpha, boolean left) {
            if (breastRenderType != null) {
                VertexConsumer vertexConsumer = bufferSource.getBuffer(breastRenderType);
                renderBox(left ? this.lB : this.rB, poseStack, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
                if (entity.getOriginal().isModelPartShown(PlayerModelPart.JACKET)) {
                    poseStack.translate(0.0F, 0.0F, -0.015F);
                    poseStack.scale(1.05F, 1.05F, 1.05F);
                    renderBox(left ? lBreastWear : rBreastWear, poseStack, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, alpha);
                }
            }

            if (!armorStack.isEmpty()) {
                Item var11 = armorStack.getItem();
                if (var11 instanceof ArmorItem armorItem) {
                    ResourceLocation armorTexture = this.getArmorResource(entity.getOriginal(), armorStack, EquipmentSlot.CHEST, (String)null);
                    ResourceLocation overlayTexture = null;
                    float armorR = 1.0F;
                    float armorG = 1.0F;
                    float armorB = 1.0F;
                    if (armorItem instanceof DyeableLeatherItem) {
                        DyeableLeatherItem dyeableItem = (DyeableLeatherItem)armorItem;
                        overlayTexture = this.getArmorResource(entity.getOriginal(), armorStack, EquipmentSlot.CHEST, "overlay");
                        int color = dyeableItem.getColor(armorStack);
                        armorR = (float)(color >> 16 & 255) / 255.0F;
                        armorG = (float)(color >> 8 & 255) / 255.0F;
                        armorB = (float)(color & 255) / 255.0F;
                    }

                    poseStack.pushPose();
                    poseStack.translate(left ? 0.001F : -0.001F, 0.015F, -0.015F);
                    poseStack.scale(1.05F, 1.0F, 1.0F);
                    WildfireModelRenderer.BreastModelBox armor = left ? this.lBoobArmor : this.rBoobArmor;
                    RenderType armorType = RenderType.armorCutoutNoCull(armorTexture);
                    VertexConsumer armorVertexConsumer = bufferSource.getBuffer(armorType);
                    renderBox(armor, poseStack, armorVertexConsumer, light, OverlayTexture.NO_OVERLAY, armorR, armorG, armorB, 1.0F);
                    if (overlayTexture != null) {
                        RenderType overlayType = RenderType.armorCutoutNoCull(overlayTexture);
                        VertexConsumer overlayVertexConsumer = bufferSource.getBuffer(overlayType);
                        renderBox(armor, poseStack, overlayVertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                    }

//                    ArmorTrim.getTrim(entity.getOriginal().level().registryAccess(), armorStack).ifPresent((trim) -> {
//                        ArmorMaterial armorMaterial = armorItem.getMaterial();
//                        TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(trim.outerTexture(armorMaterial));
//                        VertexConsumer trimVertexConsumer = sprite.wrap(bufferSource.getBuffer(Sheets.armorTrimsSheet()));
//                        renderBox(armor, poseStack, trimVertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
//                    });
                    if (armorStack.hasFoil()) {
                        renderBox(armor, poseStack, bufferSource.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                    }

                    poseStack.popPose();
                }
            }
        }
        private static void renderBox(WildfireModelRenderer.ModelBox model, PoseStack poseStack, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f matrix3f = poseStack.last().normal();

            for(WildfireModelRenderer.TexturedQuad quad : model.quads) {
                Vector3f vector3f = new Vector3f((float)quad.normal.getX(), (float)quad.normal.getY(), (float)quad.normal.getZ());
                vector3f.mul(matrix3f);

                for(WildfireModelRenderer.PositionTextureVertex vertex : quad.vertexPositions) {
                    bufferIn.vertex(matrix4f, vertex.x() / 16.0F, vertex.y() / 16.0F, vertex.z() / 16.0F);
                    bufferIn.color(red, green, blue, alpha);
                    bufferIn.uv(vertex.texturePositionX(), vertex.texturePositionY());
                    bufferIn.overlayCoords(packedOverlayIn);
                    bufferIn.uv2(packedLightIn);
                    bufferIn.normal(vector3f.x(), vector3f.y(), vector3f.z());
                    bufferIn.endVertex();
                }
            }
        }
    }


}
