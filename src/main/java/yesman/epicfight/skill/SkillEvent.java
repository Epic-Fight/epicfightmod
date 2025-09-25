package yesman.epicfight.skill;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

/**
 * Define this annotation to forge event subscribing methods in {@link yesman.epicfight.skill.Skill.class}
 * Call {@link yesman.epicfight.world.capabilities.skill.PlayerSkills#fireForgeEvent} in Forge events
 * 
 * To avoid mod conflicts, specify the caller of the event as your mod id, and fire tutorial events with matching mod id
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface SkillEvent {
	
	/**
	 * Caller of this event
	 * 
	 * For {@link PlayerPatchEvent.class}, use "epicfight"
	 * Other events that Epic Fight API does not call, specify your mod Id to avoid duplicate calls
	 * 
	 * @return
	 */
	String caller() default EpicFightMod.MODID;
	
	/**
	 * Determines the logical side of the event
	 */
	Side side();
	
	/**
	 * Priority of the event, the higher value has the higher priority
	 */
	int priority() default -1;
	
	/**
	 * Prohibit subscribing the skill event in sub-classes
	 */
	boolean extendable() default true;
	
	/**
	 * Specify that the method overrides an event subscriber from the super class
	 */
	boolean override() default false;
	
	public enum Side {
		CLIENT(entitypatch -> entitypatch.isLogicalClient()), SERVER(entitypatch -> !entitypatch.isLogicalClient()), BOTH(entitypatch -> true);
		
		public final Predicate<PlayerPatch<?>> predicate;
		
		Side(Predicate<PlayerPatch<?>> predicate) {
			this.predicate = predicate;
		}
	}
}