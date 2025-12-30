package yesman.epicfight.api.physics;

import javax.annotation.Nullable;

import yesman.epicfight.api.physics.SimulationObject.SimulationObjectBuilder;

public interface SimulationProvider<OWN, OBJ extends SimulationObject<?, ?, ?>, BUILDER extends SimulationObjectBuilder, P extends SimulationProvider<OWN, OBJ, BUILDER, P>> {
	public OBJ createSimulationData(@Nullable P provider, OWN simOwner, BUILDER simBuilder);
}