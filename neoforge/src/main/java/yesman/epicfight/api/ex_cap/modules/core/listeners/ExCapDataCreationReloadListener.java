package yesman.epicfight.api.ex_cap.modules.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;
import yesman.epicfight.api.ex_cap.modules.core.managers.DatasetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

public class ExCapDataCreationReloadListener extends SimpleJsonResourceReloadListener
{

    public static final String DIRECTORY = "capabilities/weapons/ex_cap_data/definitions";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapDataCreationReloadListener()
    {
        super(GSON, DIRECTORY);
    }
    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ExCapDataRegistrationEvent exCapDataRegistrationEvent = new ExCapDataRegistrationEvent();
        EpicFightEventHooks.Registry.EX_CAP_DATA_CREATION.post(exCapDataRegistrationEvent);
        DatasetManager.acceptEvent(exCapDataRegistrationEvent);


    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_DATA){
            DatasetManager.acceptEvent(EpicFightEventHooks.Registry.EX_CAP_DATA_CREATION.post(new ExCapDataRegistrationEvent()));
        }
    }
}
