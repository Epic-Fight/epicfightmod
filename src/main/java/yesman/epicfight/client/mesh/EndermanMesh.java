package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class EndermanMesh extends SkinnedMesh {
	public final SkinnedMeshPart headTop;
	public final SkinnedMeshPart headBottom;
	public final SkinnedMeshPart torso;
	public final SkinnedMeshPart leftArm;
	public final SkinnedMeshPart rightArm;
	public final SkinnedMeshPart leftLeg;
	public final SkinnedMeshPart rightLeg;
	
	public EndermanMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.headTop = this.getOrLogException(this.parts, "headTop");
		this.headBottom = this.getOrLogException(this.parts, "headBottom");
		this.torso = this.getOrLogException(this.parts, "torso");
		this.leftArm = this.getOrLogException(this.parts, "leftArm");
		this.rightArm = this.getOrLogException(this.parts, "rightArm");
		this.leftLeg = this.getOrLogException(this.parts, "leftLeg");
		this.rightLeg = this.getOrLogException(this.parts, "rightLeg");
	}
}