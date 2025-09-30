package yesman.epicfight.data.loot.function;

import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import it.unimi.dsi.fastutil.floats.FloatObjectPair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.fml.ModList;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.skill.Skill;

public class SetSkillFunction implements LootItemFunction {
	private final List<FloatObjectPair<String>> skillsAndWeight;
	
	public SetSkillFunction(List<FloatObjectPair<String>> skillsAndWeight) {
		this.skillsAndWeight = skillsAndWeight;
	}
	
	private Skill getSkillForSeed(float seed) {
		for (FloatObjectPair<String> pair : this.skillsAndWeight) {
			if (seed < pair.firstFloat()) {
				return SkillManager.getSkill(pair.second());
			}
		}
		
		return this.skillsAndWeight.isEmpty() ? null : SkillManager.getSkill(this.skillsAndWeight.get(0).second());
	}
	
	@Override
	public ItemStack apply(ItemStack itemstack, LootContext lootContext) {
		if (ModList.get().isLoaded("epicskills")) {
			return ItemStack.EMPTY;
		}
		
		float val = lootContext.getRandom().nextFloat();
		Skill skill = this.getSkillForSeed(val);
		
		if (skill != null) {
			itemstack.getOrCreateTag().putString("skill", skill.toString());
		}
		
		return itemstack;
	}
	
	public static LootItemFunction.Builder builder(String... skills) {
		return new LootItemFunction.Builder() {
			public LootItemFunction build() {
				List<FloatObjectPair<String>> list = Lists.newArrayList();
				float weight = 1.0F / skills.length;
				float weightSum = 0.0F;
				
				for (String skill : skills) {
					weightSum += weight;
					list.add(FloatObjectPair.of(weightSum, skill));
				}
				
				return new SetSkillFunction(list);
			}
		};
	}
	
	public static LootItemFunction.Builder builder(Object... skillAndWeight) {
		return new LootItemFunction.Builder() {
			public LootItemFunction build() {
				List<FloatObjectPair<String>> list = Lists.newArrayList();
				float weightTotal = 0.0F;
				float weightSum = 0.0F;
				
				for (int i = 0; i < skillAndWeight.length / 2; i++) {
					weightTotal += (float)skillAndWeight[i * 2];
				}
				
				for (int i = 0; i < skillAndWeight.length / 2; i++) {
					weightSum += (float)skillAndWeight[i * 2];
					list.add(FloatObjectPair.of(weightSum / weightTotal, (String)skillAndWeight[i * 2 + 1]));
				}
				
				return new SetSkillFunction(list);
			}
		};
	}
	
	@Override
	public LootItemFunctionType getType() {
		return EpicFightLootTables.SET_SKILLBOOK_SKILL;
	}
	
	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<SetSkillFunction> {
		@Override
		public void serialize(JsonObject jsonObj, SetSkillFunction skillFunction, JsonSerializationContext jsonDeserializationContext) {
			JsonArray skillArray = new JsonArray();
			JsonArray weightArray = new JsonArray();
			
			for (FloatObjectPair<String> pair : skillFunction.skillsAndWeight) {
				skillArray.add(pair.second());
				weightArray.add(pair.firstFloat());
			}
			
			jsonObj.add("skills", skillArray);
			jsonObj.add("weights", weightArray);
		}
		
		@Override
		public SetSkillFunction deserialize(JsonObject jsonObj, JsonDeserializationContext jsonDeserializationContext) {
			JsonArray skillArray = jsonObj.getAsJsonArray("skills");
			JsonArray weightArray = jsonObj.getAsJsonArray("weights");
			List<FloatObjectPair<String>> list = Lists.newArrayList();
			float totalWeights = 0.0F;
			
			for (int i = 0; i < skillArray.size(); i++) {
				totalWeights += weightArray.get(i).getAsFloat();
			}
			
			for (int i = 0; i < skillArray.size(); i++) {
				if (SkillManager.getSkill(skillArray.get(i).getAsString()) == null) {
					(new NoSuchElementException("SetSkillFunction: There is no skill named " + skillArray.get(i).getAsString())).printStackTrace();
					continue;
				}
				
				list.add(FloatObjectPair.of(weightArray.get(i).getAsFloat() / totalWeights, skillArray.get(i).getAsString()));
			}
			
			return new SetSkillFunction(list);
		}
	}
}