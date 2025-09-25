package yesman.epicfight.registry.entries;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

public final class EpicFightAttachmentTypes {
	private EpicFightAttachmentTypes() {}
	
	public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EpicFightMod.MODID);
	
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<AttachmentEntityPatchProvider>> ENTITY_PATCH = REGISTRY.register(
            "entitypatch",
            () ->
            	AttachmentType
                    .builder(AttachmentEntityPatchProvider::new)
                    .build()
    );
}
