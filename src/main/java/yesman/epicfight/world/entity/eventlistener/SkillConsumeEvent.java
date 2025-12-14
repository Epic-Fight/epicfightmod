package yesman.epicfight.world.entity.eventlistener;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Canceling this event will make skill failed to predicate resource check
 * See also {@link Skill#resourcePredicate(PlayerPatch)}
 */
public class SkillConsumeEvent extends AbstractPlayerEvent<PlayerPatch<?>> {
	private final Skill skill;
	private float amount;
	private Skill.Resource resource;
	@Nullable
	private FriendlyByteBuf arguments;
	
	public SkillConsumeEvent(PlayerPatch<?> playerpatch, Skill skill, Skill.Resource resource, @Nullable FriendlyByteBuf args) {
		this(playerpatch, skill, resource, skill.getDefaultConsumptionAmount(playerpatch), args);
	}
	
	public SkillConsumeEvent(PlayerPatch<?> playerpatch, Skill skill, Skill.Resource resource, float amount, @Nullable FriendlyByteBuf args) {
		super(playerpatch, true);
		
		this.skill = skill;
		this.resource = resource;
		this.amount = amount;
		this.arguments = args;
	}
	
	public Skill getSkill() {
		return this.skill;
	}
	
	public Skill.Resource getResourceType() {
		return this.resource;
	}
	
	public float getAmount() {
		return this.amount;
	}
	
	@Nullable
	public FriendlyByteBuf getArguments() {
		return this.arguments;
	}
	
	public void setResourceType(Skill.Resource resource) {
		this.resource = resource;
	}
	
	public void setAmount(float amount) {
		this.amount = amount;
	}
}