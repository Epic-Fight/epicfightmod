package yesman.epicfight.api.animation.types;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import yesman.epicfight.api.animation.types.EntityState.StateFactor;
import yesman.epicfight.api.utils.datastruct.TypeFlexibleHashMap;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class StateSpectrum {
	private final Set<StatesInTime> timePairs = Sets.newHashSet();
	
	void readFrom(StateSpectrum.Blueprint blueprint) {
		this.timePairs.clear();
		this.timePairs.addAll(blueprint.timePairs);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSingleState(StateFactor<T> stateFactor, LivingEntityPatch<?> entitypatch, float time) {
		for (StatesInTime state : this.timePairs) {
			if (state.isIn(entitypatch, time)) {
				for (Map.Entry<StateFactor<?>, ?> timeEntry : state.getStates(entitypatch)) {
					if (timeEntry.getKey() == stateFactor) {
						return (T)timeEntry.getValue();
					}
				}
			}
		}
		
		return stateFactor.defaultValue();
	}
	
	public TypeFlexibleHashMap<StateFactor<?>> getStateMap(LivingEntityPatch<?> entitypatch, float time) {
		TypeFlexibleHashMap<StateFactor<?>> stateMap = new TypeFlexibleHashMap<>(true);
		
		for (StatesInTime state : this.timePairs) {
			if (state.isIn(entitypatch, time)) {
				for (Map.Entry<StateFactor<?>, ?> timeEntry : state.getStates(entitypatch)) {
					stateMap.put(timeEntry.getKey(), timeEntry.getValue());
				}
			}
		}
		
		return stateMap;
	}
	
	abstract static class StatesInTime {
		public abstract Set<Map.Entry<StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch);
		
		public abstract void removeState(StateFactor<?> state);
		
		public abstract boolean hasState(StateFactor<?> state);
		
		public abstract boolean isIn(LivingEntityPatch<?> entitypatch, float time);
	}
	
	static class SimpleStatesInTime extends StatesInTime {
		float start;
		float end;
		Map<StateFactor<?>, Object> states = Maps.newHashMap();
		
		public SimpleStatesInTime(float start, float end) {
			this.start = start;
			this.end = end;
		}
		
		@Override
		public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
			return this.start <= time && this.end > time;
		}
		
		public <T> StatesInTime addState(StateFactor<T> factor, T val) {
			this.states.put(factor, val);
			return this;
		}
		
		@Override
		public Set<Map.Entry<StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
			return this.states.entrySet();
		}
		
		@Override
		public boolean hasState(StateFactor<?> state) {
			return this.states.containsKey(state);
		}
		
		@Override
		public void removeState(StateFactor<?> state) {
			this.states.remove(state);
		}
		
		@Override
		public String toString() {
			return String.format("Time: %.2f ~ %.2f, States: %s", this.start, this.end, this.states);
		}
	}
	
	static class ConditionalStatesInTime extends StatesInTime {
		float start;
		float end;
		Int2ObjectMap<Map<StateFactor<?>, Object>> conditionalStates = new Int2ObjectOpenHashMap<>();
		Function<LivingEntityPatch<?>, Integer> condition;
		
		public ConditionalStatesInTime(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
			this.start = start;
			this.end = end;
			this.condition = condition;
		}
		
		public <T> StatesInTime addConditionalState(int metadata, StateFactor<T> factor, T val) {
			Map<StateFactor<?>, Object> states = this.conditionalStates.computeIfAbsent(metadata, (key) -> Maps.newHashMap());
			states.put(factor, val);
			
			return this;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public Set<Map.Entry<StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
			return this.conditionalStates.get(this.condition.apply(entitypatch)).entrySet();
		}
		
		@Override
		public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
			return this.start <= time && this.end > time;
		}
		
		@Override
		public boolean hasState(StateFactor<?> state) {
			boolean hasState = false;
			
			for (Map<StateFactor<?>, Object> states : this.conditionalStates.values()) {
				hasState |= states.containsKey(state);
			}
			
			return hasState;
		}
		
		@Override
		public void removeState(StateFactor<?> state) {
			for (Map<StateFactor<?>, Object> states : this.conditionalStates.values()) {
				states.remove(state);
			}
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			sb.append(String.format("Time: %.2f ~ %.2f, ", this.start, this.end));
			int entryCnt = 0;
			
			for (Map.Entry<Integer, Map<StateFactor<?>, Object>> entry : this.conditionalStates.entrySet()) {
				sb.append(String.format("States %d: %s", entry.getKey(), entry.getValue()));
				entryCnt++;
				
				if (entryCnt < this.conditionalStates.size()) {
					sb.append(", ");
				}
			}
			
			return sb.toString();
		}
	}
	
	static class VariableStatesInTime extends StatesInTime {
		Function<LivingEntityPatch<?>, Float> variableStart;
		Function<LivingEntityPatch<?>, Float> variableEnd;
		Map<StateFactor<?>, Object> states = Maps.newHashMap();
		
		public VariableStatesInTime(Function<LivingEntityPatch<?>, Float> variableStart, Function<LivingEntityPatch<?>, Float> variableEnd) {
			this.variableStart = variableStart;
			this.variableEnd = variableEnd;
		}
		
		@Override
		public boolean isIn(LivingEntityPatch<?> entitypatch, float time) {
			return this.variableStart.apply(entitypatch) <= time && this.variableEnd.apply(entitypatch) > time;
		}
		
		public <T> StatesInTime addState(StateFactor<T> factor, T val) {
			this.states.put(factor, val);
			return this;
		}
		
		@Override
		public Set<Map.Entry<StateFactor<?>, Object>> getStates(LivingEntityPatch<?> entitypatch) {
			return this.states.entrySet();
		}
		
		@Override
		public boolean hasState(StateFactor<?> state) {
			return this.states.containsKey(state);
		}
		
		@Override
		public void removeState(StateFactor<?> state) {
			this.states.remove(state);
		}
		
		@Override
		public String toString() {
			return String.format("States: %s", this.states);
		}
	}
	
	public static class Blueprint {
		StatesInTime currentState;
		Set<StatesInTime> timePairs = Sets.newHashSet();
		
		public Blueprint newTimePair(float start, float end) {
			this.currentState = new SimpleStatesInTime(start, end);
			this.timePairs.add(this.currentState);
			return this;
		}
		
		public Blueprint newConditionalTimePair(Function<LivingEntityPatch<?>, Integer> condition, float start, float end) {
			this.currentState = new ConditionalStatesInTime(condition, start, end);
			this.timePairs.add(this.currentState);
			return this;
		}
		
		public Blueprint newVariableTimePair(Function<LivingEntityPatch<?>, Float> variableStart, Function<LivingEntityPatch<?>, Float> variableEnd) {
			this.currentState = new VariableStatesInTime(variableStart, variableEnd);
			this.timePairs.add(this.currentState);
			return this;
		}
		
		public <T> Blueprint addState(StateFactor<T> factor, T val) {
			if (this.currentState instanceof SimpleStatesInTime simpleState) {
				simpleState.addState(factor, val);
			}
			
			if (this.currentState instanceof VariableStatesInTime variableState) {
				variableState.addState(factor, val);
			}
			
			return this;
		}
		
		public <T> Blueprint addConditionalState(int metadata, StateFactor<T> factor, T val) {
			if (this.currentState instanceof ConditionalStatesInTime conditionalState) {
				conditionalState.addConditionalState(metadata, factor, val);
			}
			
			return this;
		}
		
		public <T> Blueprint removeState(StateFactor<T> factor) {
			for (StatesInTime timePair : this.timePairs) {
				timePair.removeState(factor);
			}
			
			return this;
		}
		
		public <T> Blueprint addStateRemoveOld(StateFactor<T> factor, T val) {
			this.removeState(factor);
			return this.addState(factor, val);
		}
		
		public <T> Blueprint addStateIfNotExist(StateFactor<T> factor, T val) {
			for (StatesInTime timePair : this.timePairs) {
				if (timePair.hasState(factor)) {
					return this;
				}
			}
			
			return this.addState(factor, val);
		}
		
		public Blueprint clear() {
			this.currentState = null;
			this.timePairs.clear();
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			for (StatesInTime state : this.timePairs) {
				sb.append(state + "\n");
			}
			
			return sb.toString();
		}
	}
}