package yesman.epicfight.data.conditions.entity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.ibm.icu.text.MessageFormat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class PlayerSkillActivated extends EntityPatchCondition {
	private Skill skill;
	
	@Override
	public PlayerSkillActivated read(CompoundTag tag) {
		String skillName = this.assertTag("skill", "string", tag, StringTag.class, CompoundTag::getString);
		
		if ((this.skill = SkillManager.getSkill(skillName)) == null) {
			throw new NoSuchElementException(MessageFormat.format("{} condition error: Skill named {} does not exist", this.getClass().getSimpleName(), skillName));
		}
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("skill", this.skill.getRegistryName().toString());
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		if (target instanceof PlayerPatch<?> playerpatch) {
			Optional<SkillContainer> skill = playerpatch.getSkillContainerFor(this.skill);
			
			if (skill.isEmpty()) {
				return false;
			} else {
				return skill.get().isActivated();
			}
		}
		
		return false;
	}
	
	@OnlyIn(Dist.CLIENT)
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		AbstractWidget popupBox = new PopupBox.RegistryPopupBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, Component.literal("skill"), SkillManager.getSkillRegistry(), null);
		
		return List.of(ParameterEditor.of((skill) -> StringTag.valueOf(skill.toString()), (tag) -> SkillManager.getSkill(ParseUtil.nullOrToString(tag, Tag::getAsString)), popupBox));
	}
}