package yesman.epicfight.api.ex_cap.modules.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import yesman.epicfight.api.ex_cap.modules.core.managers.MovesetManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

public class ExCapMovesetReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/movesets";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapMovesetReloadListener()
    {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ExCapMovesetRegistryEvent exCapMovesetRegistryEvent = new ExCapMovesetRegistryEvent();
        EpicFightEventHooks.Registry.EX_CAP_MOVESET_REGISTRY.post(exCapMovesetRegistryEvent);
        MovesetManager.acceptEvent(exCapMovesetRegistryEvent);
        elementMap.forEach(MovesetManager::add);
    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_MOVESET) {
            MovesetManager.acceptEvent(EpicFightEventHooks.Registry.EX_CAP_MOVESET_REGISTRY.post(new ExCapMovesetRegistryEvent()));
        }
    }
}
