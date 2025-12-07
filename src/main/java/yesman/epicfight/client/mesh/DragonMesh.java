package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class DragonMesh extends SkinnedMesh {
	public final SkinnedMeshPart head;
	public final SkinnedMeshPart neck;
	public final SkinnedMeshPart torso;
	public final SkinnedMeshPart leftLegFront;
	public final SkinnedMeshPart rightLegFront;
	public final SkinnedMeshPart leftLegBack;
	public final SkinnedMeshPart rightLegBack;
	public final SkinnedMeshPart leftWing;
	public final SkinnedMeshPart rightWing;
	public final SkinnedMeshPart tail;
	
	public DragonMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.head = this.getOrLogException(this.parts, "head");
		this.neck = this.getOrLogException(this.parts, "neck");
		this.torso = this.getOrLogException(this.parts, "torso");
		this.leftLegFront = this.getOrLogException(this.parts, "leftLegFront");
		this.rightLegFront = this.getOrLogException(this.parts, "rightLegFront");
		this.leftLegBack = this.getOrLogException(this.parts, "leftLegBack");
		this.rightLegBack = this.getOrLogException(this.parts, "rightLegBack");
		this.leftWing = this.getOrLogException(this.parts, "leftWing");
		this.rightWing = this.getOrLogException(this.parts, "rightWing");
		this.tail = this.getOrLogException(this.parts, "tail");
	}
}