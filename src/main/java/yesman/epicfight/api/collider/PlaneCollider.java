package yesman.epicfight.api.collider;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Deprecated
public class PlaneCollider extends Collider {
	static AABB getInitialAABB(double center_x, double center_y, double center_z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
		double xLength = Math.max(Math.abs(aX), Math.abs(bX)) + Math.abs(center_x);
		double yLength = Math.max(Math.abs(aY), Math.abs(bY)) + Math.abs(center_y);
		double zLength = Math.max(Math.abs(aZ), Math.abs(bZ)) + Math.abs(center_z);
		double maxLength = Math.max(xLength, Math.max(yLength, zLength));
		return new AABB(maxLength, maxLength, maxLength, -maxLength, -maxLength, -maxLength);
	}
	
	private final Vec3[] modelPos;
	private final Vec3[] worldPos;
	
	public PlaneCollider(double x, double y, double z, double aX, double aY, double aZ, double bX, double bY, double bZ) {
		this(getInitialAABB(x, y, z, aX, aY, aZ, bX, bY, bZ), x, y, z, aX, aY, aZ, bX, bY, bZ);
	}
	
	public PlaneCollider(AABB entityCallAABB, double centerX, double centerY, double centerZ, double pos1X, double pos1Y, double pos1Z, double pos2X, double pos2Y, double pos2Z) {
		super(new Vec3(centerX, centerY, centerZ), entityCallAABB);
		
		this.modelPos = new Vec3[2];
		this.worldPos = new Vec3[2];
		this.modelPos[0] = new Vec3(pos1X, pos1Y, pos1Z);
		this.modelPos[1] = new Vec3(pos2X, pos2Y, pos2Z);
		this.worldPos[0] = new Vec3(0.0D, 0.0D, 0.0D);
		this.worldPos[1] = new Vec3(0.0D, 0.0D, 0.0D);
	}

	@Override
	public boolean isCollide(Entity entity) {
		AABB opponent = entity.getBoundingBox();
		Vec3 planeNorm = this.worldPos[0].cross(this.worldPos[1]);
		Vec3 pos = new Vec3(planeNorm.x >= 0 ? opponent.maxX : opponent.minX, planeNorm.y >= 0 ? opponent.maxY : opponent.minY, planeNorm.z >= 0 ? opponent.maxZ : opponent.minZ);
		Vec3 neg = new Vec3(planeNorm.x >= 0 ? opponent.minX : opponent.maxX, planeNorm.y >= 0 ? opponent.minY : opponent.maxY, planeNorm.z >= 0 ? opponent.minZ : opponent.maxZ);
		double planeD = planeNorm.dot(this.worldCenter);
		double dot1 = planeNorm.dot(pos) - planeD;
		
		if (dot1 < 0.0D) {
			return false;
		}
		
		double dot2 = planeNorm.dot(neg) - planeD;

		return !(dot2 > 0.0D);
	}

	@Override
	public void transform(OpenMatrix4f mat) {
		for (int i = 0; i < 2; i ++) {
			this.worldPos[i] = OpenMatrix4f.transform(mat.removeTranslation(), this.modelPos[i]);
		}
		
		super.transform(mat);
	}
	
	@Override
	public PlaneCollider deepCopy() {
		Vec3 aVec = this.modelPos[0];
		Vec3 bVec = this.modelPos[1];
		
		return new PlaneCollider(this.modelCenter.x, this.modelCenter.y, this.modelCenter.z, aVec.x, aVec.y, aVec.z, bVec.x, bVec.y, bVec.z);
	}
	
	@Override
	public void drawInternal(PoseStack poseStack, VertexConsumer vertexConsumer, Armature armature, Joint joint, Pose pose1, Pose pose2, float partialTicks, int color) {
	}
	
	@Override
	public void draw(PoseStack matrixStackIn, MultiBufferSource buffer, LivingEntityPatch<?> entitypatch, AttackAnimation animation, Joint joint, float prevElapsedTime, float elapsedTime, float partialTicks, float attackSpeed) {
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public RenderType getRenderType() {
		return null;
	}
}