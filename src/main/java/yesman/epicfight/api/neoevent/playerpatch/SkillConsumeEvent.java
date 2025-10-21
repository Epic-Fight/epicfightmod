package yesman.epicfight.api.neoevent.playerpatch;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.ICancellableEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Canceling this event will make skill execution failed to predicate resource check
 * See also {@link Skill#resourcePredicate(PlayerPatch)}
 */
public class SkillConsumeEvent extends PlayerPatchEvent<PlayerPatch<?>> implements ICancellableEvent {
	private final Skill skill;
	private float consumeAmount;
	private Skill.Resource resource;
	@Nullable
	private CompoundTag arguments;
	
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
