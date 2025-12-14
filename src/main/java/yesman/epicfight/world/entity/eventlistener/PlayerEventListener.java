package yesman.epicfight.world.entity.eventlistener;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraftforge.fml.LogicalSide;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class PlayerEventListener {
	private final Map<EventType<? extends DetachablePlayerEvent<?>>, TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>>> events;
	private final PlayerPatch<?> playerpatch;
	
	public PlayerEventListener(PlayerPatch<?> playerpatch) {
		this.playerpatch = playerpatch;
		this.events = Maps.newHashMap();
	}
	
	public <T extends DetachablePlayerEvent<?>> void addEventListener(EventType<T> eventType, UUID uuid, Consumer<T> function) {
		this.addEventListener(eventType, uuid, function, -1);
	}
	
	public <T extends DetachablePlayerEvent<?>> void addEventListener(EventType<T> eventType, UUID uuid, Consumer<T> function, int priority) {
		if (eventType.shouldActive(this.playerpatch.isLogicalClient())) {
			if (!this.events.containsKey(eventType)) {
				this.events.put(eventType, TreeMultimap.create());
			}
			
			priority = Math.max(priority, -1);
			this.removeListener(eventType, uuid, priority);
			TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = this.events.get(eventType);
			map.put(priority, EventTrigger.makeEvent(uuid, function, priority));
		}
	}
	
	public <T extends DetachablePlayerEvent<?>> void removeListener(EventType<T> eventType, UUID uuid) {
		this.removeListener(eventType, uuid, -1);
	}
	
	/**
	 * 
	 * @param <T>
	 * @param eventType
	 * @param uuid
	 * @param priority -1: always occurs, others: not fired if higher priority event canceled.
	 */
	public <T extends DetachablePlayerEvent<?>> void removeListener(EventType<T> eventType, UUID uuid, int priority) {
		Multimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = this.events.get(eventType);
		
		if (map != null) {
			priority = Math.max(priority, -1);
			map.get(priority).removeIf((trigger) -> trigger.is(uuid));
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends DetachablePlayerEvent<?>> boolean triggerEvents(EventType<T> eventType, T event) {
		boolean cancel = false;
		TreeMultimap<Integer, EventTrigger<? extends DetachablePlayerEvent<?>>> map = this.events.get(eventType);
		
		if (map != null) {
			for (int i : map.keySet().descendingSet()) {
				if (!cancel || i == -1) {
					for (EventTrigger<?> eventTrigger : map.get(i)) {
						if (eventType.shouldActive(this.playerpatch.isLogicalClient())) {
							EventTrigger<T> castedTrigger = ((EventTrigger<T>)eventTrigger);
							castedTrigger.trigger(event);
							cancel |= event.isCanceled();
						}
					}
				}
			}
		}
		
		return cancel;
	}
	
	public static class EventType<T extends DetachablePlayerEvent<?>> {
		public static final EventType<ActionEvent<LocalPlayerPatch>> ACTION_EVENT_CLIENT = new EventType<>(null);
		public static final EventType<ActionEvent<ServerPlayerPatch>> ACTION_EVENT_SERVER = new EventType<>(null);
		public static final EventType<ModifyAttackSpeedEvent> MODIFY_ATTACK_SPEED_EVENT = new EventType<>(null);
		public static final EventType<ModifyBaseDamageEvent<PlayerPatch<?>>> MODIFY_DAMAGE_EVENT = new EventType<>(null);
		public static final EventType<DealDamageEvent.Attack> DEAL_DAMAGE_EVENT_ATTACK = new EventType<>(LogicalSide.SERVER);
		public static final EventType<DealDamageEvent.Hurt> DEAL_DAMAGE_EVENT_HURT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<DealDamageEvent.Damage> DEAL_DAMAGE_EVENT_DAMAGE = new EventType<>(LogicalSide.SERVER);
		public static final EventType<TakeDamageEvent.Attack> TAKE_DAMAGE_EVENT_ATTACK = new EventType<>(LogicalSide.SERVER);
		public static final EventType<TakeDamageEvent.Hurt> TAKE_DAMAGE_EVENT_HURT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<TakeDamageEvent.Damage> TAKE_DAMAGE_EVENT_DAMAGE = new EventType<>(LogicalSide.SERVER);
		public static final EventType<AnimationBeginEvent> ANIMATION_BEGIN_EVENT = new EventType<>(null);
		public static final EventType<AnimationEndEvent> ANIMATION_END_EVENT = new EventType<>(null);
		public static final EventType<AttackEndEvent> ATTACK_ANIMATION_END_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<AttackPhaseEndEvent> ATTACK_PHASE_END_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<BasicAttackEvent> BASIC_ATTACK_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<MovementInputEvent> MOVEMENT_INPUT_EVENT = new EventType<>(LogicalSide.CLIENT);
		public static final EventType<RightClickItemEvent<LocalPlayerPatch>> CLIENT_ITEM_USE_EVENT = new EventType<>(LogicalSide.CLIENT);
		public static final EventType<RightClickItemEvent<ServerPlayerPatch>> SERVER_ITEM_USE_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<ItemUseEndEvent> SERVER_ITEM_STOP_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<ProjectileHitEvent> PROJECTILE_HIT_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<SkillCastEvent> SKILL_CAST_EVENT = new EventType<>(null);
		public static final EventType<SkillCancelEvent> SKILL_CANCEL_EVENT = new EventType<>(null);
		public static final EventType<SkillConsumeEvent> SKILL_CONSUME_EVENT = new EventType<>(null);
		public static final EventType<StaminaConsumeEvent> STAMINA_CONSUME_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<ComboCounterHandleEvent> COMBO_COUNTER_HANDLE_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<TargetIndicatorCheckEvent> TARGET_INDICATOR_ALERT_CHECK_EVENT = new EventType<>(LogicalSide.CLIENT);
		public static final EventType<FallEvent> FALL_EVENT = new EventType<>(null);
		public static final EventType<SetTargetEvent> SET_TARGET_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<DodgeSuccessEvent> DODGE_SUCCESS_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<PlayerKilledEvent> PLAYER_KILLED_EVENT = new EventType<>(LogicalSide.SERVER);
		public static final EventType<UpdatePlayerMotionEvent.BaseLayer> UPDATE_BASE_LIVING_MOTION_EVENT = new EventType<>(LogicalSide.CLIENT);
		public static final EventType<UpdatePlayerMotionEvent.CompositeLayer> UPDATE_COMPOSITE_LIVING_MOTION_EVENT = new EventType<>(LogicalSide.CLIENT);
		
		LogicalSide side;

		/**
		 * Changes - Made EventType's Constructor Public to allow addons to add their own custom events.
		 * @param side whether its client or server-sided.
		 */
		public EventType(LogicalSide side) {
			this.side = side;
		}
		
		public boolean shouldActive(boolean isRemote) {
			return this.side == null || this.side.isClient() == isRemote;
		}
	}
}