package yesman.epicfight.api.neoevent;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.Event;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public abstract class HandleEntityDataEvent extends Event {
	private EntityPatch<?> entitypatch;
	private CompoundTag compound;
	
	public HandleEntityDataEvent(EntityPatch<?> entitypatch, CompoundTag compound) {
		this.entitypatch = entitypatch;
		this.compound = compound;
	}
	
	public EntityPatch<?> getEntityPatch() {
		return this.entitypatch;
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
