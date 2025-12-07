package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class VexMesh extends SkinnedMesh {
	public final SkinnedMeshPart torso;
	public final SkinnedMeshPart head;
	public final SkinnedMeshPart tail;
	public final SkinnedMeshPart leftArm;
	public final SkinnedMeshPart rightArm;
	public final SkinnedMeshPart leftWing;
	public final SkinnedMeshPart rightWing;
	
	public VexMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.torso = this.getOrLogException(this.parts, "torso");
		this.head = this.getOrLogException(this.parts, "head");
		this.tail = this.getOrLogException(this.parts, "tail");
		this.leftArm = this.getOrLogException(this.parts, "leftArm");
		this.rightArm = this.getOrLogException(this.parts, "rightArm");
		this.leftWing = this.getOrLogException(this.parts, "leftWing");
		this.rightWing = this.getOrLogException(this.parts, "rightWing");
	}
}