package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class HoglinMesh extends SkinnedMesh {
	public final SkinnedMeshPart head;
	public final SkinnedMeshPart body;
	public final SkinnedMeshPart leftFrontLeg;
	public final SkinnedMeshPart rightFrontLeg;
	public final SkinnedMeshPart leftBackLeg;
	public final SkinnedMeshPart rightBackLeg;
	
	public HoglinMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.head = this.getOrLogException(this.parts, "head");
		this.body = this.getOrLogException(this.parts, "body");
		this.leftFrontLeg = this.getOrLogException(this.parts, "leftFrontLeg");
		this.rightFrontLeg = this.getOrLogException(this.parts, "rightFrontLeg");
		this.leftBackLeg = this.getOrLogException(this.parts, "leftBackLeg");
		this.rightBackLeg = this.getOrLogException(this.parts, "rightBackLeg");
	}
}