package yesman.epicfight.api.animation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.Lists;

import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public class Joint {
	public static final Joint EMPTY = new Joint("empty", -1, new OpenMatrix4f());
	
	private final List<Joint> subJoints = Lists.newArrayList();
	private final int jointId;
	private final String jointName;
	private final OpenMatrix4f localTransform;
	private final OpenMatrix4f toOrigin = new OpenMatrix4f();
	
	public Joint(String name, int jointId, OpenMatrix4f localTransform) {
		this.jointId = jointId;
		this.jointName = name;
		this.localTransform = localTransform.unmodifiable();
	}

	public void addSubJoints(Joint... joints) {
		for (Joint joint : joints) {
			if (!this.subJoints.contains(joint)) {
				this.subJoints.add(joint);
			}
		}
	}
	
	public void removeSubJoints(Joint... joints) {
		for (Joint joint : joints) {
			this.subJoints.remove(joint);
		}
	}
	
	public List<Joint> getAllJoints() {
		List<Joint> list = Lists.newArrayList();
		this.getSubJoints(list);
		
		return list;
	}
	
	public void iterSubJoints(Consumer<Joint> iterTask) {
		iterTask.accept(this);
		
		for (Joint joint : this.subJoints) {
			joint.iterSubJoints(iterTask);
		}
	}
	
	private void getSubJoints(List<Joint> list) {
		list.add(this);
		
		for (Joint joint : this.subJoints) {
			joint.getSubJoints(list);
		}
	}
	
	public void initOriginTransform(OpenMatrix4f parentTransform) {
		OpenMatrix4f modelTransform = OpenMatrix4f.mul(parentTransform, this.localTransform, null);
		OpenMatrix4f.invert(modelTransform, this.toOrigin);
		
		for (Joint joint : this.subJoints) {
			joint.initOriginTransform(modelTransform);
		}
	}
	
	public OpenMatrix4f getLocalTransform() {
		return this.localTransform;
	}
	
	public OpenMatrix4f getToOrigin() {
		return this.toOrigin;
	}
	
	public List<Joint> getSubJoints() {
		return this.subJoints;
	}
	
	// Null if index out of range
	@Nullable
	public Joint getSubJoint(int index) {
		if (index < 0 || this.subJoints.size() <= index) {
			return null;
		}
		
		return this.subJoints.get(index);
	}
	
	public String getName() {
		return this.jointName;
	}
	
	public int getId() {
		return this.jointId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Joint joint) {
			return this.jointName.equals(joint.jointName) && this.jointId == joint.jointId;
		} else {
			return super.equals(o);
		}
	}
	
	@Override
	public int hashCode() {
		return this.jointName.hashCode() ^ this.jointId;
	}
	
	/**
	 * Use the method that memorize path search results. {@link Armature#searchPathIndex(Joint, String)}
	 * 
	 * @param builder
	 * @param jointName
	 * @return
	 */
	@ApiStatus.Internal
	public HierarchicalJointAccessor.Builder searchPath(HierarchicalJointAccessor.Builder builder, String jointName) {
		if (jointName.equals(this.getName())) {
			return builder;
		} else {
			int i = 0;
			
			for (Joint subJoint : this.subJoints) {
				HierarchicalJointAccessor.Builder nextBuilder = subJoint.searchPath(builder.append(i), jointName);
				i++;
				
				if (nextBuilder != null) {
					return nextBuilder;
				}
			}
			
			return null;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nid: " + this.jointId);
		sb.append("\nidentifier: " + this.jointName);
		sb.append("\nlocal transform: " + this.localTransform);
		sb.append("\nto origin: " + this.toOrigin);
		sb.append("\nchildren: [");
		
		int idx = 0;
		
		for (Joint joint : this.subJoints) {
			idx++;
			sb.append(joint.jointName);
			
			if (idx != this.subJoints.size()) {
				sb.append(", ");
			}
		}
		
		sb.append("]\n");
		
		return sb.toString();
	}
	
	public String printIncludingChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.toString());
		
		for (Joint joint : this.subJoints) {
			sb.append(joint.printIncludingChildren());
		}
		
		return sb.toString();
	}
	
	public static class HierarchicalJointAccessor {
		private Queue<Integer> indicesToTerminal;
		private final String signature;
		
		private HierarchicalJointAccessor(Builder builder) {
			this.indicesToTerminal = builder.indicesToTerminal;
			this.signature = builder.signature;
		}
		
		public AccessTicket createAccessTicket(Joint rootJoint) {
			return new AccessTicket(this.indicesToTerminal, rootJoint);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof HierarchicalJointAccessor accessor) {
				this.signature.equals(accessor.signature);
			}
			
			return super.equals(o);
		}
		
		@Override
		public int hashCode() {
			return this.signature.hashCode();
		}
		
		public static Builder builder() {
			return new Builder(new LinkedList<> (), "");
		}
		
		public static class Builder {
			private Queue<Integer> indicesToTerminal;
			private String signature;
			
			private Builder(Queue<Integer> indicesToTerminal, String signature) {
				this.indicesToTerminal = indicesToTerminal;
				this.signature = signature;
			}
			
			public Builder append(int index) {
				String signatureNext;
				
				if (this.indicesToTerminal.isEmpty()) {
					signatureNext = this.signature + String.valueOf(index);
				} else {
					signatureNext = this.signature + "-" + String.valueOf(index);
				}
				
				Queue<Integer> nextQueue = new LinkedList<> (this.indicesToTerminal);
				nextQueue.add(index);
				
				return new Builder(nextQueue, signatureNext);
			}
			
			public HierarchicalJointAccessor build() {
				return new HierarchicalJointAccessor(this);
			}
		}
	}
	
	public static class AccessTicket implements Iterator<Joint> {
		Queue<Integer> accecssStack;
		Joint joint;
		
		private AccessTicket(Queue<Integer> indicesToTerminal, Joint rootJoint) {
			this.accecssStack = new LinkedList<> (indicesToTerminal);
			this.joint = rootJoint;
		}
		
		public boolean hasNext() {
			return !this.accecssStack.isEmpty();
		}
		
		public Joint next() {
			if (this.hasNext()) {
				int nextIndex = this.accecssStack.poll();
				this.joint = this.joint.subJoints.get(nextIndex);
			} else {
				throw new NoSuchElementException();
			}
			
			return this.joint;
		}
	}
}
