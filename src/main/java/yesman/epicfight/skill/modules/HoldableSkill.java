package yesman.epicfight.skill.modules;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

/**
 * This module classifies a skill to be holdable; this allows the skill to be held as long as the player likes with some logic on when the skill stops holding.
 */
public interface HoldableSkill
{
    /**
     * Called every tick, used common-sided. If needed for handling Client or Server, use an if-else statement with the condition (container.getExecutor.isLogicalClient();)
     * @param container Class: {@link SkillContainer} - The SkillContainer that holds the skill, used often to do stuff on the executor.
     */
    void holdTick(SkillContainer container);

    /**
     * A method that is called on the server-side to perform stuff on the player when they stop holding the key that is being held.
     * @param container Class: {@link SkillContainer} - The SkillContainer that holds the skill, used often to do stuff on the executor, note this is server-sided.
     */
    void onStopHolding(SkillContainer container, FriendlyByteBuf packet);

    /**
     * Gives the normal skill object of this {@link HoldableSkill} object.
     * @return Class: {@link Skill} - this object cast into a normal {@link Skill} class.
     */
    default Skill asSkill() {
        return (Skill)this;
    }

    /**
     * Retrieves the keybind of this skill.
     * @return Class: {@link KeyMapping} the key mapping that the skill is mapped to.
     */
    @OnlyIn(Dist.CLIENT)
    KeyMapping getKeyMapping();
}
