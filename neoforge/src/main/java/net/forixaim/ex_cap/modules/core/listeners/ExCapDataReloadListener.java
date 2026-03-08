package net.forixaim.ex_cap.modules.core.listeners;

import com.google.gson.Gson;
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
    public ExCapDataReloadListener(Gson gson, String directory)
    {
        super(gson, directory);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller)
    {
        ExCapabilityBuilderPopulationEvent exCapabilityBuilderPopulationEvent = new ExCapabilityBuilderPopulationEvent();
        EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(exCapabilityBuilderPopulationEvent);
        ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);
    }
}
