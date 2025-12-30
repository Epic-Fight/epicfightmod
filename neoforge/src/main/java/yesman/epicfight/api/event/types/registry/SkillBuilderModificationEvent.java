package yesman.epicfight.api.event.types.registry;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.skill.SkillBuilder;

public class SkillBuilderModificationEvent extends Event {
	private final ResourceLocation registryName;
	private final SkillBuilder<?> skillBuilder;
	
	public SkillBuilderModificationEvent(ResourceLocation registryName, SkillBuilder<?> skillBuilder) {
		this.registryName = registryName;
		this.skillBuilder = skillBuilder;
	}
	
	public ResourceLocation getRegistryName() {
		return this.registryName;
	}
	
	public SkillBuilder<?> getSkillBuilder() {
		return this.skillBuilder;
	}
}
