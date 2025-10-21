package yesman.epicfight.particle;

import java.util.Random;
import java.util.function.BiFunction;

import org.joml.Vector3d;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

public class HitParticleType extends SimpleParticleType {
	public static final BiFunction<Entity, Entity, Vector3d> CENTER_OF_TARGET = (target, attacker) -> {
		EntityDimensions size = target.getDimensions(target.getPose());
		double x = target.getX();
		double y = target.getY() + size.width() * 0.5D;
		double z = target.getZ();
		
		return new Vector3d(x, y, z);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> RANDOM_WITHIN_BOUNDING_BOX = (target, attacker) -> {
		EntityDimensions size = target.getDimensions(target.getPose());
		Random random = new Random();
		double x = target.getX() + (random.nextDouble() - 0.5D) * size.width();
		double y = target.getY() + (random.nextDouble() + size.height()) * 0.5;
		double z = target.getZ() + (random.nextDouble() - 0.5D) * size.width();
		
		return new Vector3d(x, y, z);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> FRONT_OF_EYES = (target, attacker) -> {
		Vec3 eyePosition = target.getEyePosition();
		Vec3 viewVec = target.getLookAngle().scale(2.0D);
		
		return new Vector3d(eyePosition.x + viewVec.x, eyePosition.y + viewVec.y, eyePosition.z + viewVec.z);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> MIDDLE_OF_ENTITIES = (target, attacker) -> {
		Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5F, 0.0F);
		Vec3 attackerPos = attacker.position().add(0, target.getBbHeight() * 0.5F, 0.0F);
		Vec3 to = attackerPos.subtract(targetPos).scale(0.5D);
		
		return new Vector3d(targetPos.x + to.x, targetPos.y + to.y, targetPos.z + to.z);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> ZERO = (target, attacker) -> {
		return new Vector3d(0.0D, 0.0D, 0.0D);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> ATTACKER_XY_ROTATION = (target, attacker) -> {
		return new Vector3d(attacker.getXRot(), 180.0F - attacker.getYRot(), -1.0D);
	};
	
	public static final BiFunction<Entity, Entity, Vector3d> ATTACKER_Y_ROTATION = (target, attacker) -> {
		return new Vector3d(90.0F, attacker.getYRot(), -1.0D);
	};
	
	public BiFunction<Entity, Entity, Vector3d> positionProvider;
	public BiFunction<Entity, Entity, Vector3d> argumentProvider;
	
	public HitParticleType(boolean pOverrideLimiter) {
		this(pOverrideLimiter, CENTER_OF_TARGET, ZERO);
	}
	
	public HitParticleType(boolean pOverrideLimiter, BiFunction<Entity, Entity, Vector3d> positionProvider, BiFunction<Entity, Entity, Vector3d> argumentProvider) {
		super(pOverrideLimiter);
		this.positionProvider = positionProvider;
		this.argumentProvider = argumentProvider;
	}
	
	public void spawnParticleWithArgument(ServerLevel world, Entity e1, Entity e2) {
		this.spawnParticleWithArgument(world, null, null, e1, e2);
	}	
	
	public void spawnParticleWithArgument(ServerLevel world, BiFunction<Entity, Entity, Vector3d> positionProvider, BiFunction<Entity, Entity, Vector3d> argumentProvider, Entity e1, Entity e2) {
		Vector3d position = positionProvider == null ? this.positionProvider.apply(e1, e2) : positionProvider.apply(e1, e2);
		Vector3d arguments = argumentProvider == null ? this.argumentProvider.apply(e1, e2) : argumentProvider.apply(e1, e2);
		
		world.sendParticles(this, position.x, position.y, position.z, 0, arguments.x, arguments.y, arguments.z, 1.0D);
	}
	
	public void spawnParticleWithArgument(ServerLevel world, double posX, double posY, double posZ, double argX, double argY, double argZ) {
		world.sendParticles(this, posX, posY, posZ, 0, argX, argY, argZ, 1.0D);
	}
}