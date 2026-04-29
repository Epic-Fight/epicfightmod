package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.ex_cap.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@ApiStatus.Experimental
public class ConditionalManager
{
    private static final Map<ResourceLocation, ProviderConditional.Builder> CONDITIONALS = Maps.newHashMap();
    private static final Map<ResourceLocation, ProviderConditional.Builder> REGISTERED_CONDITIONALS = Maps.newHashMap();
    private static boolean FROZEN = false;

    public static ProviderConditional.Builder get(ResourceLocation id) {
        return CONDITIONALS.get(id);
    }

    /**
     * Registers a provider conditional into the global manager.
     * <p>
     * This method links a unique identifier to a conditional builder, allowing the system
     * to evaluate weapon styles and moveset swaps dynamically at runtime.
     * </p>
     *
     * @param id      The unique {@link ResourceLocation} identifier for this condition.
     * @param builder The {@link ProviderConditional.Builder} defining the logic
     * (e.g., style, hand requirements, or skill checks).
     * @return A {@link ConditionalEntry} handle that can be passed directly into
     * {@link yesman.epicfight.world.capabilities.item.WeaponCapability.Builder#addConditionals(ConditionalEntry...)}.
     */
    public static ConditionalEntry register(ResourceLocation id, ProviderConditional.Builder builder) {
        if (FROZEN)
            throw new UnsupportedOperationException("Registry is frozen, cannot register " + id.toString());
        if (REGISTERED_CONDITIONALS.containsKey(id)) {
            throw new IllegalArgumentException("Conditional with ID " + id + " already exists!");
        }
        REGISTERED_CONDITIONALS.put(id, builder);
        return new ConditionalEntry(id, builder);
    }

    public static void freeze()
    {
        EpicFight.LOGGER.info("Freezing conditional registry");
        FROZEN = true;
    }

    public static void add(ResourceLocation id, JsonElement json) {
        CONDITIONALS.put(id, ProviderConditional.Builder.deserialize(json));
    }

    @ApiStatus.Internal
    public static void refresh()
    {
        CONDITIONALS.clear();
        CONDITIONALS.putAll(REGISTERED_CONDITIONALS);
    }

}
