package yesman.epicfight.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import yesman.epicfight.skill.SkillCategories;

public class JEIEpicFightSkillsCategory implements IRecipeCategory<SkillCategories> {
	@Override
	public RecipeType<SkillCategories> getRecipeType() {
		return null;
	}
	
	@Override
	public Component getTitle() {
		return null;
	}
	
	@Override
	public IDrawable getIcon() {
		return null;
	}
	
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, SkillCategories recipe, IFocusGroup focuses) {
		
	}
}
