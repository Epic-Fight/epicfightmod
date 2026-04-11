package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.ex_cap.core.events.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.api.ex_cap.core.managers.ExCapManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.main.EpicFightMod;
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
        ModLoader.get().postEvent(exCapabilityBuilderPopulationEvent);
        ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);

        elementMap.forEach(this::deserialize);
    }

    protected void deserialize(ResourceLocation rl, JsonElement jsonElement) {
        EpicFightMod.LOGGER.info("Deserializing data for Ex Cap from JSON: {}", rl);
        JsonObject obj = jsonElement.getAsJsonObject();
        if (obj.has("target") && obj.get("target").isJsonPrimitive())
        {
            ResourceLocation target = ResourceLocation.tryParse(obj.get("target").getAsString().toLowerCase());
            if (target == null)
            {
                EpicFightMod.LOGGER.warn("Invalid target ResourceLocation in Ex Cap data JSON: {}", rl);
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

    public static void sync(SPDatapackSync packet) {
        if (packet.getType() == SPDatapackSync.Type.EX_CAP_INJECTION) {
            ExCapabilityBuilderPopulationEvent exCapabilityBuilderPopulationEvent = new ExCapabilityBuilderPopulationEvent();
            ModLoader.get().postEvent(exCapabilityBuilderPopulationEvent);
            ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);
        }
    }
}