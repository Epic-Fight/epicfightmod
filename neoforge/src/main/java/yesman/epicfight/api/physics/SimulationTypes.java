package yesman.epicfight.api.physics;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.physics.ik.InverseKinematicsProvider;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulatable;
import yesman.epicfight.api.physics.ik.InverseKinematicsSimulator;

public interface SimulationTypes<KEY, O, PV extends SimulationProvider<O, DATA, B, PV>, B extends SimulationObject.SimulationObjectBuilder, DATA extends SimulationObject<B, PV, O>, SIM extends PhysicsSimulator<KEY, B, PV, O, DATA>> {
	SimulationTypes<ResourceLocation, ClothSimulatable, SoftBodyTranslatable, ClothSimulator.ClothObjectBuilder, ClothSimulator.ClothObject, ClothSimulator> CLOTH = new SimulationTypes<> () {};
	SimulationTypes<Joint, InverseKinematicsSimulatable, InverseKinematicsProvider, InverseKinematicsSimulator.InverseKinematicsBuilder, InverseKinematicsSimulator.InverseKinematicsObject, InverseKinematicsSimulator> INVERSE_KINEMATICS = new SimulationTypes<> () {};
}