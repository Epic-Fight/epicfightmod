package yesman.epicfight.api.physics;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import yesman.epicfight.api.physics.SimulationObject.SimulationObjectBuilder;

public interface PhysicsSimulator<KEY, B extends SimulationObjectBuilder, PV extends SimulationProvider<O, T, B, PV>, O, T extends SimulationObject<B, PV, O>> {
	public void tick(O object);
	
	public boolean isRunning(KEY key);
	
	public void runUntil(KEY key, PV provider, B builder, BooleanSupplier when);
	
	public void runWhen(KEY key, PV provider, B builder, BooleanSupplier when);
	
	public void restart(KEY key);
	
	public void stop(KEY key);
	
	public Optional<T> getRunningObject(KEY key);
}
