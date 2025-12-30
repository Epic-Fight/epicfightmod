package yesman.epicfight.api.event;

import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Events that are called during an entity's lifecycle
/// 
/// Target entities must hold [LivingEntityPatch] returned by [EpicFightCapabilities#getEntityPatch]
///
/// Developers can add or remove these events throguth [LivingEntityPatch#getEventListener()] in runtime
public abstract class LivingEntityPatchEvent extends Event {
    private final LivingEntityPatch<?> entityPatch;

    public LivingEntityPatchEvent(LivingEntityPatch<?> entityPatch) {
        this.entityPatch = entityPatch;
    }

    public final LivingEntityPatch<?> getEntityPatch() {
        return this.entityPatch;
    }
}