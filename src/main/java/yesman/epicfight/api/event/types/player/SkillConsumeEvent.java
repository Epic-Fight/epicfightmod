package yesman.epicfight.api.event.types.player;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import javax.annotation.Nullable;

/// Canceling this event will make skill execution failed to predicate resource check
/// See also [Skill#resourcePredicate]
public class SkillConsumeEvent extends LivingEntityPatchEvent implements CancelableEvent {
	private float consumeAmount;
	private Skill.Resource resource;
    private final Skill skill;
	@Nullable
	private final CompoundTag arguments;
	
	public SkillConsumeEvent(PlayerPatch<?> playerpatch, Skill skill, Skill.Resource resource, @Nullable CompoundTag args) {
		this(playerpatch, skill, resource, skill.getDefaultConsumptionAmount(playerpatch), args);
	}
	
	public SkillConsumeEvent(PlayerPatch<?> playerpatch, Skill skill, Skill.Resource resource, float consumeAmount, @Nullable CompoundTag args) {
		super(playerpatch);
		
		this.skill = skill;
		this.resource = resource;
		this.consumeAmount = consumeAmount;
		this.arguments = args;
	}
	
	public Skill getSkill() {
		return this.skill;
	}
	
	public Skill.Resource getResourceType() {
		return this.resource;
	}
	
	public float getAmount() {
		return this.consumeAmount;
	}
	
	public void setResourceType(Skill.Resource resource) {
		this.resource = resource;
	}
	
	public void setAmount(float amount) {
		this.consumeAmount = amount;
	}
	
	@Nullable
	public CompoundTag getArguments() {
		return this.arguments;
	}
}
