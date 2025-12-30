package yesman.epicfight.api.client.event;

import yesman.epicfight.api.client.event.types.camera.*;
import yesman.epicfight.api.client.event.types.control.MappedMovementInputUpdateEvent;
import yesman.epicfight.api.client.event.types.entity.ModifyPlayerLivingMotionEvent;
import yesman.epicfight.api.client.event.types.entity.ProcessEntityPairingPacketEvent;
import yesman.epicfight.api.client.event.types.hud.TickTargetIndicatorEvent;
import yesman.epicfight.api.client.event.types.registry.RegisterAttributeIconEvent;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.api.client.event.types.registry.RegisterWeaponCategoryIconEvent;
import yesman.epicfight.api.client.event.types.render.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.event.types.render.PrepareModelEvent;
import yesman.epicfight.api.client.event.types.render.RenderEnderDragonEvent;
import yesman.epicfight.api.client.event.types.render.ValidatePlayerModelEvent;
import yesman.epicfight.api.event.*;
import yesman.epicfight.api.utils.side.LogicalSide;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// These are pre-defined hooks for all existing event types
///
/// To register an event listener that subscribes a specific event, call one of [EventHook#registerEvent],
/// [CancelableEventHook#registerCancelableEvent], or [CancelableEventHook#registerContextAwareEvent].
///
/// If you want to listen an event not globally, but per entity, call the exact same registering methods in
/// [EntityEventListener], which you can access by [LivingEntityPatch#getEventListener] Be aware that you
/// only can register events that inherit [LivingEntityPatchEvent] for per entity events.
///
/// For common event hooks for both client and server side, refer to [EpicFightEventHooks]
public interface EpicFightClientEventHooks {
    interface Camera {
        CancelableEventHook<BuildCameraTransform.Pre> BUILD_TRANSFORM_PRE = CancelableEventHook.createCancelableEventHook();
        EventHook<BuildCameraTransform.Post> BUILD_TRANSFORM_POST = EventHook.createEventHook();
        EventHook<ItemUsedInDecoupledCamera> ITEM_USED_WHEN_DECOUPLED = EventHook.createEventHook();
        EventHook<CoupleTPSCamera> COUPLE_CAMERA = EventHook.createEventHook();
        EventHook<LockOnEvent.Start> LOCK_ON_START = CancelableEventHook.createCancelableEventHook();
        EventHook<LockOnEvent.Tick> LOCK_ON_TICK = EventHook.createEventHook();
        EventHook<LockOnEvent.Release> LOCK_ON_RELEASED = CancelableEventHook.createCancelableEventHook();
        EventHook<ActivateTPSCamera> ACTIVATE_TPS_CAMERA = CancelableEventHook.createCancelableEventHook();
    }

    interface Control {
        EventHook<MappedMovementInputUpdateEvent> MAPPED_MOVEMENT_INPUT_UPDATE = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }

    interface Entity {
        EventHook<ProcessEntityPairingPacketEvent> HANDLE_ENTITY_PAIRING_PACKET = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.CLIENT);
        EventHook<ModifyPlayerLivingMotionEvent.BaseLayer> MODIFY_PLAYER_LIVING_MOTION_BASE = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<ModifyPlayerLivingMotionEvent.CompositeLayer> MODIFY_PLAYER_LIVING_MOTION_COMPOSITE = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }

    interface HUD {
        EventHook<TickTargetIndicatorEvent> TARGET_INDICATOR_TICK = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }

    interface Registry {
        EventHook<RegisterAttributeIconEvent> ATTRIBUTE_ICON = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<RegisterPatchedRenderersEvent.ModifyEntity> MODIFY_PATCHED_ENTITY = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<RegisterPatchedRenderersEvent.AddEntity> ADD_PATCHED_ENTITY = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<RegisterPatchedRenderersEvent.Item> PATCHED_ITEM = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<RegisterWeaponCategoryIconEvent> WEAPON_CATEGORY_ICON = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }

    interface Render {
        EventHook<AnimatedArmorTextureEvent> ANIMATED_ARMOR_TEXTURE = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<PrepareModelEvent> PREPARE_MODEL_TO_RENDER = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<RenderEnderDragonEvent> RENDER_ENDER_DRAGON = EventHook.createSidedEventHook(LogicalSide.CLIENT);
        EventHook<ValidatePlayerModelEvent> VALIDATE_PLAYER_MODEL_TO_RENDER = EventHook.createSidedEventHook(LogicalSide.CLIENT);
    }
}