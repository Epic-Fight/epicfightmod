package yesman.epicfight.client.renderer;

import java.util.Optional;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@OnlyIn(Dist.CLIENT)
public class AimHelperRenderer {
	public void doRender(PoseStack poseStack, float partialTick) {
		if (!ClientConfig.enableAimHelper) {
			return;
		}
		
		Minecraft minecraft = Minecraft.getInstance();
		Entity entity = minecraft.player;
		Optional<LocalPlayerPatch> entitypatchOpt = EpicFightCapabilities.getUnparameterizedEntityPatch(entity, LocalPlayerPatch.class);
		HitResult rayHit = entitypatchOpt.isEmpty() ? entity.pick(200.D, partialTick, false) : pick(entitypatchOpt.get(), 200.0D, partialTick);
		
		Vec3 vec3 = rayHit.getLocation();
		Vec3f pos1 = new Vec3f(
			  (float)Mth.lerp(partialTick, entity.xOld, entity.getX())
			, (float)Mth.lerp(partialTick, entity.yOld, entity.getY()) + entity.getEyeHeight() - 0.15F
			, (float)Mth.lerp(partialTick, entity.zOld, entity.getZ())
		);
		Vec3f pos2 = new Vec3f((float)vec3.x, (float)vec3.y, (float)vec3.z);
		
		Camera renderInfo = minecraft.gameRenderer.getMainCamera();
		Vec3 projectedView = renderInfo.getPosition();
		poseStack.pushPose();
		poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
		
		Matrix4f matrix = poseStack.last().pose();
		
		int color = ClientConfig.aimHelperPackedColor;
		float f1 = (float)(color >> 16 & 255) / 255.0F;
		float f2 = (float)(color >> 8 & 255) / 255.0F;
		float f3 = (float)(color & 255) / 255.0F;
		
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		RenderSystem.enableBlend();
        RenderSystem.disableCull();
		RenderSystem.lineWidth(2.0F);
		
		BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		bufferBuilder.addVertex(matrix, pos2.x, pos2.y, pos2.z).setColor(f1, f2, f3, 0.5F).setNormal(pos2.x - pos1.x, pos2.y - pos1.y, pos2.z - pos1.z);
		bufferBuilder.addVertex(matrix, pos1.x, pos1.y, pos1.z).setColor(f1, f2, f3, 0.5F).setNormal(pos2.x - pos1.x, pos2.y - pos1.y, pos2.z - pos1.z);
		BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
		
		poseStack.popPose();
	}
	
	public static HitResult pick(LocalPlayerPatch entitypatch, double hitDistance, float partialTick) {
        Vec3 vec3 = entitypatch.getOriginal().getEyePosition(partialTick);
        Vec3 vec31 = entitypatch.getViewVector(partialTick);
        Vec3 vec32 = vec3.add(vec31.x * hitDistance, vec31.y * hitDistance, vec31.z * hitDistance);
        
        return entitypatch.getLevel().clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entitypatch.getOriginal()));
    }
}
