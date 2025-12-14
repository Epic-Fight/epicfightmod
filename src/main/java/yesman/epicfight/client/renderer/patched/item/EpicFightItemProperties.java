package yesman.epicfight.client.renderer.patched.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.item.EpicFightItems;
import yesman.epicfight.world.item.SkillBookItem;

public class EpicFightItemProperties {
	public static void registerItemProperties() {
		ItemProperties.register(EpicFightItems.SKILLBOOK.get(), EpicFightMod.identifier("skill"), (itemstack, level, entity, i) -> {
            Skill skill = SkillBookItem.getContainSkill(itemstack);

            if (skill != null) {
                return skill.getCategory().universalOrdinal();
            }

            return Float.NEGATIVE_INFINITY;
        });
	}
}