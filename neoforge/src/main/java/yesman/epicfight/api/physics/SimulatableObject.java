package yesman.epicfight.api.physics;

import java.util.Optional;

public interface SimulatableObject {
	<SIM extends PhysicsSimulator<?, ?, ?, ?, ?>> Optional<SIM> getSimulator(SimulationTypes<?, ?, ?, ?, ?, SIM> simulationType);
}
