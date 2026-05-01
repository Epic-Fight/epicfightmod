package yesman.epicfight.api.event.types.entity;

import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

/// TODO: Change to better name, HandlePermanentEntityDataEvent
public abstract class HandleEntityDataEvent extends Event {
	private EntityPatch<?> entityPatch;
	private CompoundTag compound;
	
	public HandleEntityDataEvent(EntityPatch<?> entityPatch, CompoundTag compound) {
		this.entityPatch = entityPatch;
		this.compound = compound;
	}
	
	public EntityPatch<?> getEntityPatch() {
		return this.entityPatch;
	}
	
	public CompoundTag getCompound() {
		return this.compound;
	}
	
	public static class Save extends HandleEntityDataEvent {
		public Save(EntityPatch<?> entitypatch, CompoundTag compound) {
			super(entitypatch, compound);
		}
	}
	
	public static class Load extends HandleEntityDataEvent {
		public Load(EntityPatch<?> entitypatch, CompoundTag compound) {
			super(entitypatch, compound);
		}
	}
}
