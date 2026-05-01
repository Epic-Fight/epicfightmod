package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.network.server.SPSkillFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

/// This module classifies a skill to be holdable; this allows the skill to be held as long as the player likes with some logic on when the skill stops holding.
public interface HoldableSkill {
    /// Some beginning logic when the skill starts to get held.
    /// @param container Class: [SkillContainer] - The SkillContainer that holds the skill, used often to do stuff on the executor.
    default void startHolding(SkillContainer container) {}
    
    /// Called every tick, used common-sided. If needed for handling Client or Server, use an if-else statement with the condition (container.getExecutor.isLogicalClient();)
    /// @param container Class: [SkillContainer] - The SkillContainer that holds the skill, used often to do stuff on the executor.
    default void holdTick(SkillContainer container) {}
    
    /// A method that is called on the server-side to perform stuff on the player when they stop holding the key that is being held.
    /// @param container Class: [SkillContainer] - The SkillContainer that holds the skill, used often to do stuff on the executor, note this is server-sided.
    default void onStopHolding(SkillContainer container, SPSkillFeedback feedbackPacket) {}

    default void resetHolding(SkillContainer container) {}

    @ClientOnly
    default void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, CompoundTag arguments) {}
    
    /// Gives the normal skill object of this [HoldableSkill] object.
    /// @return Class: [Skill] - this object cast into a normal [Skill] class.
    default Skill asSkill() {
        return (Skill)this;
    }
    
    /// Retrieves the keybind of this skill.
    ///
    /// If the returned [KeyMapping] corresponds to an action defined in
    /// [EpicFightInputAction#keyMapping()],
    /// controller input will be supported as well. Otherwise, the skill will only
    /// support keyboard and mouse input. This is related to the workaround implemented in the internal
    /// [ControlEngine#mapKeyMappingToAction].
    ///
    /// In future updates, this method may be deprecated in favor of returning
    /// []EpicFightInputAction]
    /// (OR [yesman.epicfight.api.client.input.action.InputAction]) directly,
    /// eliminating the need for [ControlEngine#mapKeyMappingToAction] to support controllers.
    /// @see EpicFightInputAction
    /// @see ControlEngine#mapKeyMappingToAction
    @SuppressWarnings({"JavadocReference"})
    @ClientOnly
    KeyMapping getKeyMapping();
}
