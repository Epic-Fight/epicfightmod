package yesman.epicfight.compat.mca.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.layer.VillagerLayer;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;

public class MCAPatchedLayer extends PatchedLayer<VillagerEntityMCA, HumanoidMobPatch<VillagerEntityMCA>, VillagerEntityModelMCA<VillagerEntityMCA>, VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>>> {
    final PMCAVillagerRenderer parent;
    final VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer;
    MCAPatchedLayer(PMCAVillagerRenderer parent, VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer)
    {
        this.parent = parent;
        this.vanillaLayer = vanillaLayer;
    }
    @Override
    protected void renderLayer(HumanoidMobPatch<VillagerEntityMCA> entitypatch, VillagerEntityMCA villager, @Nullable VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> vanillaLayer, PoseStack poseStack, MultiBufferSource buffer, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
        ResourceLocation resourceLocation = this.vanillaLayer.getSkin(villager);
        if (resourceLocation != null)
        {
            int packerOverlay = LivingEntityRenderer.getOverlayCoords(villager, 0);
            RenderType renderType = RenderType.entityCutoutNoCull(resourceLocation);
            int color = this.vanillaLayer.getColor(villager, partialTicks);
            int a = (color >> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            float alpha = a / 255.0F;
            float red   = r / 255.0F;
            float green = g / 255.0F;
            float blue  = b / 255.0F;
            parent.getDefaultMesh().get().draw(poseStack, buffer, renderType, packedLight, red, green, blue, alpha, packerOverlay, entitypatch.getArmature(), poses);
        }

    }
}
