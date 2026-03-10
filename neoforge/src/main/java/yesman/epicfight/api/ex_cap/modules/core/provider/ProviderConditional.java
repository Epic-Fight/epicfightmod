package yesman.epicfight.api.ex_cap.modules.core.provider;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import yesman.epicfight.api.ex_cap.modules.core.managers.ConditionalManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.EpicFight;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class ProviderConditional
{
	public static final ResourceKey<Registry<ProviderConditional>> REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(EpicFight.MODID, "ex_cap_conditional"));

	//Identifiers
	protected final ProviderConditionalType type;
	//Output
	protected final Style style;
	protected final Boolean combination;
	//Input
	protected final Skill skillToCheck;
	protected final WeaponCategory category;
	protected final Item weapon;
	protected final List<ProviderConditional> providerConditionals;
	protected final SkillSlot slot;
	protected final Holder<SkillDataKey<?>> key;
	protected final InteractionHand hand;
	protected final Predicate<LivingEntityPatch<?>> customFunction;

	private ProviderConditional(ProviderConditionalType type, Style style, Skill skillToCheck, WeaponCategory category, Item weapon, InteractionHand hand, SkillSlot slot, Holder<SkillDataKey<?>> key, Boolean combination, Predicate<LivingEntityPatch<?>> customFunction, List<ProviderConditional> providerConditionals) {
		this.type = type;
		this.style = style;
		this.skillToCheck = skillToCheck;
		this.category = category;
		this.weapon = weapon;
		this.hand = hand;
		this.slot = slot;
		this.key = key;
		this.combination = combination;
		this.customFunction = customFunction;
		this.providerConditionals = Lists.newArrayList(providerConditionals);
	}

	public static ProviderConditionalBuilder builder()
	{
		return new ProviderConditionalBuilder();
	}

	public ProviderConditional(ProviderConditionalBuilder builder)
	{
		this.type = builder.type;
		this.style = builder.wieldStyle;
		this.combination = builder.visibleOffHand;
		this.skillToCheck = builder.skillToCheck;
		this.category = builder.category;
		this.weapon = builder.weapon;
		//Build the sub-conditionals,
		this.providerConditionals = Lists.newArrayList();
		builder.providerConditionals.forEach(subCond -> providerConditionals.add(subCond.build()));
		this.slot = builder.slot;
		this.key = builder.key;
		this.hand = builder.hand;
		this.customFunction = builder.customFunction;
	}


	/**
	 * @param entityPatch the patch used to return whatever it is.
	 * @return if the conditionals ever evaluate to true.
	 */
	public Boolean test(LivingEntityPatch<?> entityPatch)
	{
		if (type.equals(ProviderConditionalType.SKILL_ACTIVATION))
		{
			if (entityPatch instanceof PlayerPatch<?> pPatch)
			{
				if (pPatch.getSkill(slot).isActivated())
				{
					return true;
				}
			}
		}
		if (type.equals(ProviderConditionalType.SKILL_EXISTENCE))
		{
			if (HelperFunctions.skillCheck(entityPatch, skillToCheck, slot))
			{
				return true;
			}
		}
		if (type.equals(ProviderConditionalType.WEAPON_CATEGORY))
		{
			if (HelperFunctions.itemCheck(entityPatch, category, hand))
			{
				return true;
			}
		}
		if (type.equals(ProviderConditionalType.DATA_KEY))
		{
			if (entityPatch instanceof PlayerPatch<?> playerPatch)
			{
				return playerPatch.getSkill(slot).getDataManager().hasData(key) && (Boolean) playerPatch.getSkill(slot).getDataManager().getRawDataValue(key);
			}
		}
		if (type.equals(ProviderConditionalType.SPECIFIC_WEAPON))
		{
			assert hand != null;
			return entityPatch.getOriginal().getItemInHand(hand).is(weapon);
		}
		if (type.equals(ProviderConditionalType.COMPOSITE))
		{
			assert providerConditionals != null;
			for (ProviderConditional conditional : providerConditionals)
			{
				if (!conditional.test(entityPatch))
				{
					return false;
				}
			}
			return true;
		}
		if (type.equals(ProviderConditionalType.CUSTOM))
		{
			assert this.customFunction != null;
			if (this.customFunction.test(entityPatch))
			{
				return true;
			}
		}
		return type.equals(ProviderConditionalType.DEFAULT);
	}

	public Style testConditionalStyle(LivingEntityPatch<?> entityPatch)
	{
		if (type.equals(ProviderConditionalType.SKILL_ACTIVATION))
		{
			if (entityPatch instanceof PlayerPatch<?> pPatch)
			{
				if (pPatch.getSkill(slot).isActivated())
				{
					return style;
				}
			}
		}
		if (type.equals(ProviderConditionalType.SKILL_EXISTENCE))
		{
			if (HelperFunctions.skillCheck(entityPatch, skillToCheck, slot))
				return style;
		}
		if (type.equals(ProviderConditionalType.WEAPON_CATEGORY))
		{
			if (HelperFunctions.itemCheck(entityPatch, category, hand))
				return style;
		}
		if (type.equals(ProviderConditionalType.DATA_KEY))
		{
			if (entityPatch instanceof PlayerPatch<?> playerPatch)
			{
				if (playerPatch.getSkill(slot).getDataManager().hasData(key) && (Boolean) playerPatch.getSkill(slot).getDataManager().getRawDataValue(key))
					return style;
			}
		}
		if (type.equals(ProviderConditionalType.SPECIFIC_WEAPON))
		{
			assert hand != null;
			if (entityPatch.getOriginal().getItemInHand(hand).is(this.weapon))
				return style;
		}
		if (type.equals(ProviderConditionalType.COMPOSITE))
		{
			assert providerConditionals != null;
			for (ProviderConditional conditional : providerConditionals)
			{
				if (!conditional.test(entityPatch))
					return null;
			}
			return style;
		}
		if (type.equals(ProviderConditionalType.CUSTOM))
		{
			assert this.customFunction != null;
			if (this.customFunction.test(entityPatch))
			{
				return style;
			}
		}
		if (type.equals(ProviderConditionalType.DEFAULT))
			return style;
		return null;
	}

	public Boolean testConditionalCombo(LivingEntityPatch<?> entityPatch)
	{
		if (type.equals(ProviderConditionalType.SKILL_ACTIVATION))
		{
			if (entityPatch instanceof PlayerPatch<?> pPatch)
			{
				if (pPatch.getSkill(slot).isActivated())
				{
					return combination;
				}
			}
		}
		if (type.equals(ProviderConditionalType.SKILL_EXISTENCE))
		{
			if (HelperFunctions.skillCheck(entityPatch, skillToCheck, slot))
				return combination;
		}
		if (type.equals(ProviderConditionalType.WEAPON_CATEGORY))
		{
			if (HelperFunctions.itemCheck(entityPatch, category, hand))
				return combination;
		}
		if (type.equals(ProviderConditionalType.DATA_KEY))
		{
			if (entityPatch instanceof PlayerPatch<?> playerPatch)
			{
				if (playerPatch.getSkill(slot).getDataManager().hasData(key) && (Boolean) playerPatch.getSkill(slot).getDataManager().getRawDataValue(key))
					return combination;
			}
		}
		if (type.equals(ProviderConditionalType.SPECIFIC_WEAPON))
		{
			assert hand != null;
			if (entityPatch.getOriginal().getItemInHand(hand).is(this.weapon))
				return combination;
		}
		if (type.equals(ProviderConditionalType.COMPOSITE))
		{
			assert providerConditionals != null;
			for (ProviderConditional conditional : providerConditionals)
			{
				if (!conditional.test(entityPatch))
					return null;
			}
			return combination;
		}
		if (type.equals(ProviderConditionalType.CUSTOM))
		{
			assert this.customFunction != null;
			if (this.customFunction.test(entityPatch))
			{
				return combination;
			}
		}
		if (type.equals(ProviderConditionalType.DEFAULT))
			return combination;
		return null;
	}

