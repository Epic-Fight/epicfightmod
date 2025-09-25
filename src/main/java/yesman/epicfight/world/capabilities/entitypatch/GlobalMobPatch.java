package yesman.epicfight.world.capabilities.entitypatch;

import net.minecraft.world.entity.Mob;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

public class GlobalMobPatch extends HurtableEntityPatch<Mob> {
	private int remainStunTime;
	
	public GlobalMobPatch(Mob original) {
		super(original);
	}
	
	@Override
	protected void updateStunTime() {
		super.updateStunTime();
		
		--this.remainStunTime;
	}
	
	@Override
	public boolean applyStun(StunType stunType, float stunTime) {
		this.original.xxa = 0.0F;
		this.original.yya = 0.0F;
		this.original.zza = 0.0F;
		this.original.setDeltaMovement(0.0D, 0.0D, 0.0D);
		this.cancelKnockback = true;
		this.remainStunTime = (int)(stunTime * 20.0F);
		
		return true;
	}
	
	@Override
	public OpenMatrix4f getModelMatrix(float partialTicks) {
		return null;
	}
	
	public boolean isStunned() {
		return this.remainStunTime > 0 && EpicFightGameRules.GLOBAL_STUN.getRuleValue(this.original.level());
	}
}