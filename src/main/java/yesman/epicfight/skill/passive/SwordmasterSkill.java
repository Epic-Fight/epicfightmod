package yesman.epicfight.skill.passive;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.neoevent.playerpatch.ModifyAttackSpeedEvent;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillEvent;
import yesman.epicfight.skill.SkillEvent.Side;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations;
import yesman.epicfight.world.capabilities.entitypatch.EntityDecorations.AnimationPropertyModifier;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class SwordmasterSkill extends PassiveSkill {
	public static class Builder extends SkillBuilder<SwordmasterSkill.Builder> {
		protected final Set<WeaponCategory> availableWeaponCategories = Sets.newHashSet();
		
		public Builder(Function<Builder, ? extends Skill> constructor) {
			super(constructor);
		}
		
		public Builder addAvailableWeaponCategory(WeaponCategory... wc) {
			this.availableWeaponCategories.addAll(Arrays.asList(wc));
			return this;
		}
	}
	
	public static SwordmasterSkill.Builder createSwordMasterBuilder() {
		return new SwordmasterSkill.Builder(SwordmasterSkill::new)
			.addAvailableWeaponCategory(WeaponCategories.UCHIGATANA, WeaponCategories.LONGSWORD, WeaponCategories.SWORD, WeaponCategories.TACHI)
			.setCategory(SkillCategories.PASSIVE)
			.setResource(Resource.NONE);
	}
	
	private float speedBonus;
	private Set<WeaponCategory> availableWeaponCategories;
	@OnlyIn(Dist.CLIENT)
	private List<WeaponCategory> availableWeaponCategoryList;
	
	public SwordmasterSkill(SwordmasterSkill.Builder builder) {
		super(builder);
		
		this.availableWeaponCategories = ImmutableSet.copyOf(builder.availableWeaponCategories);
	}
	
	@Override
	public void loadDatapackParameters(CompoundTag parameters) {
		super.loadDatapackParameters(parameters);
		this.speedBonus = parameters.getFloat("speed_bonus");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		if (!container.getExecutor().isLogicalClient()) {
			container.getExecutor().getEntityDecorations().addSwingSoundModifier(EntityDecorations.SWORDMASTER_SWING_SOUND, new AnimationPropertyModifier<> () {
				@Override
				public SoundEvent getModifiedValue(SoundEvent val, CapabilityItem object) {
					return (SwordmasterSkill.this.availableWeaponCategories.contains(object.getWeaponCategory()) && val == EpicFightSounds.WHOOSH.get()) ? EpicFightSounds.SWORDMASTER_SWING.get() : val;
				}
				
				@Override
				public boolean shouldRemove() {
					return !container.getExecutor().getPlayerSkills().isEquipping(SwordmasterSkill.this);
				}
			});
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void onInitiateClient(SkillContainer container) {
		container.getExecutor().getEntityDecorations().addTrailInfoModifier(EntityDecorations.SWORDMASTER_TRAIL_MODIFIER, new AnimationPropertyModifier<> () {
			@Override
			public TrailInfo getModifiedValue(TrailInfo val, CapabilityItem object) {
				if (SwordmasterSkill.this.getAvailableWeaponCategories().contains(object.getWeaponCategory())) {
					TrailInfo.Builder builder = val.unpackAsBuilder();
					builder.lifetime(val.trailLifetime() + 2);
					builder.blockLight(val.blockLight() + 10);
					if (val.texturePath().equals(TrailInfo.GENERIC_TRAIL_TEXTURE)) builder.texture(TrailInfo.SWORDMASTER_SWING_TRAIL_TEX);
					
					return builder.create();
				}
				
				return val;
			}
			
			@Override
			public boolean shouldRemove() {
				return !container.getExecutor().getPlayerSkills().isEquipping(SwordmasterSkill.this);
			}
		});
	}
	
	@SkillEvent(caller = EpicFightMod.MODID, side = Side.BOTH)
	public void modifyAttackSpeedEvent(ModifyAttackSpeedEvent event, SkillContainer skillContainer) {
		WeaponCategory heldWeaponCategory = event.getItemCapability().getWeaponCategory();

		if (this.availableWeaponCategories.contains(heldWeaponCategory)) {
			float attackSpeed = event.getAttackSpeed();
			event.setAttackSpeed(attackSpeed * (1.0F + this.speedBonus * 0.01F));
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<Object> getTooltipArgsOfScreen(List<Object> list) {
		list.add(String.format("%.0f", this.speedBonus));
		StringBuilder sb = new StringBuilder();
		int i = 0;
		
		for (WeaponCategory weaponCategory : this.availableWeaponCategories) {
			sb.append(WeaponCategory.ENUM_MANAGER.toTranslated(weaponCategory));
			if (i < this.availableWeaponCategories.size() - 1) sb.append(", ");
			i++;
		}
		
        list.add(sb.toString());
		
		return list;
	}
	
	@Override
	public Set<WeaponCategory> getAvailableWeaponCategories() {
		return this.availableWeaponCategories;
	}
}