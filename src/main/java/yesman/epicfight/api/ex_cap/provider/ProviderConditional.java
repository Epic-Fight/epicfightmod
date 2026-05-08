package yesman.epicfight.api.ex_cap.provider;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.managers.ConditionalManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
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

@ApiStatus.Experimental
public class ProviderConditional
{
	///Helper Methods
	/**
	 * Creates a basic provider conditional that always applies the given style.
	 * @param style          The combat style to apply.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#DEFAULT}.
	 */
	public static Builder createDefault(Style style, boolean offHandVisible) {
		return new Builder()
				.setType(ProviderConditionalType.DEFAULT)
				.setWieldStyle(style)
				.isVisibleOffHand(offHandVisible);
	}

	/**
	 * Creates a conditional that applies based on a specific weapon category (e.g., Greatswords).
	 * @param style          The style to apply if the weapon matches the category.
	 * @param category       The {@link WeaponCategory} to check for.
	 * @param hand           The {@link InteractionHand} that must be holding the weapon.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#WEAPON_CATEGORY}.
	 */
	public static Builder createWeaponCategory(Style style, WeaponCategory category, InteractionHand hand, boolean offHandVisible) {
		return new Builder()
				.setType(ProviderConditionalType.WEAPON_CATEGORY)
				.setWieldStyle(style)
				.setCategory(category)
				.setHand(hand)
				.isVisibleOffHand(offHandVisible);
	}

	/**
	 * Creates a conditional for a specific individual item.
	 * Use this to override category-wide movesets for unique "named" weapons.
	 * @param style          The style to apply if the specific item is held.
	 * @param item           The {@link Item} instance to check for.
	 * @param hand           The {@link InteractionHand} required.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#SPECIFIC_WEAPON}.
	 */
	public static Builder createSpecificWeapon(Style style, Item item, InteractionHand hand, boolean offHandVisible) {
		return new Builder()
				.setType(ProviderConditionalType.SPECIFIC_WEAPON)
				.setWieldStyle(style)
				.setWeapon(item)
				.setHand(hand)
				.isVisibleOffHand(offHandVisible);
	}

	/**
	 * Creates a conditional based on whether a specific skill is active or toggled on.
	 * @param style          The style to apply when the skill condition is met.
	 * @param skill          The {@link Skill} to monitor.
	 * @param slot           The {@link SkillSlot} where the skill is located.
	 * @param activation     True to check if the skill is active, false for existence only.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#SKILL_ACTIVATION} or {@code SKILL_EXISTENCE}.
	 */
	public static Builder createSkillCondition(Style style, Holder<Skill> skill, SkillSlot slot, boolean activation, boolean offHandVisible) {
		return new Builder()
				.setType(activation ? ProviderConditionalType.SKILL_ACTIVATION : ProviderConditionalType.SKILL_EXISTENCE)
				.setWieldStyle(style)
				.setSkillToCheck(skill)
				.setSlot(slot)
				.isVisibleOffHand(offHandVisible);
	}

	/**
	 * Creates a conditional that checks a boolean value within a skill's data map.
	 * @param style          The style to apply if the key is true.
	 * @param skill          The skill containing the data.
	 * @param slot           The slot where the skill is assigned.
	 * @param key            The boolean {@link SkillDataKey} to check.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#DATA_KEY}.
	 */
	public static Builder createSkillDataKey(Style style, Holder<Skill> skill, SkillSlot slot, Holder<SkillDataKey<?>> key, boolean offHandVisible) {
		return new Builder()
				.setType(ProviderConditionalType.DATA_KEY)
				.setWieldStyle(style)
				.setSkillToCheck(skill)
				.setSlot(slot)
				.setKey(key)
				.isVisibleOffHand(offHandVisible);
	}

	/**
	 * Creates a custom logic conditional using a code-based predicate.
	 * @param style          The style to apply if the predicate returns true.
	 * @param predicate      The custom logic to evaluate against the entity.
	 * @param offHandVisible Whether the off-hand item is rendered.
	 * @return A builder configured for {@link ProviderConditionalType#CUSTOM}.
	 */
	public static Builder createCustom(Style style, Predicate<LivingEntityPatch<?>> predicate, boolean offHandVisible) {
		return new Builder()
				.setType(ProviderConditionalType.CUSTOM)
				.setWieldStyle(style)
				.setCustomFunction(predicate)
				.isVisibleOffHand(offHandVisible);
	}


