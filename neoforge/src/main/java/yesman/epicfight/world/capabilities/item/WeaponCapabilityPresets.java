package yesman.epicfight.world.capabilities.item;

import java.util.function.Function;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.registry.entries.EpicFightSkillDataKeys;
import yesman.epicfight.registry.entries.EpicFightSkills;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.CapabilityItem.ZoomInType;

public abstract class WeaponCapabilityPresets {
	public static int vanillaTierToLevel(Tier tier) {
		if (tier instanceof Tiers vanillaTier) {
			switch (vanillaTier) {
			case WOOD -> {return 0;}
			case STONE -> {return 1;}
			case IRON -> {return 2;}
			case DIAMOND -> {return 3;}
			case GOLD -> {return 0;}
			case NETHERITE -> {return 4;}
			}
		}
		
		double sqrt = Math.sqrt(tier.getUses());
		
		// Custom tier mapping
		return sqrt < 10.0D ? 0 : (int)Math.round(sqrt / 10.0D);
	}
	
	public static final Function<Item, WeaponCapability.Builder> AXE = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.AXE)
			.hitSound(EpicFightSounds.BLADE_HIT.get())
			.collider(ColliderPreset.TOOLS)
			.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.THE_GUILLOTINE.get())
			.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD);
		
		if (item instanceof TieredItem tieredItem) {
			int tierLevel = vanillaTierToLevel(tieredItem.getTier());
			
			if (tierLevel != 0) {
				builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.ARMOR_NEGATION, EpicFightAttributes.getArmorNegationModifier(10.0D * tierLevel));
			}
			
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(0.7D + 0.3D * tierLevel));
		}
		
		return builder;
	};
	
	public static final Function<Item, WeaponCapability.Builder> HOE = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.HOE)
			.hitSound(EpicFightSounds.BLADE_HIT.get())
			.collider(ColliderPreset.TOOLS).newStyleCombo(Styles.ONE_HAND, Animations.TOOL_AUTO1, Animations.TOOL_AUTO2, Animations.TOOL_DASH, Animations.SWORD_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);
		
		if (item instanceof TieredItem tieredItem) {
			int tierLevel = vanillaTierToLevel(tieredItem.getTier());;
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(-0.4D + 0.1D * tierLevel));
		}
		
		return builder;
	};
	
	public static final Function<Item, WeaponCapability.Builder> PICKAXE = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.PICKAXE)
			.hitSound(EpicFightSounds.BLADE_HIT.get())
			.collider(ColliderPreset.TOOLS)
			.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);
		
		if (item instanceof TieredItem tieredItem) {
			int tierLevel = vanillaTierToLevel(tieredItem.getTier());;
			
			if (tierLevel != 0) {
				builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.ARMOR_NEGATION, EpicFightAttributes.getArmorNegationModifier(6.0D * tierLevel));
			}
			
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(0.4D + 0.1D * tierLevel));
		}
		
		return builder;
	};
	
	public static final Function<Item, WeaponCapability.Builder> SHOVEL = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.SHOVEL)
			.collider(ColliderPreset.TOOLS)
			.newStyleCombo(Styles.ONE_HAND, Animations.AXE_AUTO1, Animations.AXE_AUTO2, Animations.AXE_DASH, Animations.AXE_AIRSLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK);
		
		if (item instanceof TieredItem tieredItem) {
			int tierLevel = vanillaTierToLevel(tieredItem.getTier());;
			builder.addStyleAttibutes(CapabilityItem.Styles.COMMON, EpicFightAttributes.IMPACT, EpicFightAttributes.getImpactModifier(0.8D + 0.4D * tierLevel));
		}
		
		return builder;
	};
	
	public static final Function<Item, WeaponCapability.Builder> SWORD = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.SWORD)
			.styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == WeaponCategories.SWORD ? Styles.TWO_HAND : Styles.ONE_HAND)
			.collider(ColliderPreset.SWORD)
			.newStyleCombo(Styles.ONE_HAND, Animations.SWORD_AUTO1, Animations.SWORD_AUTO2, Animations.SWORD_AUTO3, Animations.SWORD_DASH, Animations.SWORD_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.SWORD_DUAL_AUTO1, Animations.SWORD_DUAL_AUTO2, Animations.SWORD_DUAL_AUTO3, Animations.SWORD_DUAL_DASH, Animations.SWORD_DUAL_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.SWEEPING_EDGE.get())
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.DANCING_EDGE.get())
			.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SWORD_DUAL_GUARD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_DUAL_WEAPON)
			.weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).getWeaponCategory() == WeaponCategories.SWORD);
		
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};
	public static final Function<Item, WeaponCapability.Builder> SPEAR = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.SPEAR)
			.swingSound(EpicFightSounds.WHOOSH_ROD.get())
			.styleProvider((playerpatch) -> (playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == WeaponCategories.SHIELD) ? Styles.ONE_HAND : Styles.TWO_HAND)
			.collider(ColliderPreset.SPEAR)
			.canBePlacedOffhand(false)
			.reach(1.0F)
			.newStyleCombo(Styles.ONE_HAND, Animations.SPEAR_ONEHAND_AUTO, Animations.SPEAR_DASH, Animations.SPEAR_ONEHAND_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.SPEAR_TWOHAND_AUTO1, Animations.SPEAR_TWOHAND_AUTO2, Animations.SPEAR_DASH, Animations.SPEAR_TWOHAND_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SPEAR_MOUNT_ATTACK)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.HEARTPIERCER.get())
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.GRASPING_SPIRE.get())
			.livingMotionModifier(Styles.ONE_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_SPEAR)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.SPEAR_GUARD);
		
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};

	public static final Function<Item, WeaponCapability.Builder> GREATSWORD = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.GREATSWORD)
			.styleProvider((playerpatch) -> Styles.TWO_HAND)
			.collider(ColliderPreset.GREATSWORD)
			.swingSound(EpicFightSounds.WHOOSH_BIG.get())
			.canBePlacedOffhand(false)
			.reach(1.0F)
			.newStyleCombo(Styles.TWO_HAND, Animations.GREATSWORD_AUTO1, Animations.GREATSWORD_AUTO2, Animations.GREATSWORD_DASH, Animations.GREATSWORD_AIR_SLASH)
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.STEEL_WHIRLWIND.get())
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_GREATSWORD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_GREATSWORD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_GREATSWORD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.JUMP, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLY, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_FLY, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CREATIVE_IDLE, Animations.BIPED_HOLD_GREATSWORD)
	    	.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.GREATSWORD_GUARD);
		
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};

	public static final Function<Item, WeaponCapability.Builder> UCHIGATANA = (item) ->
		WeaponCapability.builder()
			.category(WeaponCategories.UCHIGATANA)
			.styleProvider((entitypatch) -> {
				if (entitypatch instanceof PlayerPatch<?> playerpatch && (playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().hasData(EpicFightSkillDataKeys.SHEATH) &&
							playerpatch.getSkill(SkillSlots.WEAPON_PASSIVE).getDataManager().getDataValue(EpicFightSkillDataKeys.SHEATH))) {
						return Styles.SHEATH;
				}
				return Styles.TWO_HAND;
			})
			.passiveSkill(EpicFightSkills.BATTOJUTSU_PASSIVE.get())
			.hitSound(EpicFightSounds.BLADE_HIT.get())
			.collider(ColliderPreset.UCHIGATANA)
			.canBePlacedOffhand(false)
			.newStyleCombo(Styles.SHEATH, Animations.UCHIGATANA_SHEATHING_AUTO, Animations.UCHIGATANA_SHEATHING_DASH, Animations.UCHIGATANA_SHEATH_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.UCHIGATANA_AUTO1, Animations.UCHIGATANA_AUTO2, Animations.UCHIGATANA_AUTO3, Animations.UCHIGATANA_DASH, Animations.UCHIGATANA_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.SHEATH, (itemstack) -> EpicFightSkills.BATTOJUTSU.get())
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.BATTOJUTSU.get())
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_WALK_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_WALK_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.IDLE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.KNEEL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.WALK, Animations.BIPED_WALK_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.CHASE, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.RUN, Animations.BIPED_RUN_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.SNEAK, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.SWIM, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.FLOAT, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.SHEATH, LivingMotions.FALL, Animations.BIPED_HOLD_UCHIGATANA_SHEATHING)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.UCHIGATANA_GUARD);

	public static final Function<Item, WeaponCapability.Builder> TACHI = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.TACHI)
			.styleProvider((playerpatch) -> Styles.TWO_HAND)
			.collider(ColliderPreset.TACHI)
			.canBePlacedOffhand(false)
			.newStyleCombo(Styles.TWO_HAND, Animations.TACHI_AUTO1, Animations.TACHI_AUTO2, Animations.TACHI_AUTO3, Animations.TACHI_DASH, Animations.LONGSWORD_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.RUSHING_TEMPO.get())
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_TACHI)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);
	
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};

	public static final Function<Item, WeaponCapability.Builder> LONGSWORD = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.LONGSWORD)
			.styleProvider((playerpatch) -> {
				if (playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == WeaponCategories.SHIELD) {
					return Styles.ONE_HAND;
				} else if (playerpatch instanceof PlayerPatch<?> tplayerpatch) {
					return tplayerpatch.getSkill(SkillSlots.WEAPON_INNATE).isActivated() ? Styles.OCHS : Styles.TWO_HAND;
				}
				
				return Styles.TWO_HAND;
			})
			.collider(ColliderPreset.LONGSWORD)
			.canBePlacedOffhand(false)
			.newStyleCombo(Styles.ONE_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.LONGSWORD_AUTO1, Animations.LONGSWORD_AUTO2, Animations.LONGSWORD_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.newStyleCombo(Styles.OCHS, Animations.LONGSWORD_LIECHTENAUER_AUTO1, Animations.LONGSWORD_LIECHTENAUER_AUTO2, Animations.LONGSWORD_LIECHTENAUER_AUTO3, Animations.LONGSWORD_DASH, Animations.LONGSWORD_AIR_SLASH)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.SHARP_STAB.get())
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.LIECHTENAUER.get())
			.innateSkill(Styles.OCHS, (itemstack) -> EpicFightSkills.LIECHTENAUER.get())
			.livingMotionModifier(Styles.COMMON, LivingMotions.IDLE, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.WALK, Animations.BIPED_WALK_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.CHASE, Animations.BIPED_WALK_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.RUN, Animations.BIPED_RUN_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.SNEAK, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.KNEEL, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.JUMP, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.SWIM, Animations.BIPED_HOLD_LONGSWORD)
			.livingMotionModifier(Styles.COMMON, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
			.livingMotionModifier(Styles.OCHS, LivingMotions.IDLE, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.WALK, Animations.BIPED_WALK_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.CHASE, Animations.BIPED_WALK_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.RUN, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.SNEAK, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.KNEEL, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.JUMP, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.OCHS, LivingMotions.SWIM, Animations.BIPED_HOLD_LIECHTENAUER)
			.livingMotionModifier(Styles.ONE_HAND, LivingMotions.BLOCK, Animations.SWORD_GUARD)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD)
			.livingMotionModifier(Styles.OCHS, LivingMotions.BLOCK, Animations.LONGSWORD_GUARD);
		
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};
	
	public static final Function<Item, WeaponCapability.Builder> DAGGER = (item) -> {
		WeaponCapability.Builder builder = WeaponCapability.builder()
			.category(WeaponCategories.DAGGER)
			.styleProvider((playerpatch) -> playerpatch.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == WeaponCategories.DAGGER ? Styles.TWO_HAND : Styles.ONE_HAND)
			.swingSound(EpicFightSounds.WHOOSH_SMALL.get())
			.collider(ColliderPreset.DAGGER)
			.weaponCombinationPredicator((entitypatch) -> EpicFightCapabilities.getItemStackCapability(entitypatch.getOriginal().getOffhandItem()).getWeaponCategory() == WeaponCategories.DAGGER)
			.newStyleCombo(Styles.ONE_HAND, Animations.DAGGER_AUTO1, Animations.DAGGER_AUTO2, Animations.DAGGER_AUTO3, Animations.DAGGER_DASH, Animations.DAGGER_AIR_SLASH)
			.newStyleCombo(Styles.TWO_HAND, Animations.DAGGER_DUAL_AUTO1, Animations.DAGGER_DUAL_AUTO2, Animations.DAGGER_DUAL_AUTO3, Animations.DAGGER_DUAL_AUTO4, Animations.DAGGER_DUAL_DASH, Animations.DAGGER_DUAL_AIR_SLASH)
			.newStyleCombo(Styles.MOUNT, Animations.SWORD_MOUNT_ATTACK)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.EVISCERATE.get())
			.innateSkill(Styles.TWO_HAND, (itemstack) -> EpicFightSkills.BLADE_RUSH.get())
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.IDLE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.KNEEL, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.WALK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.CHASE, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.RUN, Animations.BIPED_RUN_DUAL)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SNEAK, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.SWIM, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FLOAT, Animations.BIPED_HOLD_DUAL_WEAPON)
			.livingMotionModifier(Styles.TWO_HAND, LivingMotions.FALL, Animations.BIPED_HOLD_DUAL_WEAPON);
		
		if (item instanceof TieredItem tieredItem) {
			builder.hitSound(tieredItem.getTier() == Tiers.WOOD ? EpicFightSounds.BLUNT_HIT.get() : EpicFightSounds.BLADE_HIT.get());
			builder.hitParticle(tieredItem.getTier() == Tiers.WOOD ? EpicFightParticles.HIT_BLUNT.get() : EpicFightParticles.HIT_BLADE.get());
		}
		
		return builder;
	};

	public static final Function<Item, WeaponCapability.Builder> FIST = (item) -> WeaponCapability.builder()
			.newStyleCombo(Styles.ONE_HAND, Animations.FIST_AUTO1, Animations.FIST_AUTO2, Animations.FIST_AUTO3, Animations.FIST_DASH, Animations.FIST_AIR_SLASH)
			.innateSkill(Styles.ONE_HAND, (itemstack) -> EpicFightSkills.RELENTLESS_COMBO.get())
			.category(WeaponCategories.FIST)
			.constructor(GloveCapability::new);
	
	public static final Function<Item, RangedWeaponCapability.Builder> BOW =  (item) -> RangedWeaponCapability.builder()
			.zoomInType(ZoomInType.USE_TICK)
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_BOW_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_BOW_SHOT)
			.constructor(BowCapability::new);
			
	public static final Function<Item, RangedWeaponCapability.Builder> CROSSBOW =  (item) -> RangedWeaponCapability.builder()
			.zoomInType(ZoomInType.AIMING)
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.KNEEL, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.RUN, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.SNEAK, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.SWIM, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.FLOAT, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.FALL, Animations.BIPED_HOLD_CROSSBOW)
			.addAnimationsModifier(LivingMotions.RELOAD, Animations.BIPED_CROSSBOW_RELOAD)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_CROSSBOW_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_CROSSBOW_SHOT)
			.constructor(CrossbowCapability::new);
	
	public static final Function<Item, RangedWeaponCapability.Builder> TRIDENT = (item) -> RangedWeaponCapability.builder()
			.zoomInType(ZoomInType.USE_TICK)
			.addAnimationsModifier(LivingMotions.IDLE, Animations.BIPED_IDLE)
			.addAnimationsModifier(LivingMotions.WALK, Animations.BIPED_WALK)
			.addAnimationsModifier(LivingMotions.AIM, Animations.BIPED_JAVELIN_AIM)
			.addAnimationsModifier(LivingMotions.SHOT, Animations.BIPED_JAVELIN_THROW)
			.constructor(TridentCapability::new)
			.collider(ColliderPreset.SPEAR)
			.category(WeaponCategories.TRIDENT);
	
	public static final Function<Item, CapabilityItem.Builder<?>> SHIELD = (item) -> CapabilityItem.builder()
			.constructor(ShieldCapability::new)
			.category(WeaponCategories.SHIELD);
}
