package yesman.epicfight.api.animation.types;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.datastructure.ParameterizedHashMap;

import java.util.function.Function;

public class EntityState {
	public static class StateFactor<T> implements ParameterizedHashMap.ParameterizedKey<T> {
		private final String name;
		private final T defaultValue;
		
		public StateFactor(String name, T defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		
		public String toString() {
			return this.name;
		}
		
		public T defaultValue() {
			return this.defaultValue;
		}
	}
	
	public static final EntityState DEFAULT_STATE = new EntityState(new ParameterizedHashMap<StateFactor<?>> ());
	
	public static final StateFactor<Boolean> TURNING_LOCKED = new StateFactor<>("turningLocked", false);
	public static final StateFactor<Boolean> MOVEMENT_LOCKED = new StateFactor<>("movementLocked", false);
	public static final StateFactor<Boolean> ATTACKING = new StateFactor<>("attacking", false);
	public static final StateFactor<Boolean> COMBO_ATTACKS_DOABLE = new StateFactor<>("comboAttacksDoable", true);
	public static final StateFactor<Boolean> SKILL_EXECUTABLE = new StateFactor<>("skillExecutable", true);
	public static final StateFactor<Boolean> CAN_USE_ITEM = new StateFactor<>("canUseItem", true);
	public static final StateFactor<Boolean> CAN_SWITCH_HAND_ITEM = new StateFactor<>("canSwitchHandItem", true);
	public static final StateFactor<Boolean> INACTION = new StateFactor<>("takingAction", false);
	public static final StateFactor<Boolean> KNOCKDOWN = new StateFactor<>("knockdown", false);
	public static final StateFactor<Boolean> LOOK_TARGET = new StateFactor<>("lookTarget", false);
	public static final StateFactor<Boolean> UPDATE_LIVING_MOTION = new StateFactor<>("updateLivingMotion", true);
	public static final StateFactor<Integer> HURT_LEVEL = new StateFactor<>("hurtLevel", 0);
	public static final StateFactor<Integer> PHASE_LEVEL = new StateFactor<>("phaseLevel", 0);
	public static final StateFactor<Function<DamageSource, AttackResult.ResultType>> ATTACK_RESULT = new StateFactor<>("attackResultModifier", (damagesource) -> AttackResult.ResultType.SUCCESS);
	public static final StateFactor<ProjectileHitPredicate> PROJECTILE_IMPACT_RESULT = new StateFactor<>("projectileImpactResult", (projectile, hitResult) -> true);
	
	private final ParameterizedHashMap<StateFactor<?>> stateMap;
	
	public EntityState(ParameterizedHashMap<StateFactor<?>> states) {
		this.stateMap = states;
	}
	
	public <T> T getState(StateFactor<T> stateFactor) {
		return this.stateMap.getOrDefault(stateFactor);
	}
	
	public ParameterizedHashMap<StateFactor<?>> getStateMap() {
		return this.stateMap;
	}
	
	public boolean turningLocked() {
		return this.getState(EntityState.TURNING_LOCKED);
	}
	
	public boolean movementLocked() {
		return this.getState(EntityState.MOVEMENT_LOCKED);
	}
	
	public boolean attacking() {
		return this.getState(EntityState.ATTACKING);
	}
	
	public AttackResult.ResultType attackResult(DamageSource damagesource) {
		return this.getState(EntityState.ATTACK_RESULT).apply(damagesource);
	}
	
	public boolean setProjectileImpactResult(Projectile projectile, HitResult hitResult) {
		return this.getState(EntityState.PROJECTILE_IMPACT_RESULT).test(projectile, hitResult);
	}
	
	public boolean canBasicAttack() {
		return this.getState(EntityState.COMBO_ATTACKS_DOABLE);
	}
	
	public boolean canUseSkill() {
		return this.getState(EntityState.SKILL_EXECUTABLE);
	}
	
	public boolean canUseItem() {
		return this.canUseSkill() && this.getState(EntityState.CAN_USE_ITEM);
	}
	
	public boolean canSwitchHoldingItem() {
		return !this.inaction() && this.getState(EntityState.CAN_SWITCH_HAND_ITEM);
	}
	
	public boolean inaction() {
		return this.getState(EntityState.INACTION);
	}
	
	public boolean updateLivingMotion() {
		return this.getState(EntityState.UPDATE_LIVING_MOTION);
	}
	
	public boolean hurt() {
		return this.getState(EntityState.HURT_LEVEL) > 0;
	}
	
	public int hurtLevel() {
		return this.getState(EntityState.HURT_LEVEL);
	}
	
	public boolean knockDown() {
		return this.getState(EntityState.KNOCKDOWN);
	}
	
	public boolean lookTarget() {
		return this.getState(EntityState.LOOK_TARGET);
	}
	
	/**
	 * 1: anticipation
	 * 2: attacking
	 * 3: recovery
	 * @return level
	 */
	public int getLevel() {
		return this.getState(EntityState.PHASE_LEVEL);
	}
	
	@Override
	public String toString() {
		return this.stateMap.toString();
	}

    @FunctionalInterface
    public interface ProjectileHitPredicate {
        boolean test(Projectile projectile, HitResult hitResult);
    }
}