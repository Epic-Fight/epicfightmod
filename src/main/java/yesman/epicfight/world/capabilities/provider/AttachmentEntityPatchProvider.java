package yesman.epicfight.world.capabilities.provider;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

import javax.annotation.Nullable;

public final class AttachmentEntityPatchProvider {
	@Nullable
	private EntityPatch<?> entitypatch;
	
	public AttachmentEntityPatchProvider(IAttachmentHolder attachmentHolder) {
		if (!(attachmentHolder instanceof Entity entity)) {
			throw new IllegalArgumentException(attachmentHolder + " is not a subtype of Entity");
		}

        if (attachmentHolder instanceof EnderDragonPart) {
            return;
        }

		this.entitypatch = EpicFightCapabilities.ENTITY_PATCH_PROVIDER.getCapability(entity);
	}
	
	public EntityPatch<?> getCapability() {
		return this.entitypatch;
	}
}
