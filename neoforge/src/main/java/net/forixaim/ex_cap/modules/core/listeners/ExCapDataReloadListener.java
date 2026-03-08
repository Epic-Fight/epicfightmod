package net.forixaim.ex_cap.modules.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.forixaim.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import net.forixaim.ex_cap.modules.core.managers.ExCapManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.event.EpicFightEventHooks;

import java.util.Map;

public class ExCapDataReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/ex_cap_data";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapDataReloadListener()
    {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller)
    {
        ExCapabilityBuilderPopulationEvent exCapabilityBuilderPopulationEvent = new ExCapabilityBuilderPopulationEvent();
        EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(exCapabilityBuilderPopulationEvent);
        ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);
    }
}
