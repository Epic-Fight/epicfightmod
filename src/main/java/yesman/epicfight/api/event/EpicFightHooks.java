package yesman.epicfight.api.event;

public final class EpicFightHooks {
	
	/// we will eventually put all epic fight neoforge events here to decouple the event handling system originally
    /// conducted by mod-loaders (Forge, Neoforge, Fabric)
    /// There are a bunch of event definitions under {@link yesman.epicfight.api.neoevent} package. We
    /// plan to define each class as static fields of {@link yesman.epicfight.api.event.Event} in future API model.
    ///
    /// Example snippet
    /// yesman.epicfight.api.event.Event<BattleModeSustainableEvent> BATTLE_MODE_TICK = Event.createHook();
    /// yesman.epicfight.api.event.Event<BuilderModificationEvent> MODIFY_SKILL_BUILDER = Event.createHook();
    /// ...
    ///
    /// See also with {@link EpicFightClientEvents}
    ///
	private EpicFightHooks() {}
}
