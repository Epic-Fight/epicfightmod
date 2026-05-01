package yesman.epicfight.skill;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.main.EpicFightExtensions;
import yesman.epicfight.skill.Skill.ActivateType;
import yesman.epicfight.skill.Skill.Resource;

import java.util.function.Function;

@SuppressWarnings("unchecked")
public class SkillBuilder<B extends SkillBuilder<?>> {
	protected final Function<B, ? extends Skill> constructor;
	protected ResourceLocation registryName;
	protected CreativeModeTab tab;
	protected SkillCategory category;
	protected ActivateType activateType = ActivateType.ONE_SHOT;
	protected Resource resource = Resource.NONE;

	public SkillBuilder(Function<B, ? extends Skill> constructor) {
		this.constructor = constructor;
	}
	
	public B setRegistryName(ResourceLocation registryName) {
		this.registryName = registryName;
		return (B)this;
	}
	
	/**
	 *  Leave the value as null if you want your skill's creative tab is decided by {@link EpicFightExtensions}
	 */
	public B setCreativeTab(CreativeModeTab tab) {
		this.tab = tab;
		return (B)this;
	}
	
	public B setCategory(SkillCategory category) {
		this.category = category;
		return (B)this;
	}
	
	public B setActivateType(ActivateType activateType) {
		this.activateType = activateType;
		return (B)this;
	}
	
	public B setResource(Resource resource) {
		this.resource = resource;
		return (B)this;
	}

	public <T extends Skill> T build(ResourceLocation key) {
		this.setRegistryName(key);

        SkillBuilderModificationEvent builderModificationEvent = new SkillBuilderModificationEvent(key, this);
        EpicFightEventHooks.Registry.MODIFY_SKILL_BUILDER.post(builderModificationEvent);

		return (T)this.constructor.apply((B)this);
	}
}
