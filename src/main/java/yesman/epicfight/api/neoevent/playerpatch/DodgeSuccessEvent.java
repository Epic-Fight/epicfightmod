package yesman.epicfight.api.neoevent.playerpatch;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DodgeSuccessEvent extends PlayerPatchEvent<ServerPlayerPatch> {
	private final DamageSource damageSource;
	private final Vec3 location;
	
	public DodgeSuccessEvent(ServerPlayerPatch playerpatch, DamageSource damageSource, Vec3 location) {
		super(playerpatch);
		
		this.damageSource = damageSource;
		this.location = location;
	}
	
	public DamageSource getDamageSource() {
		return this.damageSource;
	}
	
	public Vec3 getLocation() {
		return this.location;
	}
}