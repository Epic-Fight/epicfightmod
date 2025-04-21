package yesman.epicfight.skill.passive;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

public class SwordmasterSkill extends PassiveSkill {
	private static final UUID EVENT_UUID = UUID.fromString("a395b692-fd97-11eb-9a03-0242ac130003");
	
	public static class Builder extends SkillBuilder<SwordmasterSkill> {
		protected final Set<WeaponCategory> availableWeaponCategories = Sets.newHashSet();
		
		public Builder addAvailableWeaponCategory(WeaponCategory... wc) {
			this.availableWeaponCategories.addAll(Arrays.asList(wc));
			return this;
		}
	}
	
	public static SwordmasterSkill.Builder createSwordMasterBuilder() {
		return new SwordmasterSkill.Builder()
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
		
		this.availableWeaponCategories = builder.availableWeaponCategories;
	}
	
	@Override
	public void setParams(CompoundTag parameters) {
		super.setParams(parameters);
		this.speedBonus = parameters.getFloat("speed_bonus");
	}
	
	@Override
	public void onInitiate(SkillContainer container) {
		super.onInitiate(container);
		
		container.getExecutor().getEventListener().addEventListener(EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID, (event) -> {
			WeaponCategory heldWeaponCategory = event.getItemCapability().getWeaponCategory();
			
			if (this.availableWeaponCategories.contains(heldWeaponCategory)) {
				float attackSpeed = event.getAttackSpeed();
				event.setAttackSpeed(attackSpeed * (1.0F + this.speedBonus * 0.01F));
			}
		});
	}
	
	@Override
	public void onRemoved(SkillContainer container) {
		super.onRemoved(container);
		
		container.getExecutor().getEventListener().removeListener(EventType.MODIFY_ATTACK_SPEED_EVENT, EVENT_UUID);
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
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public List<WeaponCategory> getAvailableWeaponCategories() {
		if (this.availableWeaponCategoryList == null) {
			this.availableWeaponCategoryList = List.copyOf(this.availableWeaponCategories);
		}
		
		return this.availableWeaponCategoryList;
	}
}