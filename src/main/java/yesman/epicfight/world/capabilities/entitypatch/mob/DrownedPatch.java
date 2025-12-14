package yesman.epicfight.world.capabilities.entitypatch.mob;

import com.google.common.collect.ImmutableMap;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Drowned;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.world.capabilities.entitypatch.HumanoidMobPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public class DrownedPatch extends ZombiePatch<Drowned> {
	@Override
	protected void setWeaponMotions() {
		super.setWeaponMotions();
		this.weaponAttackMotions.put(WeaponCategories.TRIDENT, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.DROWNED_TRIDENT));
	}
	
	@Override
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
		
		if (builder != null) {
			this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
			this.original.goalSelector.addGoal(1, new DrownedTargetChasingGoal(this, this.getOriginal(), 1.0D, true));
		}
	}
	
	static class DrownedTargetChasingGoal extends TargetChasingGoal {
		private final Drowned drowned;
		
		public DrownedTargetChasingGoal(DrownedPatch mobpatch, PathfinderMob pathfinderMob, double speedModifier, boolean longMemory) {
			super(mobpatch, pathfinderMob, speedModifier, longMemory);
			this.drowned = mobpatch.getOriginal();
		}
		
		@Override
		public boolean canUse() {
	        return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
	    }
		
		@Override
		public boolean canContinueToUse() {
			return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
	    }
	}
}