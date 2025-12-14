package yesman.epicfight.world.entity.eventlistener;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillCastEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private final SkillContainer skillContainer;
	@Nullable
	private final FriendlyByteBuf args;
	private boolean skillExecutable;
	private boolean stateExecutable;
	
	public SkillCastEvent(PlayerPatch<?> playerpatch, SkillContainer skillContainer, @Nullable FriendlyByteBuf args) {
		super(playerpatch, true);
		
		this.skillContainer = skillContainer;
		this.args = args;
	}
	
	public SkillContainer getSkillContainer() {
		return this.skillContainer;
	}
	
	@Nullable
	public FriendlyByteBuf getArguments() {
		return this.args;
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
}