	//Identifiers
	protected final ProviderConditionalType type;
	//Output
	protected final Style style;
	protected final Boolean combination;
	//Input
	protected final Holder<Skill> skillToCheck;
	protected final WeaponCategory category;
	protected final Item weapon;
	protected final List<ProviderConditional> providerConditionals;
	protected final SkillSlot slot;
	protected final Holder<SkillDataKey<?>> key;
	protected final InteractionHand hand;
	protected final Predicate<LivingEntityPatch<?>> customFunction;

	@ApiStatus.Internal
	private ProviderConditional(ProviderConditionalType type, Style style, Holder<Skill> skillToCheck, WeaponCategory category, Item weapon, InteractionHand hand, SkillSlot slot, Holder<SkillDataKey<?>> key, Boolean combination, Predicate<LivingEntityPatch<?>> customFunction, List<ProviderConditional> providerConditionals) {
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

	public static Builder builder()
	{
		return new Builder();
	}

	public ProviderConditional(Builder builder)
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
			if (HelperFunctions.skillCheck(entityPatch, skillToCheck.value(), slot))
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

    public ProviderConditionalType getType()
	{
		return type;
	}

    public int getPriority()
	{
        return this.type.getPriority();
	}

	public ProviderConditional copy()
	{
		return new ProviderConditional(type, style, skillToCheck, category, weapon, hand, slot, key, combination, customFunction, providerConditionals);
	}
	/**
	 * A low-level fluent builder for creating {@link ProviderConditional} logic.
	 * <p>
	 * While this builder provides full access to every internal field of the conditional
	 * system, most developers should prefer the factory methods in {@link ProviderConditional}
	 * (e.g., {@code createDefault}, {@code createSkillCondition}) for standard implementations.
	 * </p>
	 * * <p>Use this builder directly only when creating <b>COMPOSITE</b> or <b>CUSTOM</b>
	 * logic that requires specific field tuning not covered by the static factories.</p>
	 */
	public static class Builder
	{
		private ProviderConditionalType type;
		private Style wieldStyle;
		private Boolean visibleOffHand;
		private Holder<Skill> skillToCheck;
		private WeaponCategory category;
		private Item weapon;
		private final List<Builder> providerConditionals;
		private SkillSlot slot;
		private Holder<SkillDataKey<?>> key;
		private InteractionHand hand;
		private Predicate<LivingEntityPatch<?>> customFunction;
		protected ResourceLocation parent;

		public Style getWieldStyle()
		{
			return wieldStyle;
		}


		public Builder()
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

		public Builder setParent(ResourceLocation parent)
		{
			this.parent = parent;
			return this;
		}

		@ApiStatus.Internal
		public static Builder deserialize(JsonElement jsonElement) throws JsonParseException
		{
			Builder builder = new Builder();
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
							.setSkillToCheck(EpicFightRegistries.SKILL.getHolder(ResourceLocation.parse(gsonObject.get("skill").getAsString())).get())
							.setSlot(SkillSlot.ENUM_MANAGER.get(gsonObject.get("slot").getAsString().toUpperCase()));
					case DATA_KEY -> builder
							.setType(type).setWieldStyle(wieldStyle).isVisibleOffHand(visibleOffHand)
							.setSkillToCheck(EpicFightRegistries.SKILL.getHolder(ResourceLocation.parse(gsonObject.get("skill").getAsString())).get())
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

		@ApiStatus.Internal
		private Builder merge()
		{
			Builder result = new Builder();
			Deque<Builder> hierarchy = new ArrayDeque<>();
			Builder current = this;

			// Collect parent chain
			while (current != null) {
				hierarchy.push(current);
				current = ConditionalManager.get(parent);
			}

			while (!hierarchy.isEmpty()) {
				Builder builder = hierarchy.pop();
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

		/**
		 * Finalizes the builder by merging parent hierarchy and constructing the logic predicate.
		 * @return A fully configured {@link ProviderConditional}.
		 */
		public ProviderConditional build()
		{
			return new ProviderConditional(merge());
		}


		/**
		 * Determines if the weapon should remain visible in the off-hand when this style is active.
		 * @param visibleOffHand True to render in off-hand, false to hide.
		 * @return This builder for chaining.
		 */
		public Builder isVisibleOffHand(boolean visibleOffHand) {
			this.visibleOffHand = visibleOffHand;
			return this;
		}

		/**
		 * Defines the {@link Style} to apply when this condition is met.
		 * @param wieldStyle The combat style (e.g., ONE_HAND, TWO_HAND).
		 * @return This builder for chaining.
		 */
		public Builder setWieldStyle(Style wieldStyle) {
			this.wieldStyle = wieldStyle;
			return this;
		}

		/**
		 * Sets the {@link ProviderConditionalType} for this logic.
		 * <p>Note: Different types require different parameters (e.g., DATA_KEY requires a Skill and a Key).</p>
		 * @param type The type of condition to evaluate.
		 * @return This builder for chaining.
		 */
		public Builder setType(@NotNull ProviderConditionalType type)
		{
			this.type = type;
			return this;
		}

		/**
		 * Configures a check for a specific {@link Skill} state.
		 * <p>Used primarily with {@code SKILL_EXISTENCE}, {@code SKILL_ACTIVATION}, or {@code DATA_KEY}.</p>
		 * @param skillToCheck The skill to monitor.
		 * @return This builder for chaining.
		 */
		public Builder setSkillToCheck(Holder<Skill> skillToCheck) {
			this.skillToCheck = skillToCheck;
			return this;
		}

		/**
		 * Sets the target {@link WeaponCategory} for this condition.
		 * <p>Used with {@link ProviderConditionalType#WEAPON_CATEGORY} to apply styles
		 * to entire groups of weapons (e.g., all Greatswords).</p>
		 * @param category The category to match against.
		 * @return This builder for chaining.
		 */
		public Builder setCategory(WeaponCategory category) {
			this.category = category;
			return this;
		}

		/**
		 * Sets a specific {@link Item} requirement for this condition.
		 * <p>Used with {@link ProviderConditionalType#SPECIFIC_WEAPON} for unique items
		 * that require a moveset different from their base category.</p>
		 * @param weapon The specific item instance to match.
		 * @return This builder for chaining.
		 */
		public Builder setWeapon(Item weapon) {
			this.weapon = weapon;
			return this;
		}

		/**
		 * Adds sub-conditionals to create a composite logic chain.
		 * @param providerConditionals A list of builders to evaluate.
		 * @throws IllegalArgumentException If a sub-conditional is itself a COMPOSITE type.
		 * @return This builder for chaining.
		 */
		public Builder setProviderConditionals(Builder... providerConditionals) {
			for (Builder conditional : providerConditionals)
			{
				if (conditional.type == ProviderConditionalType.COMPOSITE)
				{
					throw new IllegalArgumentException("Cannot have composite conditionals as sub-conditionals.");
				}
				this.providerConditionals.add(conditional);
			}
			return this;
		}

		/**
		 * Specifies the {@link SkillSlot} to check when evaluating skill-based conditions.
		 * <p>Commonly used to check the {@code WEAPON_INNATE} or {@code WEAPON_PASSIVE}
		 * slots for specific skill activations.</p>
		 * @param slot The skill slot to inspect.
		 * @return This builder for chaining.
		 */
		public Builder setSlot(SkillSlot slot) {
			this.slot = slot;
			return this;
		}

		/**
		 * Sets the boolean data key to check within a skill's data map.
		 * <p>Only utilized when the type is set to {@code DATA_KEY}.</p>
		 * @param key The holder for the {@link SkillDataKey}.
		 * @return This builder for chaining.
		 */
		public Builder setKey(Holder<SkillDataKey<?>> key) {
			this.key = key;
			return this;
		}

		/**
		 * Defines which {@link InteractionHand} must be holding the item for this condition to pass.
		 * <p>Essential for dual-wielding logic, allowing different movesets when an item
		 * is in the {@code OFF_HAND} versus the {@code MAIN_HAND}.</p>
		 * @param hand The hand to check (Main or Offhand).
		 * @return This builder for chaining.
		 */
		public Builder setHand(InteractionHand hand) {
			this.hand = hand;
			return this;
		}

		/**
		 * Provides a custom logic gate via a {@link Predicate}.
		 * <p>Used with {@link ProviderConditionalType#CUSTOM}. This allows for highly
		 * specialized checks based on the entity's current state (e.g., health,
		 * potion effects, or world conditions) that aren't covered by standard types.</p>
		 * @param customFunction A predicate that takes a {@link LivingEntityPatch} and returns true if the condition is met.
		 * @return This builder for chaining.
		 */
		public Builder setCustomFunction(Predicate<LivingEntityPatch<?>> customFunction) {
			this.customFunction = customFunction;
			return this;
		}
	}
}
