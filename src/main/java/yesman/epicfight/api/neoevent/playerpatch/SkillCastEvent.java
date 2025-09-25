package yesman.epicfight.api.neoevent.playerpatch;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillCastEvent extends PlayerPatchEvent<PlayerPatch<?>> implements ICancellableEvent {
	private final SkillContainer skillContainer;
	private final CompoundTag arguments;
	private boolean skillExecutable;
	private boolean stateExecutable;
	
	public SkillCastEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer, CompoundTag arguments) {
		super(playerpatch);
		
		this.skillContainer = skillContainer;
		this.arguments = arguments;
	}
	
	public SkillContainer getSkillContainer() {
		return this.skillContainer;
	}
	
	public boolean isSkillExecutable() {
		return this.skillExecutable;
	}
	
	public boolean isStateExecutable() {
		return this.stateExecutable;
	}
	
	public void setSkillExecutable(boolean skillExecutable) {
		this.skillExecutable = skillExecutable;
	}
	
	public void setStateExecutable(boolean stateExecutable) {
		this.stateExecutable = stateExecutable;
	}
	
	public boolean isExecutable() {
		return this.skillExecutable && this.stateExecutable;
	}
	
	public boolean shouldReserveKey() {
		return !this.isExecutable() && !this.isCanceled();
	}
	
	public CompoundTag getArguments() {
		return this.arguments;
	}
}