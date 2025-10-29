package yesman.epicfight.world.capabilities.skill;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashMultimap;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class CapabilitySkill {
	public static final CapabilitySkill EMPTY = new CapabilitySkill(null);
	public final SkillContainer[] skillContainers;
	private final Map<Skill, SkillContainer> containersBySkill = new HashMap<> ();
	private final HashMultimap<SkillCategory, SkillContainer> containersByCategory = HashMultimap.create();
	private final HashMultimap<SkillCategory, Skill> learnedSkills = HashMultimap.create();
	
	public CapabilitySkill(PlayerPatch<?> playerpatch) {
		Collection<SkillSlot> slots = SkillSlot.ENUM_MANAGER.universalValues();
		this.skillContainers = new SkillContainer[slots.size()];
		
		for (SkillSlot slot : slots) {
			SkillContainer skillContainer = new SkillContainer(playerpatch, slot);
			this.skillContainers[slot.universalOrdinal()] = skillContainer;
			this.containersByCategory.put(slot.category(), skillContainer);
		}
	}
	
	public void addLearnedSkill(Skill skill) {
		SkillCategory category = skill.getCategory();
		
		if (!this.learnedSkills.containsKey(category) || !this.learnedSkills.get(category).contains(skill)) {
			this.learnedSkills.put(category, skill);
		}
	}
	
	public boolean removeLearnedSkill(Skill skill) {
		SkillCategory category = skill.getCategory();
		
		if (this.learnedSkills.containsKey(category)) {
			if (this.learnedSkills.remove(category, skill)) {
				if (this.learnedSkills.get(category).isEmpty()) {
					this.learnedSkills.removeAll(category);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean hasCategory(SkillCategory skillCategory) {
		return this.learnedSkills.containsKey(skillCategory);
	}
	
	public boolean hasEmptyContainer(SkillCategory skillCategory) {
		for (SkillContainer container : this.containersByCategory.get(skillCategory)) {
			if (container.isEmpty()) return true; 
		}
		
		return false;
	}
	
	/**
	 * @return null if there is not empty container
	 */
	@Nullable
	public SkillContainer getFirstEmptyContainer(SkillCategory skillCategory) {
		for (SkillContainer container : this.containersByCategory.get(skillCategory)) {
			if (container.isEmpty()) return container; 
		}
		
		return null;
	}
	
	public boolean isEquipping(Skill skill) {
		return this.containersBySkill.containsKey(skill);
	}
	
	public boolean hasLearned(Skill skill) {
		return this.learnedSkills.get(skill.getCategory()).contains(skill);
	}
	
	public Set<SkillContainer> getSkillContainersFor(SkillCategory skillCategory) {
		return this.containersByCategory.get(skillCategory);
	}
	
	public SkillContainer getSkillContainerFor(SkillSlot skillSlot) {
		return this.getSkillContainerFor(skillSlot.universalOrdinal());
	}
	
	public SkillContainer getSkillContainerFor(int slotIndex) {
		return this.skillContainers[slotIndex];
	}
	
	@ApiStatus.Internal
	public void setSkillToContainer(Skill skill, SkillContainer container) {
		this.containersBySkill.put(skill, container);
	}
	
	@ApiStatus.Internal
	public void removeSkillFromContainer(Skill skill) {
		this.containersBySkill.remove(skill);
	}
	
	public SkillContainer getSkillContainer(Skill skill) {
		return this.containersBySkill.get(skill);
	}
	
	public Stream<SkillContainer> listSkillContainers() {
		return Stream.of(this.skillContainers);
	}
	
	public Stream<Skill> listAcquiredSkills() {
		return this.learnedSkills.values().stream();
	}
	
	public void clearContainersAndLearnedSkills(boolean isLocalOrServerPlayer) {
		for (SkillContainer container : this.skillContainers) {
			if (container.getSlot().category().learnable()) {
				if (isLocalOrServerPlayer) { container.setSkill(null); container.setReplaceCooldown(0); }
				else { container.setSkillRemote(null); container.setReplaceCooldown(0); }
			}
		}
		
		this.learnedSkills.clear();
	}
	
	public void copyFrom(CapabilitySkill capabilitySkill) {
		int i = 0;
		
		for (SkillContainer container : this.skillContainers) {
			Skill oldone = capabilitySkill.skillContainers[i].getSkill();
			
			if (oldone != null && oldone.getCategory().shouldSynchronize()) {
				container.setSkill(capabilitySkill.skillContainers[i].getSkill());
				container.setReplaceCooldown(capabilitySkill.skillContainers[i].getReplaceCooldown());
			}
			
			i++;
		}
		
		this.learnedSkills.putAll(capabilitySkill.learnedSkills);
	}
	
	public CompoundTag serialize() {
		CompoundTag compound = new CompoundTag();
		
		for (SkillContainer container : this.skillContainers) {
			if (container.getSkill() != null && container.getSkill().getCategory().shouldSave()) {
				compound.putString(ParseUtil.toLowerCase(container.getSlot().toString()), container.getSkill().toString());
			}
		}
		
		CompoundTag replaceCooldownNbt = new CompoundTag();
		
		for (SkillContainer container : this.skillContainers) {
			replaceCooldownNbt.putInt(ParseUtil.toLowerCase(container.getSlot().toString()), container.getReplaceCooldown());
		}
		
		compound.put("replace_cooldowns", replaceCooldownNbt);
		
		for (Map.Entry<SkillCategory, Collection<Skill>> entry : this.learnedSkills.asMap().entrySet()) {
			CompoundTag learnedNBT = new CompoundTag();
			int i = 0;
			
			for (Skill skill : entry.getValue()) {
				learnedNBT.putString(String.valueOf(i++), skill.toString());
			}
			
			compound.put("learned:" + ParseUtil.toLowerCase(entry.getKey().toString()), learnedNBT);
		}
		
		compound.putString("playerMode", this.skillContainers[0].getExecutor().getPlayerMode().toString());
		
		return compound;
	}
	
	public void deserialize(CompoundTag compound) {
		for (SkillContainer container : this.skillContainers) {
			String key = ParseUtil.toLowerCase(container.getSlot().toString());
			
			if (compound.contains(key)) {
				Skill skill = SkillManager.getSkill(compound.getString(key));
				
				if (skill != null) {
					container.setSkill(skill);
					container.setReplaceCooldown(0);
					this.addLearnedSkill(skill);
				}
			}
		}
		
		if (compound.contains("replace_cooldowns")) {
			CompoundTag replaceCooldownCompound = compound.getCompound("replace_cooldowns");
			
			for (SkillContainer container : this.skillContainers) {
				String slotName = ParseUtil.toLowerCase(container.getSlot().toString());
				
				if (replaceCooldownCompound.contains(slotName)) {
					container.setReplaceCooldown(replaceCooldownCompound.getInt(slotName));
				}
			}
		}
		
		for (SkillCategory category : SkillCategory.ENUM_MANAGER.universalValues()) {
			if (compound.contains("learned:" + ParseUtil.toLowerCase(category.toString()))) {
				CompoundTag learnedSkillsCompound = compound.getCompound("learned:" + ParseUtil.toLowerCase(category.toString()));
				
				for (String key : learnedSkillsCompound.getAllKeys()) {
					Skill skill = SkillManager.getSkill(learnedSkillsCompound.getString(key));
					
					if (skill != null) {
						this.addLearnedSkill(skill);
					}
				}
			}
		}
		
		if (compound.contains("playerMode")) {
			String playerMode = compound.getString("playerMode");
			
			// Parse old name
			if ("MINING".equals(playerMode)) {
				playerMode = "VANILLA";
			} else if ("BATTLE".equals(playerMode)) {
				playerMode = "EPICFIGHT";
			}
			
			this.skillContainers[0].getExecutor().toMode(PlayerPatch.PlayerMode.valueOf(ParseUtil.toUpperCase(playerMode)), true);
		} else {
			this.skillContainers[0].getExecutor().toEpicFightMode(true);
		}
	}
}