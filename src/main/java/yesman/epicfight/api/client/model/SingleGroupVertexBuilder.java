package yesman.epicfight.api.client.model;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector2f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class SingleGroupVertexBuilder {
	private Vector3f position;
	private Vector3f normal;
	private Vector2f textureCoordinate;
	private Vector3f effectiveJointIDs;
	private Vector3f effectiveJointWeights;
	private int effectiveJointNumber;
	
	public SingleGroupVertexBuilder() {
		this.position = null;
		this.normal = null;
		this.textureCoordinate = null;
	}
	
	public SingleGroupVertexBuilder(SingleGroupVertexBuilder vertex) {
		this.position = vertex.position;
		this.effectiveJointIDs = vertex.effectiveJointIDs;
		this.effectiveJointWeights = vertex.effectiveJointWeights;
		this.effectiveJointNumber = vertex.effectiveJointNumber;
	}
	
	public SingleGroupVertexBuilder setPosition(Vector3f position) {
		this.position = position;
		return this;
	}
	
	public SingleGroupVertexBuilder setNormal(Vector3f vector) {
		this.normal = vector;
		return this;
	}
	
	public SingleGroupVertexBuilder setTextureCoordinate(Vector2f vector) {
		this.textureCoordinate = vector;
		return this;
	}
	
	public SingleGroupVertexBuilder setEffectiveJointIDs(Vector3f effectiveJointIDs) {
		this.effectiveJointIDs = effectiveJointIDs;
		return this;
	}
	
	public SingleGroupVertexBuilder setEffectiveJointWeights(Vector3f effectiveJointWeights) {
		this.effectiveJointWeights = effectiveJointWeights;
		return this;
	}
	
	public SingleGroupVertexBuilder setEffectiveJointNumber(int count) {
		this.effectiveJointNumber = count;
		return this;
	}
	
	public State compareTextureCoordinateAndNormal(Vector3f normal, Vector2f textureCoord) {
		if (this.textureCoordinate == null) {
			return State.EMPTY;
		} else if (this.textureCoordinate.equals(textureCoord) && this.normal.equals(normal)) {
			return State.EQUAL;
		} else {
			return State.DIFFERENT;
		}
	}
	
	public static SkinnedMesh loadVertexInformation(List<SingleGroupVertexBuilder> vertices, Map<MeshPartDefinition, IntList> indices) {
		FloatList positions = new FloatArrayList();
		FloatList normals = new FloatArrayList();
		FloatList texCoords = new FloatArrayList();
		IntList animationIndices = new IntArrayList();
		FloatList jointWeights = new FloatArrayList();
		IntList affectCountList = new IntArrayList();
		
		for (int i = 0; i < vertices.size(); i++) {
			SingleGroupVertexBuilder vertex = vertices.get(i);
			Vector3f position = vertex.position;
			Vector3f normal = vertex.normal;
			Vector2f texCoord = vertex.textureCoordinate;
			positions.add(position.x);
			positions.add(position.y);
			positions.add(position.z);
			normals.add(normal.x);
			normals.add(normal.y);
			normals.add(normal.z);
			texCoords.add(texCoord.x);
			texCoords.add(texCoord.y);
			
			Vector3f effectIDs = vertex.effectiveJointIDs;
			Vector3f weights = vertex.effectiveJointWeights;
			int count = Math.min(vertex.effectiveJointNumber, 3);
			affectCountList.add(count);
			
			for (int j = 0; j < count; j++) {
				switch (j) {
				case 0:
					animationIndices.add((int) effectIDs.x);
					jointWeights.add(weights.x);
					animationIndices.add(jointWeights.size() - 1);
					break;
				case 1:
					animationIndices.add((int) effectIDs.y);
					jointWeights.add(weights.y);
					animationIndices.add(jointWeights.size() - 1);
					break;
				case 2:
					animationIndices.add((int) effectIDs.z);
					jointWeights.add(weights.z);
					animationIndices.add(jointWeights.size() - 1);
					break;
				default:
				}
			}
		}
		
		Float[] positionList = positions.toArray(new Float[0]);
		Float[] normalList = normals.toArray(new Float[0]);
		Float[] texCoordList = texCoords.toArray(new Float[0]);
		Integer[] affectingJointIndices = animationIndices.toArray(new Integer[0]);
		Float[] jointWeightList = jointWeights.toArray(new Float[0]);
		Integer[] affectJointCounts = affectCountList.toArray(new Integer[0]);
		Map<String, Number[]> arrayMap = Maps.newHashMap();
		Map<MeshPartDefinition, List<VertexBuilder>> meshDefinitions = Maps.newHashMap();
		
		arrayMap.put("positions", positionList);
		arrayMap.put("normals", normalList);
		arrayMap.put("uvs", texCoordList);
		arrayMap.put("weights", jointWeightList);
		arrayMap.put("vcounts", affectJointCounts);
		arrayMap.put("vindices", affectingJointIndices);
		
		for (Map.Entry<MeshPartDefinition, IntList> e : indices.entrySet()) {
			meshDefinitions.put(e.getKey(), VertexBuilder.create(e.getValue().toIntArray()));
		}
		
		return new SkinnedMesh(arrayMap, meshDefinitions, null, null);
	}
	
	public enum State {
		EMPTY, EQUAL, DIFFERENT
	}
}