//	private boolean checkSubConditionalsForExistence()
//	{
//		if (type == ProviderConditionalType.COMPOSITE)
//		{
//			assert providerConditionals != null;
//			for (ProviderConditional conditional : providerConditionals)
//			{
//				if (conditional.type == ProviderConditionalType.SKILL_EXISTENCE && conditional.slot == BattleArtsSkillSlots.BATTLE_STYLE)
//					return true;
//			}
//		}
//		return false;
//	}

	public ProviderConditionalType getType()
	{
		return type;
	}

//	private boolean checkSubConditionalsForDataKey()
//	{
//		if (type == ProviderConditionalType.COMPOSITE)
//		{
//			assert providerConditionals != null;
//			for (ProviderConditional conditional : providerConditionals)
//			{
//				if (conditional.type == ProviderConditionalType.DATA_KEY && conditional.slot == BattleArtsSkillSlots.BATTLE_STYLE)
//					return true;
//			}
//		}
//		return false;
//	}

	public int getPriority()
	{
        //TODO: Move this into Battle Arts API;
        //		if (ModList.get().isLoaded("battlearts_api") && type == ProviderConditionalType.SKILL_EXISTENCE && slot == BattleArtsSkillSlots.BATTLE_STYLE)
        //			return Integer.MAX_VALUE - 3;
        //		if (type == ProviderConditionalType.DATA_KEY && ModList.get().isLoaded("battlearts_api") && slot == BattleArtsSkillSlots.BATTLE_STYLE)
        //			return Integer.MAX_VALUE - 2;
        //		if (type == ProviderConditionalType.COMPOSITE && ModList.get().isLoaded("battlearts_api") && checkSubConditionalsForExistence())
        //			return Integer.MAX_VALUE - 1;
        //		if (type == ProviderConditionalType.COMPOSITE && ModList.get().isLoaded("battlearts_api") && checkSubConditionalsForDataKey())
        //			return Integer.MAX_VALUE;
		return this.type.getPriority();
	}

	public ProviderConditional copy()
	{
		return new ProviderConditional(type, style, skillToCheck, category, weapon, hand, slot, key, combination, customFunction, providerConditionals);
	}
	public static class ProviderConditionalBuilder
	{
		private ProviderConditionalType type;
		private Style wieldStyle;
		private Boolean visibleOffHand;
		private Skill skillToCheck;
		private WeaponCategory category;
		private Item weapon;
		private final List<ProviderConditionalBuilder> providerConditionals;
		private SkillSlot slot;
		private Holder<SkillDataKey<?>> key;
		private InteractionHand hand;
		private Predicate<LivingEntityPatch<?>> customFunction;
		protected ResourceLocation parent;


		public ProviderConditionalBuilder()
		{
			type = ProviderConditionalType.DEFAULT;
			skillToCheck = null;
			category = null;
			weapon = null;
			providerConditionals = Lists.newArrayList();
			slot = null;
			key = null;
			hand = null;
			customFunction = null;
			wieldStyle = null;
			visibleOffHand = false;
		}

		public ProviderConditionalBuilder setParent(ResourceLocation parent)
		{
			this.parent = parent;
			return this;
		}

		public static ProviderConditionalBuilder deserialize(JsonElement jsonElement) throws JsonParseException
		{
			ProviderConditionalBuilder builder = new ProviderConditionalBuilder();
			try {
				JsonObject gsonObject = jsonElement.getAsJsonObject();
				ProviderConditionalType type = ProviderConditionalType.valueOf(gsonObject.get("provider_type").getAsString().toUpperCase());
				Style wieldStyle = Style.ENUM_MANAGER.get(gsonObject.get("style").getAsString().toUpperCase());
				boolean visibleOffHand = gsonObject.get("visible_offhand").getAsBoolean();
				builder.setType(type);
				switch (type) {
					case WEAPON_CATEGORY -> builder
							.setType(type).setWieldStyle(wieldStyle).isVisibleOffHand(visibleOffHand)
							.setCategory(WeaponCategory.ENUM_MANAGER.get(gsonObject.get("weapon_category").getAsString().toUpperCase()))
							.setHand(InteractionHand.valueOf(gsonObject.get("hand").getAsString().toUpperCase()));
					case SPECIFIC_WEAPON -> builder
							.setType(type).setWieldStyle(wieldStyle).isVisibleOffHand(visibleOffHand)
							.setWeapon(BuiltInRegistries.ITEM.get(ResourceLocation.parse(gsonObject.get("specific_weapon").getAsString())))
							.setHand(InteractionHand.valueOf(gsonObject.get("hand").getAsString().toUpperCase()));
					case SKILL_EXISTENCE, SKILL_ACTIVATION -> builder
							.setType(type).setWieldStyle(wieldStyle).isVisibleOffHand(visibleOffHand)
							.setSkillToCheck(EpicFightRegistries.SKILL.get(ResourceLocation.parse(gsonObject.get("skill").getAsString())))
							.setSlot(SkillSlot.ENUM_MANAGER.get(gsonObject.get("slot").getAsString().toUpperCase()));
					case DATA_KEY -> builder
							.setType(type).setWieldStyle(wieldStyle).isVisibleOffHand(visibleOffHand)
							.setSkillToCheck(EpicFightRegistries.SKILL.get(ResourceLocation.parse(gsonObject.get("skill").getAsString())))
							.setKey(EpicFightRegistries.SKILL_DATA_KEY.getHolder(ResourceLocation.parse(gsonObject.get("boolean_key").getAsString())).get())
							.setSlot(SkillSlot.ENUM_MANAGER.get(gsonObject.get("slot").getAsString().toUpperCase()));
				}
			}
			catch (RuntimeException e)
			{
				throw new JsonParseException("Failed to parse ProviderConditional: " + e.getMessage());
			}
			return builder;
		}

		private ProviderConditionalBuilder merge()
		{
			ProviderConditionalBuilder result = new ProviderConditionalBuilder();
			Deque<ProviderConditionalBuilder> hierarchy = new ArrayDeque<>();
			ProviderConditionalBuilder current = this;

			// Collect parent chain
			while (current != null) {
				hierarchy.push(current);
				current = ConditionalManager.get(parent);
			}

			while (!hierarchy.isEmpty()) {
				ProviderConditionalBuilder builder = hierarchy.pop();
				if (builder.type != ProviderConditionalType.DEFAULT)
					result.setType(builder.type);
				if (builder.wieldStyle != null)
					result.setWieldStyle(builder.wieldStyle);
				if (builder.visibleOffHand)
					result.isVisibleOffHand(true);
				if (builder.skillToCheck != null)
					result.setSkillToCheck(builder.skillToCheck);
				if (builder.category != null)
					result.setCategory(builder.category);
				if (builder.weapon != null)
					result.setWeapon(builder.weapon);
				if (builder.slot != null)
					result.setSlot(builder.slot);
				if (builder.key != null)
					result.setKey(builder.key);
				if (builder.hand != null)
					result.setHand(builder.hand);
				if (builder.customFunction != null)
					result.setCustomFunction(builder.customFunction);
				result.providerConditionals.addAll(builder.providerConditionals);
			}
			return result;
		}

		public ProviderConditional build()
		{
			return new ProviderConditional(merge());
		}

		public ProviderConditionalBuilder isVisibleOffHand(boolean visibleOffHand) {
			this.visibleOffHand = visibleOffHand;
			return this;
		}

		public ProviderConditionalBuilder setWieldStyle(Style wieldStyle) {
			this.wieldStyle = wieldStyle;
			return this;
		}

		public ProviderConditionalBuilder setType(@NotNull ProviderConditionalType type)
		{
			this.type = type;
			return this;
		}

		public ProviderConditionalBuilder setSkillToCheck(Skill skillToCheck) {
			this.skillToCheck = skillToCheck;
			return this;
		}

		public ProviderConditionalBuilder setCategory(WeaponCategory category) {
			this.category = category;
			return this;
		}

		public ProviderConditionalBuilder setWeapon(Item weapon) {
			this.weapon = weapon;
			return this;
		}

		public ProviderConditionalBuilder setProviderConditionals(ProviderConditionalBuilder... providerConditionals) {
			for (ProviderConditionalBuilder conditional : providerConditionals)
			{
				if (conditional.type == ProviderConditionalType.COMPOSITE)
				{
					throw new IllegalArgumentException("Cannot have composite conditionals as sub-conditionals.");
				}
				this.providerConditionals.add(conditional);
			}
			return this;
		}

		public ProviderConditionalBuilder setSlot(SkillSlot slot) {
			this.slot = slot;
			return this;
		}

		public ProviderConditionalBuilder setKey(Holder<SkillDataKey<?>> key) {
			this.key = key;
			return this;
		}

		public ProviderConditionalBuilder setHand(InteractionHand hand) {
			this.hand = hand;
			return this;
		}

		public ProviderConditionalBuilder setCustomFunction(Predicate<LivingEntityPatch<?>> customFunction) {
			this.customFunction = customFunction;
			return this;
		}
	}
}
