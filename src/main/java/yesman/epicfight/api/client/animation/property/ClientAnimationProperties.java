package yesman.epicfight.api.client.animation.property;

import java.util.List;

import yesman.epicfight.api.animation.property.AnimationProperty.StaticAnimationProperty;
import yesman.epicfight.api.animation.types.DirectStaticAnimation;
import yesman.epicfight.api.client.animation.AnimationSubFileReader;
import yesman.epicfight.api.client.animation.Layer;

public class ClientAnimationProperties {
	/**
	 * Layer type. (BASE: Living, attack animations, COMPOSITE: Aiming, weapon holding, digging animation)
	 */
	public static final StaticAnimationProperty<Layer.LayerType> LAYER_TYPE = new StaticAnimationProperty<Layer.LayerType> ();
	
	/**
	 * Priority of composite layer.
	 */
	public static final StaticAnimationProperty<Layer.Priority> PRIORITY = new StaticAnimationProperty<Layer.Priority> ();
	
	/**
	 * Joint mask for composite layer.
	 */
	public static final StaticAnimationProperty<JointMaskEntry> JOINT_MASK = new StaticAnimationProperty<JointMaskEntry> ();
	
	/**
	 * Trail particle information
	 */
	public static final StaticAnimationProperty<List<TrailInfo>> TRAIL_EFFECT = new StaticAnimationProperty<List<TrailInfo>> ();
	
	/**
	 * An animation clip being played in first person.
	 */
	public static final StaticAnimationProperty<DirectStaticAnimation> POV_ANIMATION = new StaticAnimationProperty<DirectStaticAnimation> ();
	
	/**
	 * An animation clip being played in first person.
	 */
	public static final StaticAnimationProperty<AnimationSubFileReader.PovSettings> POV_SETTINGS = new StaticAnimationProperty<AnimationSubFileReader.PovSettings> ();
	
	/**
	 * Multilayer for living animations (e.g. Greatsword holding animation should be played simultaneously with jumping animation) 
	 */
	public static final StaticAnimationProperty<DirectStaticAnimation> MULTILAYER_ANIMATION = new StaticAnimationProperty<DirectStaticAnimation> ();
}