package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerKilledEvent extends AbstractPlayerEvent<ServerPlayerPatch> {
	private final LivingEntity killedEntity;
	private final DamageSource damagesource;
	
	public PlayerKilledEvent(ServerPlayerPatch playerpatch, LivingEntity killedEntity, DamageSource damagesource) {
		super(playerpatch, false);
		
		this.killedEntity = killedEntity;
		this.damagesource = damagesource;
	}
	
	public LivingEntity getKilledEntity() {
		return this.killedEntity;
	}
	
	public DamageSource getDamageSource() {
		return this.damagesource;
	}
}
