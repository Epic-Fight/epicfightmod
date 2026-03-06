package yesman.epicfight.world.capabilities.item.builders.providers;

import com.google.common.collect.Lists;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.Style;

import java.util.List;

/**
 * This class is meant to be as an extendbale
 */
public class CoreWeaponCapabilityProvider
{
    private final List<ProviderConditional> conditionals;
    public CoreWeaponCapabilityProvider()
    {
        conditionals = Lists.newArrayList();
    }

    public void addConditional(ProviderConditional conditional)
    {
        this.conditionals.add(conditional);
    }

    private void sortByPriority()
    {
        if (conditionals.size() <= 1) return;
        for (int i = 0; i < conditionals.size() - 1; i++)
        {
            ProviderConditional conditional = conditionals.get(i).copy();
            if (conditional.getPriority() < conditionals.get(i + 1).getPriority())
            {
                conditionals.set(i, conditionals.get(i+1));
                conditionals.set(i+1, conditional);
            }
        }
    }

    /**
     * @throws NullPointerException if none of the provided Conditionals return a Style;
     * @return The Function that is used for the StyleProvider
     */
    public Style getStyle(LivingEntityPatch<?> entityPatch)
    {
        sortByPriority();
        for (ProviderConditional conditional : conditionals)
        {
            if (conditional.testConditionalStyle(entityPatch) != null)
            {
                return conditional.testConditionalStyle(entityPatch);
            }
        }
        return null;
    }

    /**
     * @throws NullPointerException if none of the provided Conditionals return a Style;
     * @return Boolean
     */
    public Boolean checkVisibleOffHand(LivingEntityPatch<?> entityPatch)
    {
        sortByPriority();
        for (ProviderConditional conditional : conditionals)
        {
            if (conditional.testConditionalCombo(entityPatch) != null)
            {
                return conditional.testConditionalCombo(entityPatch);
            }
        }
        return null;
    }
}
