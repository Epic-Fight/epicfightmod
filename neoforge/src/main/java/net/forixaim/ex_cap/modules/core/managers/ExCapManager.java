package net.forixaim.ex_cap.modules.core.managers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.forixaim.ex_cap.modules.core.data.ExCapData;
import net.forixaim.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.world.capabilities.item.WeaponCapability;

import java.util.Map;
import java.util.List;


public class ExCapManager
{
    private static final Map<WeaponCapability.Builder, List<ExCapData>> dataMap = Maps.newHashMap();

    /**
     * Registers the preset builder as an acceptor
     * @param cap The statically registered Capability Builder
     */
    public static void addAcceptor(WeaponCapability.Builder cap)
    {
        dataMap.putIfAbsent(cap, Lists.newArrayList());
    }

    public static List<ExCapData> retrieveExCapData(WeaponCapability.Builder cap)
    {
        return dataMap.get(cap);
    }

    public static Map<WeaponCapability.Builder, List<ExCapData>> getDataMap()
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

    private static void clearList(WeaponCapability.Builder builder, List<ExCapData> list)
    {
        list.clear();
    }
}
