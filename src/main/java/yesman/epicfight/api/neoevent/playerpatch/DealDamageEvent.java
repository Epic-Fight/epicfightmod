package yesman.epicfight.api.neoevent.playerpatch;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public abstract class DealDamageEvent<T extends LivingEvent> extends PlayerPatchEvent<ServerPlayerPatch> {
	protected final LivingEntity target;
	protected final T forgeevent;
	private final EpicFightDamageSource damageSource;
	
	public DealDamageEvent(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, T forgeevent) {
		super(playerpatch);
		this.target = target;
		this.damageSource = source;
		this.forgeevent = forgeevent;
	}
	
	public LivingEntity getTarget() {
		return this.target;
	}
	
	/**
	 * Modifying the original event's damage amount will have no effect on final damage calculation. Instead, use ValueModifier in EpicFightDamageSource
	 */
	public EpicFightDamageSource getDamageSource() {
		return this.damageSource;
	}
	
	public abstract float getAttackDamage();
	
	public T getNeoForgeEvent() {
		return this.forgeevent;
	}
	
	public static class Income extends DealDamageEvent<LivingIncomingDamageEvent> implements ICancellableEvent {
		public Income(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingIncomingDamageEvent forgeevent) {
			super(playerpatch, target, source, forgeevent);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getAmount();
		}
	}
	
	public static class Pre extends DealDamageEvent<LivingDamageEvent.Pre> {
		public Pre(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingDamageEvent.Pre forgeevent) {
			super(playerpatch, target, source, forgeevent);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getNewDamage();
		}
	}
	
	public static class Post extends DealDamageEvent<LivingDamageEvent.Post> {
		public Post(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingDamageEvent.Post forgeevent) {
			super(playerpatch, target, source, forgeevent);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getNewDamage();
		}
	}
}
