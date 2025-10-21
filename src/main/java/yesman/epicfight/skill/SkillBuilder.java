package yesman.epicfight.skill;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import yesman.epicfight.api.neoevent.BuilderModificationEvent;
import yesman.epicfight.api.neoevent.playerpatch.PlayerPatchEvent;
import yesman.epicfight.main.EpicFightExtensions;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.skill.Skill.ActivateType;
import yesman.epicfight.skill.Skill.Resource;
import yesman.epicfight.skill.Skill.SkillEventSubscriber;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;

@SuppressWarnings("unchecked")
public class SkillBuilder<B extends SkillBuilder<?>> {
	protected final Function<B, ? extends Skill> constructor;
	protected ResourceLocation registryName;
	protected CreativeModeTab tab;
	protected SkillCategory category;
	protected ActivateType activateType = ActivateType.ONE_SHOT;
	protected Resource resource = Resource.NONE;
	protected Map<String, Map<Class<?>, SkillEventSubscriber>> clientEventListeners = new HashMap<> ();
	protected Map<String, Map<Class<?>, SkillEventSubscriber>> serverEventListeners = new HashMap<> ();
	
	public SkillBuilder(Function<B, ? extends Skill> constructor) {
		this.constructor = constructor;
	}
	
	public B setRegistryName(ResourceLocation registryName) {
		this.registryName = registryName;
		return (B)this;
	}
	
	/**
	 *  Leave the value as null if you want your skill's creative tab is decided by {@link EpicFightExtensions}
	 */
	public B setCreativeTab(CreativeModeTab tab) {
		this.tab = tab;
		return (B)this;
	}
	
	public B setCategory(SkillCategory category) {
		this.category = category;
		return (B)this;
	}
	
	public B setActivateType(ActivateType activateType) {
		this.activateType = activateType;
		return (B)this;
	}
	
	public B setResource(Resource resource) {
		this.resource = resource;
		return (B)this;
	}
	
	public <T extends Event> B addClientEventListener(String modid, Class<T> eventClass, int priority, BiConsumer<T, SkillContainer> eventSubscriber) {
		Map<Class<?>, SkillEventSubscriber> events = this.clientEventListeners.computeIfAbsent(modid, k -> new HashMap<> ());
		
		if (events.containsKey(eventClass)) {
			EpicFightMod.LOGGER.warn("Overwrote the existing client side skill event " + eventClass.getSimpleName() + " in " + this.registryName);
		}
		
		events.put(eventClass, new SkillEventSubscriber(priority, (BiConsumer<Event, SkillContainer>)eventSubscriber, (Method)null));
		
		return (B)this;
	}
	
	public <T extends Event> B addServerEventListener(String modid, Class<T> eventClass, int priority, BiConsumer<T, SkillContainer> eventSubscriber) {
		Map<Class<?>, SkillEventSubscriber> events = this.serverEventListeners.computeIfAbsent(modid, k -> new HashMap<> ());
		
		if (events.containsKey(eventClass)) {
			EpicFightMod.LOGGER.warn("Overwrote the existing server side skill event " + eventClass.getSimpleName() + " in " + this.registryName);
		}
		
		events.put(eventClass, new SkillEventSubscriber(priority, (BiConsumer<Event, SkillContainer>)eventSubscriber, (Method)null));
		
		return (B)this;
	}
	
	/**
	 * This is a comment that includes above two methods
	 * 
	 * @param modid
	 * The caller mod Id distinguishes event caller to avoid duplicated event invocation.
	 * For events that inherit {@link PlayerPatchEvent}, use "epicfight" as they're called by epic fight API
	 * For other neoforge events, you have to create a skill event hook by {@link PlayerSkills#fireSkillEvents} with your mod Id
	 * 
	 * @param eventClass
	 * The class of subscribing {@link net.neoforged.bus.api.Event}
	 * 
	 * @param priority
	 * A priority that determines the execution order of same-typed events from other skills (descending order)
	 * Default value would be -1
	 * 
	 * @param eventSubscriber
	 * Event task
	 */
	public <T extends Event> B addCommonEventListener(String modid, Class<T> eventClass, int priority, BiConsumer<T, SkillContainer> eventSubscriber) {
		this.addClientEventListener(modid, eventClass, priority, eventSubscriber);
		this.addServerEventListener(modid, eventClass, priority, eventSubscriber);
		return (B)this;
	}
	
