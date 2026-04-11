package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.ex_cap.core.events.ConditionalRegistryEvent;
import yesman.epicfight.api.ex_cap.core.managers.ConditionalManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

public class ExCapConditionalReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/conditionals";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapConditionalReloadListener()
    {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ConditionalRegistryEvent conditionalRegistryEvent = new ConditionalRegistryEvent();
        ModLoader.get().postEvent(conditionalRegistryEvent);
        ConditionalManager.acceptEvent(conditionalRegistryEvent);
        elementMap.forEach(ConditionalManager::add);
    }

    public static void sync(SPDatapackSync packet) {
        if (packet.getType() == SPDatapackSync.Type.EX_CAP_CONDITIONAL) {
            ConditionalRegistryEvent conditionalRegistryEvent = new ConditionalRegistryEvent();
            ModLoader.get().postEvent(conditionalRegistryEvent);
            ConditionalManager.acceptEvent(conditionalRegistryEvent);
        }
    }
}