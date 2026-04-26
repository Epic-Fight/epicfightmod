package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
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
    protected void apply(Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        BuilderManager.acceptEvent();
    }

    public static void processServerPacket(SPDatapackSync packet)
    {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_BUILDER)
        {
            BuilderManager.acceptEvent();
            packet.tags().forEach(tag -> {
                ResourceLocation rl = ResourceLocation.parse(tag.getString("registry_name"));
            });
        }
    }
}
