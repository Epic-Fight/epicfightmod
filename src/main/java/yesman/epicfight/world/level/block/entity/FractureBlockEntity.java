package yesman.epicfight.world.level.block.entity;

import java.util.Random;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.world.level.block.FractureBlockState;

public class FractureBlockEntity extends BlockEntity {
	private Vector3f translate;
	private Quaternionf rotation;
	private BlockState originalBlockState;
	private double bouncing;
	private int maxLifeTime;
	private int lifeTime = 0;
	
	public FractureBlockEntity(BlockPos blockPos, BlockState originalBlockState) {
		super(EpicFightBlockEntities.FRACTURE.get(), blockPos, originalBlockState);
	}
	
	public FractureBlockEntity(BlockPos blockPos, BlockState blockState, FractureBlockState fractureBlockState) {
		super(EpicFightBlockEntities.FRACTURE.get(), blockPos, blockState);
		
		this.originalBlockState = fractureBlockState.getOriginalBlockState(blockPos);
		this.bouncing = fractureBlockState.getBouncing();
		this.translate = fractureBlockState.getTranslate();
		this.rotation = fractureBlockState.getRotation();
		this.maxLifeTime = fractureBlockState.getLifeTime();
	}
	
	public BlockState getOriginalBlockState() {
		return this.originalBlockState;
	}
	
	public Vector3f getTranslate() {
		return this.translate;
	}
	
	public Quaternionf getRotation() {
		return this.rotation;
	}
	
	public double getBouncing() {
		return this.bouncing;
	}
	
	public int getMaxLifeTime() {
		return this.maxLifeTime;
	}
	
	public int getLifeTime() {
		return this.lifeTime;
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void lifeTimeTick(Level level, BlockPos blockPos, BlockState blockState, FractureBlockEntity fractureBlockEntity) {
		if (fractureBlockEntity.originalBlockState.shouldSpawnParticlesOnBreak() && fractureBlockEntity.maxLifeTime - fractureBlockEntity.lifeTime < 10) {
			Particle blockParticle = new TerrainParticle((ClientLevel)level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0, 0, 0, fractureBlockEntity.originalBlockState, blockPos);
			blockParticle.setParticleSpeed((Math.random() - 0.5D) * 0.3D, Math.random() * 0.5D, (Math.random() - 0.5D) * 0.3D);
			blockParticle.setLifetime(10 + new Random().nextInt(60));
			
			Minecraft mc = Minecraft.getInstance();
			mc.particleEngine.add(blockParticle);
		}
		
		if (fractureBlockEntity.lifeTime++ > fractureBlockEntity.maxLifeTime) {
			level.removeBlockEntity(blockPos);
			FractureBlockState.remove(blockPos);
			level.setBlock(blockPos, fractureBlockEntity.getOriginalBlockState(), 0);
		}
	}
}