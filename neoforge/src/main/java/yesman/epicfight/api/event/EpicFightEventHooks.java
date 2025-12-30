package yesman.epicfight.api.event;

import yesman.epicfight.api.event.types.animation.*;
import yesman.epicfight.api.event.types.entity.*;
import yesman.epicfight.api.event.types.player.*;
import yesman.epicfight.api.event.types.registry.EntityPatchRegistryEvent;
import yesman.epicfight.api.event.types.registry.RegisterMobSkillBookLootTableEvent;
import yesman.epicfight.api.event.types.registry.SkillBuilderModificationEvent;
import yesman.epicfight.api.event.types.registry.WeaponCapabilityPresetRegistryEvent;
import yesman.epicfight.api.utils.side.LogicalSide;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// These are pre-defined hooks for all existing event types
///
/// To register an event listener that subscribes a specific event, call one of [EventHook#registerEvent],
/// [CancelableEventHook#registerCancelableEvent], or [CancelableEventHook#registerContextAwareEvent].
///
/// If you want to listen an event not globally, but per entity, call the exact same registering methods in
/// [EntityEventListener], which you can access by [LivingEntityPatch#getEventListener]. Be aware that you
/// only can register events that inherit [LivingEntityPatchEvent] for per entity events.
public interface EpicFightEventHooks {
    interface Animation {
        EventHook<AnimationBeginEvent> BEGIN = EventHook.createEventHook();
        EventHook<AnimationEndEvent> END = EventHook.createEventHook();
        EventHook<AttackPhaseEndEvent> ATTACK_PHASE_END = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<InitAnimatorEvent> INIT_ANIMATOR = EventHook.createEventHook();
        EventHook<StartActionEvent> START_ACTION = EventHook.createEventHook();
    }

    interface Entity {
        CancelableEventHook<DealDamageEvent.Income> DELIEVER_DAMAGE_INCOME = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        EventHook<DealDamageEvent.Pre> DELIEVER_DAMAGE_PRE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<DealDamageEvent.Post> DELIEVER_DAMAGE_POST = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<DodgeEvent> ON_DODGE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<FallEvent> ON_FALL = EventHook.createEventHook();
        EventHook<HandleEntityDataEvent.Load> NBT_LOAD = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<HandleEntityDataEvent.Save> NBT_SAVE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        CancelableEventHook<HitByProjectileEvent> HIT_BY_PROJECTILE = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        EventHook<KillEntityEvent> KILL_ENTITY = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<ModifyAttackSpeedEvent> MODIFY_ATTACK_SPEED = EventHook.createEventHook();
        EventHook<ModifyBaseDamageEvent> MODIFY_ATTACK_DAMAGE = EventHook.createEventHook();
        EventHook<EntityRemovedEvent> ON_REMOVED = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<StunnedEvent> ON_STUNNED = EventHook.createSidedEventHook(LogicalSide.SERVER);
        CancelableEventHook<TakeDamageEvent.Income> TAKE_DAMAGE_INCOME = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        EventHook<TakeDamageEvent.Pre> TAKE_DAMAGE_PRE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        EventHook<TakeDamageEvent.Post> TAKE_DAMAGE_POST = EventHook.createSidedEventHook(LogicalSide.SERVER);
    }

    interface Player {
        EventHook<ChangeInnateSkillEvent> CHANGE_INNATE_SKILL = EventHook.createSidedEventHook(LogicalSide.SERVER);
        CancelableEventHook<ComboAttackEvent> COMBO_ATTACK = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        CancelableEventHook<ModifyComboCounter> MODIFY_COMBO_COUNTER = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        CancelableEventHook<SetTargetEvent> SET_TARGET = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        EventHook<SkillCancelEvent> CANCEL_SKILL = EventHook.createSidedEventHook(LogicalSide.SERVER);
        CancelableEventHook<SkillCastEvent> CAST_SKILL = CancelableEventHook.createCancelableEventHook();
        CancelableEventHook<SkillConsumeEvent> CONSUME_SKILL = CancelableEventHook.createCancelableEventHook();
        CancelableEventHook<TickPlayerEpicFightModeEvent> TICK_EPICFIGHT_MODE = CancelableEventHook.createCancelableEventHook();
        EventHook<TogglePlayerModeEvent> TOGGLE_MODE = CancelableEventHook.createCancelableEventHook();
        EventHook<StartUsingItemEvent> USE_ITEM = EventHook.createEventHook();
    }

    interface Registry {
        EventHook<EntityPatchRegistryEvent> ENTITY_PATCH = EventHook.createEventHook();
        EventHook<SkillBuilderModificationEvent> MODIFY_SKILL_BUILDER = EventHook.createEventHook();
        EventHook<RegisterMobSkillBookLootTableEvent> SKILLBOOK_LOOT_TABLE = EventHook.createEventHook();
        EventHook<WeaponCapabilityPresetRegistryEvent> WEAPON_CAPABILITY_PRESET = EventHook.createEventHook();
    }
}
