package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.network.server.SPSkillExecutionFeedback;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

/**
 * This module classifies a skill to be holdable; this allows the skill to be held as long as the player likes with some logic on when the skill stops holding.
 */
public interface HoldableSkill {
    /**
     * Some beginning logic when the skill starts to get held.
     * @param container Class: {@link SkillContainer} - The SkillContainer that holds the skill, used often to do stuff on the executor.
     */
    default void startHolding(SkillContainer container) {}
    
    /**
     * Called every tick, used common-sided. If needed for handling Client or Server, use an if-else statement with the condition (container.getExecutor.isLogicalClient();)
     * @param container Class: {@link SkillContainer} - The SkillContainer that holds the skill, used often to do stuff on the executor.
     */
    default void holdTick(SkillContainer container) {}
    
    /**
     * A method that is called on the server-side to perform stuff on the player when they stop holding the key that is being held.
     * @param container Class: {@link SkillContainer} - The SkillContainer that holds the skill, used often to do stuff on the executor, note this is server-sided.
     */
    default void onStopHolding(SkillContainer container, SPSkillExecutionFeedback feedbackPacket) {}

    default void resetHolding(SkillContainer container) {}

    @OnlyIn(Dist.CLIENT)
    default void gatherHoldArguments(SkillContainer container, ControlEngine controlEngine, FriendlyByteBuf buffer){}
    
    /**
     * Gives the normal skill object of this {@link HoldableSkill} object.
     * @return Class: {@link Skill} - this object cast into a normal {@link Skill} class.
     */
    default Skill asSkill() {
        return (Skill)this;
    }
    
    /**
     * Retrieves the keybind of this skill.
     * <p>
     * If the returned {@link KeyMapping} corresponds to an action defined in
     * {@link EpicFightInputAction#keyMapping()},
     * controller input will be supported as well. Otherwise, the skill will only
     * support keyboard and mouse input. This is related to the workaround implemented in the internal
     * {@link ControlEngine#mapKeyMappingToAction}.
     * <p>
     * In future updates, this method may be deprecated in favor of returning
     * {@link EpicFightInputAction}
     * (OR {@link yesman.epicfight.api.client.input.action.InputAction}) directly,
     * eliminating the need for {@link ControlEngine#mapKeyMappingToAction} to support controllers.
     * @see EpicFightInputAction
     * @see ControlEngine#mapKeyMappingToAction
     */
    @SuppressWarnings({"JavadocReference"})
    @OnlyIn(Dist.CLIENT)
    KeyMapping getKeyMapping();
}
