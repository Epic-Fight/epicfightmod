package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import yesman.epicfight.skill.Skill.ActivateType;
import yesman.epicfight.skill.Skill.Resource;

public class SkillBuilder<T extends Skill> {
	protected ResourceLocation registryName;
	protected CreativeModeTab tab;
	protected SkillCategory category;
	protected ActivateType activateType = ActivateType.ONE_SHOT;
	protected Resource resource = Resource.NONE;
	
	@SuppressWarnings("unchecked")
	public <B extends SkillBuilder<T>> B setRegistryName(ResourceLocation registryName) {
		this.registryName = registryName;
		return (B)this;
	}
	
	/**
	 *  Leave the value as null if you want your skill's creative tab is decided by {@link EpicFightExtensions} setup
	 */
	@SuppressWarnings("unchecked")
	public <B extends SkillBuilder<T>> B setCreativeTab(CreativeModeTab tab) {
		this.tab = tab;
		return (B)this;
	}
	
	@SuppressWarnings("unchecked")
	public <B extends SkillBuilder<T>> B setCategory(SkillCategory category) {
		this.category = category;
		return (B)this;
	}
	
	@SuppressWarnings("unchecked")
	public <B extends SkillBuilder<T>> B setActivateType(ActivateType activateType) {
		this.activateType = activateType;
		return (B)this;
	}
	
	@SuppressWarnings("unchecked")
	public <B extends SkillBuilder<T>> B setResource(Resource resource) {
		this.resource = resource;
		return (B)this;
	}
}