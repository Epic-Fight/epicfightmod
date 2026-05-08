package yesman.epicfight.api.ex_cap.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapBuilderCreationEvent;
import yesman.epicfight.api.ex_cap.managers.ItemPresetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

@ApiStatus.Experimental
public class ItemPresetReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/excap_builders";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ItemPresetReloadListener()
    {
        super(GSON, DIRECTORY);
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        @Deprecated
        ExCapBuilderCreationEvent event = createEvent();
        ItemPresetManager.acceptEvent(event);
    }

    @Deprecated
    private static ExCapBuilderCreationEvent createEvent()
    {
        return EpicFightEventHooks.Registry.EX_CAP_BUILDER_CREATION.post(new ExCapBuilderCreationEvent());
    }

    public static void processServerPacket(SPDatapackSync packet)
    {
        if (packet.packetType() == SPDatapackSync.PacketType.ITEM_PRESET)
        {
            @Deprecated
            ExCapBuilderCreationEvent event = createEvent();
            ItemPresetManager.acceptEvent(event);
            packet.tags().forEach(tag -> {
                //TODO: Add item preset to item preset manager
            });
        }
    }
}
