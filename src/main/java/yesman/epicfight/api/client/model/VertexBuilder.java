package yesman.epicfight.api.client.model;

import java.util.List;

import com.google.common.collect.Lists;



// Vertex Indices
public class VertexBuilder {
	public static List<VertexBuilder> create(int[] drawingIndices) {
		List<VertexBuilder> vertexIndicators = Lists.newArrayList();
		
		for (int i = 0; i < drawingIndices.length / 3; i++) {
			int k = i * 3;
			int position = drawingIndices[k];
			int uv = drawingIndices[k + 1];
			int normal = drawingIndices[k + 2];
			VertexBuilder vi = new VertexBuilder(position, uv, normal);
			vertexIndicators.add(vi);
		}
		
		return vertexIndicators;
	}
	
	public final int position;
	public final int uv;
	public final int normal;
	
	public VertexBuilder(int position, int uv, int normal) {
		this.position = position;
		this.uv = uv;
		this.normal = normal;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof VertexBuilder vb) {
			return this.position == vb.position && this.uv == vb.uv && this.normal == vb.normal;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        result = prime * result + this.position;
        result = prime * result + this.uv;
        result = prime * result + this.normal;
        
        return result;
    }
}