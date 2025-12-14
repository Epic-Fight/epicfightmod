package yesman.epicfight.world.entity.eventlistener;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

public abstract class DealDamageEvent<T extends LivingEvent> extends AbstractPlayerEvent<ServerPlayerPatch> {
	protected final LivingEntity target;
	protected final T forgeevent;
	private final EpicFightDamageSource damageSource;
	
	public DealDamageEvent(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, T forgeevent, boolean cancelable) {
		super(playerpatch, cancelable);
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
	
	public T getForgeEvent() {
		return this.forgeevent;
	}
	
	public static class Attack extends DealDamageEvent<LivingAttackEvent> {
		public Attack(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingAttackEvent forgeevent) {
			super(playerpatch, target, source, forgeevent, true);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getAmount();
		}
	}
	
	public static class Hurt extends DealDamageEvent<LivingHurtEvent> {
		public Hurt(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingHurtEvent forgeevent) {
			super(playerpatch, target, source, forgeevent, false);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getAmount();
		}
	}
	
	public static class Damage extends DealDamageEvent<LivingDamageEvent> {
		public Damage(ServerPlayerPatch playerpatch, LivingEntity target, EpicFightDamageSource source, LivingDamageEvent forgeevent) {
			super(playerpatch, target, source, forgeevent, false);
		}
		
		@Override
		public float getAttackDamage() {
			return this.forgeevent.getAmount();
		}
	}
}