package yesman.epicfight.api.ex_cap.modules.core.listeners;

import com.google.common.collect.Lists;
import com.google.gson.*;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.api.ex_cap.modules.core.managers.ExCapManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.List;
import java.util.Map;

public class ExCapDataReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/ex_cap_data/loader";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapDataReloadListener()
    {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ExCapabilityBuilderPopulationEvent exCapabilityBuilderPopulationEvent = new ExCapabilityBuilderPopulationEvent();
        EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(exCapabilityBuilderPopulationEvent);
        ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);
        elementMap.forEach(this::deserialize);
    }



    protected void deserialize(ResourceLocation rl, JsonElement jsonElement) {
        EpicFight.LOGGER.info("Deserializing data for Ex Cap from JSON: {}", rl);
        JsonObject obj = jsonElement.getAsJsonObject();
        if (obj.has("target") && obj.get("target").isJsonPrimitive())
        {
            ResourceLocation target = ResourceLocation.tryParse(obj.get("target").getAsString().toLowerCase());
            if (target == null)
            {
                EpicFight.LOGGER.warn("Invalid target ResourceLocation in Ex Cap data JSON: {}", rl);
                return;
            }
            if (obj.has("ex_cap_data") && obj.get("ex_cap_data").isJsonArray())
            {
                JsonArray exCapData = obj.getAsJsonArray("ex_cap_data");
                List<ResourceLocation> exCapDataList = Lists.newArrayList();
                exCapData.asList().forEach(capData -> {
                    if (capData.isJsonPrimitive())
                    {
                        ResourceLocation capDataRL = ResourceLocation.tryParse(capData.getAsString().toLowerCase());
                        if (capDataRL != null)
                        {
                            exCapDataList.add(capDataRL);
                        }
                    }
                });
                ExCapManager.addExCapData(target, exCapDataList);
            }
        }
    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_INJECTION) {
            ExCapManager.acceptEvent(EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(new ExCapabilityBuilderPopulationEvent()));
        }
    }
}
