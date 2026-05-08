package yesman.epicfight.api.ex_cap.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.modules.core.events.ConditionalRegistryEvent;
import yesman.epicfight.api.ex_cap.managers.ConditionalManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

@ApiStatus.Experimental
public class ConditionalReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/conditionals";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ConditionalReloadListener()
    {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        @Deprecated
        ConditionalRegistryEvent event = createEvent();
        ConditionalManager.acceptEvent(event);
        elementMap.forEach(ConditionalManager::add);
    }

    @Deprecated
    private static ConditionalRegistryEvent createEvent()
    {
        return EpicFightEventHooks.Registry.EX_CAP_CONDITIONAL_REGISTRATION.post(new ConditionalRegistryEvent());
    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.PROVIDER_CONDITIONAL) {
            @Deprecated
            ConditionalRegistryEvent event = createEvent();
            ConditionalManager.acceptEvent(event);
        }
    }
}
