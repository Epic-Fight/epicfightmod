package yesman.epicfight.client.renderer.patched.entity;

import net.minecraft.client.model.HoglinModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.joml.Matrix4fUtils;
import yesman.epicfight.client.mesh.HoglinMesh;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

@OnlyIn(Dist.CLIENT)
public class PHoglinRenderer<E extends Mob & HoglinBase, T extends MobPatch<E>> extends PatchedLivingEntityRenderer<E, T, HoglinModel<E>, MobRenderer<E, HoglinModel<E>>, HoglinMesh> {
	public PHoglinRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
		super(context, entityType);
	}
	
	@Override
	public void setJointTransforms(T entitypatch, Armature armature, Pose pose, float partialTicks) {
		if (entitypatch.getOriginal().isBaby()) {
			pose.orElseEmpty("Head").frontResult(JointTransform.scale(new Vector3f(1.25F, 1.25F, 1.25F)), Matrix4fUtils::mulBoth);
		}
	}
	
	@Override
	public AssetAccessor<HoglinMesh> getDefaultMesh() {
		return Meshes.HOGLIN;
	}
}