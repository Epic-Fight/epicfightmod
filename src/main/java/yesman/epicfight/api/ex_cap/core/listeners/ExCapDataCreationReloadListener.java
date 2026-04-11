package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraftforge.fml.ModLoader;
import yesman.epicfight.api.ex_cap.core.events.ExCapDataRegistrationEvent;
import yesman.epicfight.api.ex_cap.core.managers.DatasetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
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
        ModLoader.get().postEvent(exCapDataRegistrationEvent);
        DatasetManager.acceptEvent(exCapDataRegistrationEvent);
    }

    public static void sync(SPDatapackSync packet) {
        if (packet.getType() == SPDatapackSync.Type.EX_CAP_DATA){
            ExCapDataRegistrationEvent exCapDataRegistrationEvent = new ExCapDataRegistrationEvent();
            ModLoader.get().postEvent(exCapDataRegistrationEvent);
            DatasetManager.acceptEvent(exCapDataRegistrationEvent);
        }
    }
}