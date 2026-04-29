package yesman.epicfight.api.ex_cap.core.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.ex_cap.core.managers.ModifierManager;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;

public class WeaponModifierReloadListener extends SimpleJsonResourceReloadListener
{
    public static final String DIRECTORY = "capabilities/weapons/modifiers";
    private static final Gson GSON = (new GsonBuilder()).create();

    public WeaponModifierReloadListener()
    {
        super(GSON, DIRECTORY);
    }


    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> resourceLocationJsonElementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ModifierManager.refresh();
        //TODO: Datapack Logic
        ModifierManager.apply();
    }

    public static void onSync(SPDatapackSync sync)
    {
        if (sync.packetType() == SPDatapackSync.PacketType.WEAPON_MODIFIER)
        {
            ModifierManager.apply();
        }
    }
}
