package yesman.epicfight.client.mesh;

import java.util.List;
import java.util.Map;

import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

public class CreeperMesh extends SkinnedMesh {
	public final SkinnedMeshPart head;
	public final SkinnedMeshPart torso;
	public final SkinnedMeshPart legRF;
	public final SkinnedMeshPart legLF;
	public final SkinnedMeshPart legRB;
	public final SkinnedMeshPart legLB;
	
	public CreeperMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
		super(arrayMap, parts, parent, properties);
		
		this.head = this.getOrLogException(this.parts, "head");
		this.torso = this.getOrLogException(this.parts, "torso");
		this.legRF = this.getOrLogException(this.parts, "legRF");
		this.legLF = this.getOrLogException(this.parts, "legLF");
		this.legRB = this.getOrLogException(this.parts, "legRB");
		this.legLB = this.getOrLogException(this.parts, "legLB");
	}
}