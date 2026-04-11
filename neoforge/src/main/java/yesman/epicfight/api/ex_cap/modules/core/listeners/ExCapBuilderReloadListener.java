package yesman.epicfight.api.ex_cap.modules.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapBuilderCreationEvent;
import yesman.epicfight.api.ex_cap.modules.core.managers.BuilderManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

public class ExCapBuilderReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/excap_builders";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapBuilderReloadListener()
    {
        super(GSON, DIRECTORY);
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, ResourceManager resourceManager, ProfilerFiller profilerFiller)
    {
        ExCapBuilderCreationEvent exCapBuilderCreationEvent = new ExCapBuilderCreationEvent();
        EpicFightEventHooks.Registry.EX_CAP_BUILDER_CREATION.post(exCapBuilderCreationEvent);
        BuilderManager.acceptEvent(exCapBuilderCreationEvent);
        elementMap.forEach(BuilderManager::add);
    }

    public static void processServerPacket(SPDatapackSync packet)
    {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_BUILDER)
        {
            BuilderManager.acceptEvent(EpicFightEventHooks.Registry.EX_CAP_BUILDER_CREATION.post(new ExCapBuilderCreationEvent()));
            //TODO: Fix the registry for utilizing CompoundTag
            packet.tags().forEach(tag -> {
                ResourceLocation rl = ResourceLocation.parse(tag.getString("registry_name"));
            });
        }
    }
}