	// Kube JS integration
	@ApiStatus.Internal
	public <T extends Event> B addClientEventListener(List<Pair<String, SkillEventSubscriber>> listeners) {
		//listeners.forEach(pair -> this.addClientEventListener(pair.getFirst(), pair.getSecond(), pair.getSecond().priority(), pair.getSecond().eventSubscriber()));
		return (B)this;
	}
	
	// Kube JS integration
	@ApiStatus.Internal
	public <T extends Event> B addServerEventListener(List<Pair<String, SkillEventSubscriber>> listeners) {
		
		return (B)this;
	}
	
	//private List<Pair<String, SkillEventSubscriber>> clientEventListeners = new ArrayList<> ();
    //private List<Pair<String, SkillEventSubscriber>> serverEventListeners = new ArrayList<> ();
	
	public <T extends Skill> T build(ResourceLocation key, Class<T> skillCls) {
		this.setRegistryName(key);
		
		BuilderModificationEvent builderModificationEvent = new BuilderModificationEvent(key, this);
		
		if (EpicFightSharedConstants.isPhysicalClient()) {
			findEventSubscriberMethods(skillCls, this.clientEventListeners, true);
		}
		
		findEventSubscriberMethods(skillCls, this.serverEventListeners, false);
		
		ModLoader.postEvent(builderModificationEvent);
		
		return (T)this.constructor.apply((B)this);
	}
	
	private static void findEventSubscriberMethods(Class<?> skillClass, Map<String, Map<Class<?>, SkillEventSubscriber>> map, boolean logicalClient) {
		try {
			for (Method m : skillClass.getMethods()) {
				SkillEvent eventAnnotation = m.getAnnotation(SkillEvent.class);
				
				if (eventAnnotation != null) {
					if (eventAnnotation.side() == SkillEvent.Side.CLIENT && (!logicalClient || !EpicFightSharedConstants.isPhysicalClient())) {
						continue;
					}
					
					if (eventAnnotation.side() == SkillEvent.Side.SERVER && logicalClient) {
						continue;
					}
					
					if (m.getParameterTypes().length != 2) {
						throw new IllegalArgumentException("Failed at loading skill events at " + skillClass.getSimpleName() + ": Skill subscriber method must have 2 paramters. (event, skillcontainer)");
					}
					
					if (!(net.neoforged.bus.api.Event.class.isAssignableFrom(m.getParameterTypes()[0]))) {
						throw new IllegalArgumentException("Failed at loading skill events at " + skillClass.getSimpleName() + ": " + m.getParameterTypes()[0] + " is not a subtype of net.neoforged.bus.api.Event");
					}
					
					if (!m.getDeclaringClass().equals(skillClass) && !eventAnnotation.extendable()) {
						continue;
					}
					
					Map<Class<?>, SkillEventSubscriber> byEventClass = map.computeIfAbsent(eventAnnotation.caller(), k -> new HashMap<> ());
					Class<?> eventClass = m.getParameterTypes()[0];
					
					if (byEventClass.containsKey(eventClass)) {
						// when the old event is overriden by new event
						if (eventAnnotation.override() && !byEventClass.get(eventClass).reflectionMethod().getDeclaringClass().equals(skillClass)) {
							continue;
						}
						
						// when the old event overrides new event
						if (byEventClass.get(eventClass).reflectionMethod().getAnnotation(SkillEvent.class).override() && !m.getDeclaringClass().equals(skillClass)) {
							continue;
						}
						
						throw new IllegalStateException("Failed at loading skill events at " + skillClass.getSimpleName() + ": Duplicated event " + m.getName() + "(" + m.getParameterTypes()[0] + ")");
					}
					
					byEventClass.put(m.getParameterTypes()[0], new SkillEventSubscriber(eventAnnotation.priority(), (event, skillContainer) -> {
						try {
							m.invoke(skillContainer.getSkill(), event, skillContainer);
						} catch (Exception e) {
							EpicFightMod.logAndStacktraceIfDevSide(Logger::error, "Failed to execute skill event: " + skillContainer.getSkill() +" "+ event.getClass().getSimpleName(), RuntimeException::new);
						}
					}, m));
				}
			}
		} catch (Exception e) {
			EpicFightMod.LOGGER.error("Error while reading skill event subscribers of " + skillClass.getSimpleName());
			throw e;
		}
	}
}
