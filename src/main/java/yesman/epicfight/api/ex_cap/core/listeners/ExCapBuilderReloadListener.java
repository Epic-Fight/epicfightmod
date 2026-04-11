package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.ex_cap.core.events.ExCapBuilderCreationEvent;
import yesman.epicfight.api.ex_cap.core.managers.BuilderManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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
        ModLoader.get().postEvent(exCapBuilderCreationEvent);
        BuilderManager.acceptEvent(exCapBuilderCreationEvent);
        elementMap.forEach(BuilderManager::add);
    }

    public static void sync(SPDatapackSync packet) {
        if (packet.getType() == SPDatapackSync.Type.EX_CAP_DATA){
            ExCapBuilderCreationEvent exCapBuilderCreationEvent = new ExCapBuilderCreationEvent();
            ModLoader.get().postEvent(exCapBuilderCreationEvent);
            BuilderManager.acceptEvent(exCapBuilderCreationEvent);
        }
    }
}