package yesman.epicfight.api.utils;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;
import java.util.function.BiFunction;

public class HitEntityList {
	private final List<Entity> hitEntites;
	private int index;
	
	public HitEntityList(LivingEntityPatch<?> attacker, List<Entity> entities, Priority priority) {
		this.index = -1;
		this.hitEntites = priority.sort(attacker, entities);
	}
	
	public Entity getEntity() {
		return this.hitEntites.get(this.index);
	}
	
	public boolean next() {
		this.index++;
		return this.hitEntites.size() > this.index;
	}
	
	public enum Priority {
		DISTANCE((attacker, list) -> {
			DoubleList distanceToAttacker = new DoubleArrayList();
			List<Entity> hitEntites = Lists.newArrayList();
			
			Outer:
			for (Entity entity : list) {
				double distance = attacker.getOriginal().distanceToSqr(entity);
				int index = 0;
				
				for (; index < hitEntites.size(); index++) {
					if (distance < distanceToAttacker.getDouble(index)) {
						hitEntites.add(index, entity);
						distanceToAttacker.add(index, distance);
						continue Outer;
					}
				}
				
				hitEntites.add(index, entity);
				distanceToAttacker.add(index, distance);
			}
			return hitEntites;
		}),
		
		TARGET((attacker, list) -> {
			List<Entity> hitEntites = Lists.newArrayList();
			
			for (Entity entity : list) {
				if (entity.is(attacker.getTarget())) {
					hitEntites.add(entity);
				}
			}
			
			return hitEntites;
		}),
		HOSTILITY((attacker, list) -> {
			List<Entity> firstTargets = Lists.newArrayList();
			List<Entity> secondTargets = Lists.newArrayList();
			List<Entity> lastTargets = Lists.newArrayList();
			
			Outer:
			for (Entity e : list) {
				if (attacker.isTargetInvulnerable(e)) {
					continue;
				}

				if (attacker.getOriginal().getLastHurtByMob() == e || attacker.getTarget() == e) {
					firstTargets.add(e);
					continue;
				}

				LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(e, LivingEntityPatch.class);

				if (entitypatch != null) {
					if (attacker.getOriginal().is(entitypatch.getTarget())) {
						firstTargets.add(e);
						continue;
					}
				}
				
				if (e instanceof Mob mob) {
					if (attacker.getOriginal().is(mob.getTarget())) {
						firstTargets.add(mob);
						continue;
					} else {
						GoalSelector targetingAi = mob.targetSelector;
						
						for (WrappedGoal goal : targetingAi.getAvailableGoals()) {
							if (goal.getGoal() instanceof NearestAttackableTargetGoal<?> targetGoal) {
								if (targetGoal.targetType.isAssignableFrom(attacker.getOriginal().getClass()) && targetGoal.targetConditions.test(mob, attacker.getOriginal())) {
									secondTargets.add(mob);
									continue Outer;
								}
							}
						}
					}
				}
				
				lastTargets.add(e);
			}
			
			secondTargets.addAll(lastTargets);
			firstTargets.addAll(secondTargets);
			
			return firstTargets;
		});
		
		BiFunction<LivingEntityPatch<?>, List<Entity>, List<Entity>> sortingFunction;
		
		Priority(BiFunction<LivingEntityPatch<?>, List<Entity>, List<Entity>> sortingFunction) {
			this.sortingFunction = sortingFunction;
		}
		
		public List<Entity> sort(LivingEntityPatch<?> attacker, List<Entity> entities) {
			return this.sortingFunction.apply(attacker, entities);
		}
	}
}