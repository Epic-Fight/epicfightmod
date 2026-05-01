package yesman.epicfight.api.client.physics.cloth;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import yesman.epicfight.api.utils.math.OpenMatrix4f;

/**
 * 0: Root,
 * 1: Thigh_R,
 * 2: "Leg_R",
 * 3: "Knee_R",
 * 4: "Thigh_L",
 * 5: "Leg_L",
 * 6: "Knee_L",
 * 7: "Torso",
 * 8: "Chest",
 * 9: "Head",
 * 10: "Shoulder_R",
 * 11: "Arm_R",
 * 12: "Hand_R",
 * 13: "Tool_R",
 * 14: "Elbow_R",
 * 15: "Shoulder_L",
 * 16: "Arm_L",
 * 17: "Hand_L",
 * 18: "Tool_L",
 * 19: "Elbow_L"
**/

public class ClothColliderPresets {
	public static final List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> BIPED_SLIM = ImmutableList.<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>>builder()
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[1], new ClothSimulator.ClothOBBCollider(0.125D, 0.24D, 0.125D, 0.0D, 0.22D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[2], new ClothSimulator.ClothOBBCollider(0.125D, 0.1875D, 0.125D, 0.0D, 0.1875D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[4], new ClothSimulator.ClothOBBCollider(0.125D, 0.24D, 0.125D, 0.0D, 0.22D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[5], new ClothSimulator.ClothOBBCollider(0.125D, 0.1875D, 0.125D, 0.0D, 0.1875D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[7], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.13D, 0.0D, 0.125D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[8], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.13D, 0.0D, 0.3D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[9], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.25D, 0.0D, 0.2D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[11], new ClothSimulator.ClothOBBCollider(0.12D, 0.24D, 0.125D, -0.05D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[12], new ClothSimulator.ClothOBBCollider(0.12D, 0.1875D, 0.125D, -0.05D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[16], new ClothSimulator.ClothOBBCollider(0.12D, 0.24D, 0.125D, 0.05D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[17], new ClothSimulator.ClothOBBCollider(0.12D, 0.1875D, 0.125D, 0.05D, 0.14D, 0.0D)))
			.build();
	
	public static final List<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>> BIPED = ImmutableList.<Pair<Function<ClothSimulatable, OpenMatrix4f>, ClothSimulator.ClothOBBCollider>>builder()
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[1], new ClothSimulator.ClothOBBCollider(0.125D, 0.24D, 0.125D, 0.0D, 0.22D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[2], new ClothSimulator.ClothOBBCollider(0.125D, 0.1875D, 0.125D, 0.0D, 0.1875D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[4], new ClothSimulator.ClothOBBCollider(0.125D, 0.24D, 0.125D, 0.0D, 0.22D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[5], new ClothSimulator.ClothOBBCollider(0.125D, 0.1875D, 0.125D, 0.0D, 0.1875D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[7], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.13D, 0.0D, 0.125D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[8], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.13D, 0.0D, 0.3D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[9], new ClothSimulator.ClothOBBCollider(0.25D, 0.25D, 0.25D, 0.0D, 0.2D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[11], new ClothSimulator.ClothOBBCollider(0.13D, 0.24D, 0.13D, -0.0D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[12], new ClothSimulator.ClothOBBCollider(0.13D, 0.1875D, 0.13D, -0.0D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[16], new ClothSimulator.ClothOBBCollider(0.13D, 0.24D, 0.13D, 0.0D, 0.14D, 0.0D)))
			.add(Pair.of((simObject) -> simObject.getArmature().getPoseMatrices()[17], new ClothSimulator.ClothOBBCollider(0.13D, 0.1875D, 0.13D, 0.0D, 0.14D, 0.0D)))
			.build();
}
