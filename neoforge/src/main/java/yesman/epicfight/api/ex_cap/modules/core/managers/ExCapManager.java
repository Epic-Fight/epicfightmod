package yesman.epicfight.api.ex_cap.modules.core.managers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.List;


public class ExCapManager
{
    private static final Map<ResourceLocation, List<ExCapData.Builder>> dataMap = Maps.newHashMap();

    /**
     * Registers the preset builder as an acceptor
     * @param cap The statically registered Capability Builder
     */
    public static void addAcceptor(ResourceLocation cap)
    {
        dataMap.putIfAbsent(cap, Lists.newArrayList());
    }

    public static List<ExCapData.Builder> retrieveExCapData(ResourceLocation cap)
    {
        return dataMap.get(cap);
    }

    public static void addExCapData(ResourceLocation cap, List<ResourceLocation> data)
    {
        List<ExCapData.Builder> list = Lists.newArrayList();
        for (ResourceLocation rl : data)
        {
            list.add(DatasetManager.get(rl));
        }
        dataMap.computeIfAbsent(cap, k -> Lists.newArrayList()).addAll(list);
    }

    public static Map<ResourceLocation, List<ExCapData.Builder>> getDataMap()
    {
        return ImmutableMap.copyOf(dataMap);
    }

    public static void acceptEvent(ExCapabilityBuilderPopulationEvent event)
    {
        clearEntries();
        dataMap.putAll(event.getBuilders());
    }

    public static void clearEntries()
    {
        //Do not clear the actual keys, only clear the lists
        dataMap.forEach(ExCapManager::clearList);
    }

    private static void clearList(ResourceLocation rl, List<ExCapData.Builder> list)
    {
        list.clear();
    }
}
