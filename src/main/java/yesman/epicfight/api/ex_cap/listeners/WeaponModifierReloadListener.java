package yesman.epicfight.api.ex_cap.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.ex_cap.managers.ModifierManager;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

@ApiStatus.Experimental
public class WeaponModifierReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/modifiers";

    private static final Gson GSON = (new GsonBuilder()).create();

    public WeaponModifierReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected @NotNull Map<ResourceLocation, JsonElement> prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        ModifierManager.acceptEvent();
        return super.prepare(resourceManager, profiler);

    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        ModifierManager.modify();
    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.MODIFIER) {
            ModifierManager.acceptEvent();
            ModifierManager.modify();

        }
    }
}
