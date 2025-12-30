package yesman.epicfight.api.physics;

import yesman.epicfight.api.physics.SimulationObject.SimulationObjectBuilder;

public interface SimulationObject<B extends SimulationObjectBuilder, PV extends SimulationProvider<O, ?, B, PV>, O> {
	/**
	 * An abstract method to specify the parameters needed to build SimulationObject
	 */
	public abstract class SimulationObjectBuilder {
	}
}