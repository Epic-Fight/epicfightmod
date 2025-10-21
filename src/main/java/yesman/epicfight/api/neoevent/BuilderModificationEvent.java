package yesman.epicfight.api.neoevent;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import yesman.epicfight.skill.SkillBuilder;

public class BuilderModificationEvent extends Event implements IModBusEvent {
	private final ResourceLocation registryName;
	private final SkillBuilder<?> skillBuilder;
	
	public BuilderModificationEvent(ResourceLocation registryName, SkillBuilder<?> skillBuilder) {
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
