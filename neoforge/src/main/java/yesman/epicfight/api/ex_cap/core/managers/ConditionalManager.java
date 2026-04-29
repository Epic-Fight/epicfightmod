package yesman.epicfight.api.ex_cap.core.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.core.data.ConditionalEntry;
import yesman.epicfight.api.ex_cap.core.provider.ProviderConditional;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@ApiStatus.Experimental
public class ConditionalManager
{
    private static final Map<ResourceLocation, ProviderConditional.ProviderConditionalBuilder> CONDITIONALS = Maps.newHashMap();
    private static final Map<ResourceLocation, ProviderConditional.ProviderConditionalBuilder> REGISTERED_CONDITIONALS = Maps.newHashMap();

    public static ProviderConditional.ProviderConditionalBuilder get(ResourceLocation id) {
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
     * @param builder The {@link ProviderConditional.ProviderConditionalBuilder} defining the logic
     * (e.g., style, hand requirements, or skill checks).
     * @return A {@link ConditionalEntry} handle that can be passed directly into
     * {@link yesman.epicfight.world.capabilities.item.WeaponCapability.Builder#addConditionals(ConditionalEntry...)}.
     */
    public static ConditionalEntry register(ResourceLocation id, ProviderConditional.ProviderConditionalBuilder builder) {
        REGISTERED_CONDITIONALS.put(id, builder);
        return new ConditionalEntry(id, builder);
    }

    public static void add(ResourceLocation id, JsonElement json) {
        CONDITIONALS.put(id, ProviderConditional.ProviderConditionalBuilder.deserialize(json));
    }

    @ApiStatus.Internal
    public static void acceptEvent()
    {
        CONDITIONALS.clear();
        CONDITIONALS.putAll(REGISTERED_CONDITIONALS);
    }

}
