package yesman.epicfight.skill.passive;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Collections;
import java.util.function.Function;

public abstract class PassiveSkill extends Skill {
	public static SkillBuilder<?> createPassiveBuilder(Function<SkillBuilder<?>, ? extends PassiveSkill> constructor) {
		return new SkillBuilder<>(constructor).setCategory(SkillCategories.PASSIVE).setResource(Resource.NONE);
	}
	
	@SuppressWarnings("rawtypes")
	public PassiveSkill(SkillBuilder<? extends SkillBuilder> builder) {
		super(builder);
	}
	

	@Override
	public Component getTranslatedTooltip(ItemStack itemStack, CapabilityItem itemCap, PlayerPatch<?> playerPatch) {
		return Component.translatable(this.getTranslationKey() + ".tooltip", getTooltipArgsOfScreen(Lists.newArrayList()).toArray(new Object[0]));
	}
}