package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class SpiderMesh extends SkinnedMesh {
	public final SkinnedMeshPart head;
	public final SkinnedMeshPart middleStomach;
	public final SkinnedMeshPart bottomStomach;
	public final SkinnedMeshPart leftLeg1;
	public final SkinnedMeshPart leftLeg2;
	public final SkinnedMeshPart leftLeg3;
	public final SkinnedMeshPart leftLeg4;
	public final SkinnedMeshPart rightLeg1;
	public final SkinnedMeshPart rightLeg2;
	public final SkinnedMeshPart rightLeg3;
	public final SkinnedMeshPart rightLeg4;
	
	public SpiderMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.head = this.getOrLogException(this.parts, "head");
		this.middleStomach = this.getOrLogException(this.parts, "middleStomach");
		this.bottomStomach = this.getOrLogException(this.parts, "bottomStomach");
		this.leftLeg1 = this.getOrLogException(this.parts, "leftLeg1");
		this.leftLeg2 = this.getOrLogException(this.parts, "leftLeg2");
		this.leftLeg3 = this.getOrLogException(this.parts, "leftLeg3");
		this.leftLeg4 = this.getOrLogException(this.parts, "leftLeg4");
		this.rightLeg1 = this.getOrLogException(this.parts, "rightLeg1");
		this.rightLeg2 = this.getOrLogException(this.parts, "rightLeg2");
		this.rightLeg3 = this.getOrLogException(this.parts, "rightLeg3");
		this.rightLeg4 = this.getOrLogException(this.parts, "rightLeg4");
	}
}