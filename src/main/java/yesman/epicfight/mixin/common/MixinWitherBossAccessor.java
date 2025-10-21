package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Mixin(value = WitherBoss.class)
public interface MixinWitherBossAccessor {
	
	@Accessor(value = "xRotHeads")
	public float[] getXRotHeads();
	
	@Accessor(value = "xRotOHeads")
	public float[] getXRotOHeads();
	
	@Accessor(value = "yRotHeads")
	public float[] getYRotHeads();
	
	@Accessor(value = "yRotOHeads")
	public float[] getYRotOHeads();
	
	@Accessor(value = "bossEvent")
	public ServerBossEvent getBossEvent();
	
	@Invoker(value = "getHeadX")
	public double invoke_getHeadX(int head);
	
	@Invoker(value = "getHeadY")
	public double invoke_getHeadY(int head);
	
	@Invoker(value = "getHeadZ")
	public double invoke_getHeadZ(int head);
	
	@Invoker(value = "performRangedAttack")
	public void invoke_performRangedAttack(int head, LivingEntity target);
	
	@Invoker(value = "performRangedAttack")
	public void invoke_performRangedAttack(int head, double x, double y, double z, boolean isDangerous);
}