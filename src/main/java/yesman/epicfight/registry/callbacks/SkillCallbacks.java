package yesman.epicfight.registry.callbacks;

import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import yesman.epicfight.skill.Skill;

public class SkillCallbacks implements BakeCallback<Skill> {
	private static final SkillCallbacks INSTANCE = new SkillCallbacks();
	
	public static SkillCallbacks getSkillCallback() {
		return SkillCallbacks.INSTANCE;
	}
	
	@Override
	public void onBake(Registry<Skill> registry) {
		// dereferencing holder in skill instance
		registry.holders().forEach(holder -> {
			holder.value().setHolder(holder);
		});
	}
}
