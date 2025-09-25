package yesman.epicfight.world.capabilities.provider;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

public final class AttachmentEntityPatchProvider {
	@Nullable
	private EntityPatch<?> entitypatch;
	
	public AttachmentEntityPatchProvider(IAttachmentHolder attachmentHolder) {
		if (!(attachmentHolder instanceof Entity entity)) {
			throw new IllegalArgumentException(attachmentHolder + " is not a subtype of Entity");
		}
		
		this.entitypatch = EpicFightCapabilities.ENTITY_PATCH_PROVIDER.getCapability(entity);
	}
	
	public EntityPatch<?> getCapability() {
		return this.entitypatch;
	}
}
