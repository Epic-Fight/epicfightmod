package yesman.epicfight.api.event;

import yesman.epicfight.api.event.types.animation.*;
import yesman.epicfight.api.event.types.entity.*;
import yesman.epicfight.api.event.types.player.*;
import yesman.epicfight.api.event.types.registry.*;
import yesman.epicfight.api.ex_cap.modules.core.events.*;
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
public final class EpicFightEventHooks {
    public static final class Animation {
        public static final EventHook<AnimationBeginEvent> BEGIN = EventHook.createEventHook();
        public static final EventHook<AnimationEndEvent> END = EventHook.createEventHook();
        public static final EventHook<AttackPhaseEndEvent> ATTACK_PHASE_END = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<InitAnimatorEvent> INIT_ANIMATOR = EventHook.createEventHook();
        public static final EventHook<StartActionEvent> START_ACTION = EventHook.createEventHook();

        private Animation() {}
    }

    public static final class Entity {
        public static final CancelableEventHook<DealDamageEvent.Income> DELIVER_DAMAGE_INCOME = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final EventHook<DealDamageEvent.Pre> DELIVER_DAMAGE_PRE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<DealDamageEvent.Post> DELIVER_DAMAGE_POST = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<DodgeEvent> ON_DODGE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<FallEvent> ON_FALL = EventHook.createEventHook();
        public static final EventHook<HandleEntityDataEvent.Load> NBT_LOAD = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<HandleEntityDataEvent.Save> NBT_SAVE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<HitByProjectileEvent> HIT_BY_PROJECTILE = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final EventHook<KillEntityEvent> KILL_ENTITY = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<ModifyAttackSpeedEvent> MODIFY_ATTACK_SPEED = EventHook.createEventHook();
        public static final EventHook<ModifyBaseDamageEvent> MODIFY_ATTACK_DAMAGE = EventHook.createEventHook();
        public static final EventHook<EntityRemovedEvent> ON_REMOVED = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<StunnedEvent> ON_STUNNED = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<ApplyStunEvent> APPLY_STUN = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<TakeDamageEvent.Income> TAKE_DAMAGE_INCOME = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final EventHook<TakeDamageEvent.Pre> TAKE_DAMAGE_PRE = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final EventHook<TakeDamageEvent.Post> TAKE_DAMAGE_POST = EventHook.createSidedEventHook(LogicalSide.SERVER);

        private Entity() {}
    }

    public static final class Player {
        public static final EventHook<ChangeInnateSkillEvent> CHANGE_INNATE_SKILL = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<ComboAttackEvent> COMBO_ATTACK = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<ModifyComboCounter> MODIFY_COMBO_COUNTER = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<SetTargetEvent> SET_TARGET = CancelableEventHook.createSidedCancelableEventHook(LogicalSide.SERVER);
        public static final EventHook<SkillCancelEvent> CANCEL_SKILL = EventHook.createSidedEventHook(LogicalSide.SERVER);
        public static final CancelableEventHook<SkillCastEvent> CAST_SKILL = CancelableEventHook.createCancelableEventHook();
        public static final CancelableEventHook<SkillConsumeEvent> CONSUME_SKILL = CancelableEventHook.createCancelableEventHook();
        public static final CancelableEventHook<TickPlayerEpicFightModeEvent> TICK_EPICFIGHT_MODE = CancelableEventHook.createCancelableEventHook();
        public static final EventHook<TogglePlayerModeEvent> TOGGLE_MODE = CancelableEventHook.createCancelableEventHook();
        public static final EventHook<StartUsingItemEvent> USE_ITEM = EventHook.createEventHook();

        private Player() {}
    }

    public static final class Registry {
        public static final EventHook<EntityPatchRegistryEvent> ENTITY_PATCH = EventHook.createEventHook();
        public static final EventHook<SkillBuilderModificationEvent> MODIFY_SKILL_BUILDER = EventHook.createEventHook();
        public static final EventHook<RegisterMobSkillBookLootTableEvent> SKILLBOOK_LOOT_TABLE = EventHook.createEventHook();
        public static final EventHook<WeaponCapabilityPresetRegistryEvent> WEAPON_CAPABILITY_PRESET = EventHook.createEventHook();
        public static final EventHook<ExCapabilityBuilderPopulationEvent> EX_CAP_DATA_POPULATION = EventHook.createEventHook();
        public static final EventHook<ExCapBuilderCreationEvent> EX_CAP_BUILDER_CREATION = EventHook.createEventHook();
        public static final EventHook<ExCapDataRegistrationEvent> EX_CAP_DATA_CREATION = EventHook.createEventHook();
        public static final EventHook<ExCapMovesetRegistryEvent> EX_CAP_MOVESET_REGISTRY = EventHook.createEventHook();
        public static final EventHook<ConditionalRegistryEvent> EX_CAP_CONDITIONAL_REGISTRATION = EventHook.createEventHook();

        private Registry() {}
    }

    private EpicFightEventHooks() {}
}
