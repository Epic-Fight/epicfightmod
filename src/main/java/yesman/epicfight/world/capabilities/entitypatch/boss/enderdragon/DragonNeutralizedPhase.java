package yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;

public class DragonNeutralizedPhase extends PatchedDragonPhase {
	public DragonNeutralizedPhase(EnderDragon dragon) {
		super(dragon);
	}
	
	@Override
	public void begin() {
		this.dragonpatch.getAnimator().playAnimation(Animations.DRAGON_NEUTRALIZED, 0.0F);
		
		if (this.dragonpatch.isLogicalClient()) {
			Minecraft.getInstance().getSoundManager().stop(EpicFightSounds.ENDER_DRAGON_CRYSTAL_LINK.get().getLocation(), SoundSource.HOSTILE);
			this.dragon.level().addParticle(EpicFightParticles.FORCE_FIELD_END.get(), this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 0, 0, 0);
		}
	}
	
	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return PatchedPhases.NEUTRALIZED;
	}
	
	@Override
	public boolean isSitting() {
		return true;
	}
}